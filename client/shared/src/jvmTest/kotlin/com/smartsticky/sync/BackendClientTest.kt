package com.smartsticky.sync

import kotlinx.serialization.json.*
import kotlin.test.*

class BackendClientTest {
    private val id = "9e166bc8-06e1-4197-8603-3ebd78ba7e1f"
    private val token = "abcdefghijklmnopqrstuvwxyz0123456789"
    private val sessionJson get() = """{"user_id":"$id","access_token":"$token","token_type":"bearer","expires_in":1800}"""

    @Test fun insecureRemoteAndCredentialUrlsRejected() {
        for (url in listOf("http://example.com", "https://user:password@example.com", "https://example.com/path", "https://example.com?token=secret", "https://example.com#secret")) {
            assertFailsWith<IllegalArgumentException> { BackendClient(url, allowLocalHttp = true) }
        }
        assertFailsWith<IllegalArgumentException> { BackendClient("http://localhost:8000") }
        BackendClient("http://127.0.0.1:8000", allowLocalHttp = true)
    }

    @Test fun loginEncodesCredentialsWithoutAuthorizationAndRedactsSession() {
        val client = BackendClient("https://api.example.com", transport = BackendTransport { request ->
            assertEquals("/v1/auth/login", request.uri.path)
            assertEquals("POST", request.method)
            assertNull(request.headers["Authorization"])
            assertEquals("application/json", request.headers["Content-Type"])
            val body = Json.parseToJsonElement(request.body!!).jsonObject
            assertEquals("private\"password", body.getValue("password").jsonPrimitive.content)
            BackendResponse(200, sessionJson)
        })
        val session = client.login("a@example.com", "private\"password")
        assertEquals(id, session.accountId)
        assertEquals(token, session.token)
        assertEquals(1800L, session.expiresInSeconds)
        assertFalse(session.toString().contains(token))
    }

    @Test fun registrationRenewalLogoutUseProtocol() {
        val requests = mutableListOf<HttpBackendRequest>()
        val client = BackendClient("https://api.example.com", transport = BackendTransport {
            requests.add(it)
            if (it.uri.path.endsWith("logout")) BackendResponse(204, "") else BackendResponse(200, sessionJson)
        })
        client.register("a@example.com", "password")
        client.renew(token)
        client.logout(token)
        assertEquals(listOf("/v1/auth/register", "/v1/auth/renew", "/v1/auth/logout"), requests.map { it.uri.path })
        assertEquals("Bearer $token", requests[1].headers["Authorization"])
        assertEquals("Bearer $token", requests[2].headers["Authorization"])
        assertNull(requests[2].body)
    }

    @Test fun pushPreservesOperationAndPayload() {
        val client = BackendClient("https://api.example.com", transport = BackendTransport {
            val body = Json.parseToJsonElement(it.body!!).jsonObject
            assertEquals("Bearer $token", it.headers["Authorization"])
            assertEquals(id, body.getValue("operation_id").jsonPrimitive.content)
            assertEquals(7L, body.getValue("base_revision").jsonPrimitive.long)
            assertEquals("my note", body.getValue("note").jsonObject.getValue("body").jsonPrimitive.content)
            BackendResponse(200, """{"operation_id":"$id","note_id":"$id","revision":8,"deleted":false}""")
        })
        assertEquals(8L, client.push(token, Mutation(id, id, 7, "upsert", NotePayload(body = "my note"))).revision)
    }

    @Test fun pullMapsTombstonesAndPagination() {
        val client = BackendClient("https://api.example.com", transport = BackendTransport {
            assertEquals("after=9&limit=1", it.uri.query)
            BackendResponse(200, """{"changes":[{"id":"$id","revision":10,"deleted":true,"payload":{}}],"cursor":10,"has_more":true}""")
        })
        val page = client.pull(token, 9, 1)
        assertEquals(10L, page.cursor)
        assertTrue(page.hasMore)
        assertNull(page.changes.single().payload)
    }

    @Test fun conflictIncludesCurrentRevisionButDoesNotExposeContentInError() {
        val client = BackendClient("https://api.example.com", transport = BackendTransport {
            BackendResponse(409, """{"detail":{"code":"revision_conflict","current":{"revision":4,"deleted":false,"payload":{"title":"","body":"private body","priority":"normal","pinned":false,"archived":false}}}}""")
        })
        val error = assertFailsWith<BackendException> { client.push(token, Mutation(id, id, 2, "delete")) }
        assertEquals("revision_conflict", error.code)
        assertEquals(4L, error.current?.revision)
        assertEquals(id, error.current?.id)
        assertFalse(error.toString().contains("private body"))
    }

    @Test fun malformedAndUntrustedErrorsRemainSanitized() {
        for ((status, body, expected) in listOf(Triple(200, "not json", "invalid_response"), Triple(500, """{"detail":"secret note text"}""", "http_error"), Triple(401, """{"detail":"invalid_session"}""", "invalid_session"), Triple(302, "", "http_error"))) {
            val client = BackendClient("https://api.example.com", transport = BackendTransport { BackendResponse(status, body) })
            val error = assertFailsWith<BackendException> { client.pull(token) }
            assertEquals(expected, error.code)
            assertFalse(error.toString().contains("secret note text"))
        }
    }
}
