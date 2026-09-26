package com.smartsticky.sync

import java.io.ByteArrayOutputStream
import java.net.URI
import java.net.HttpURLConnection
import java.util.UUID
import java.security.MessageDigest
import kotlinx.serialization.json.*

class Session(val accountId: String, val token: String, val expiresInSeconds: Long) {
    override fun toString() = "Session(accountId=$accountId, token=<redacted>)"
}
data class NotePayload(val title: String = "", val body: String, val priority: String = "normal", val pinned: Boolean = false, val archived: Boolean = false)
data class Mutation(val operationId: String, val noteId: String, val baseRevision: Long, val kind: String, val note: NotePayload? = null)
data class MutationAck(val operationId: String, val noteId: String, val revision: Long, val deleted: Boolean)
data class RemoteNote(val id: String, val revision: Long, val deleted: Boolean, val payload: NotePayload?)
data class SyncPage(val changes: List<RemoteNote>, val cursor: Long, val hasMore: Boolean)
class BackendException(val status: Int, val code: String, val current: RemoteNote? = null) : Exception("Backend request failed ($status, $code)")
class BackendResponse(val status: Int, val body: String)
class HttpBackendRequest(val uri: URI, val method: String, val headers: Map<String, String>, val body: String?)
fun interface BackendTransport { fun execute(request: HttpBackendRequest): BackendResponse }

/** No redirects; connect/read timeouts, response-size cap, and elapsed body-read limit. */
class JdkBackendTransport : BackendTransport {
    override fun execute(request: HttpBackendRequest): BackendResponse {
        val connection = request.uri.toURL().openConnection() as HttpURLConnection
        connection.instanceFollowRedirects = false
        connection.connectTimeout = 10_000
        connection.readTimeout = 30_000
        connection.requestMethod = request.method
        request.headers.forEach { (name, value) -> connection.setRequestProperty(name, value) }
        val started = System.nanoTime()
        try {
            request.body?.let {
                connection.doOutput = true
                val body = it.toByteArray(Charsets.UTF_8)
                connection.setFixedLengthStreamingMode(body.size)
                connection.outputStream.use { stream -> stream.write(body) }
            }
            val status = connection.responseCode
            val bytes = ByteArrayOutputStream()
            (if (status >= 400) connection.errorStream else connection.inputStream)?.use { stream ->
                val buffer = ByteArray(8192)
                while (true) {
                    val remaining = 30_000 - (System.nanoTime() - started) / 1_000_000
                    if (Thread.currentThread().isInterrupted || remaining <= 0) throw BackendException(0, "request_timeout")
                    connection.readTimeout = remaining.toInt()
                    val count = stream.read(buffer)
                    if (count < 0) break
                    if (bytes.size().toLong() + count > MAX_RESPONSE_BYTES) throw BackendException(status, "response_too_large")
                    bytes.write(buffer, 0, count)
                }
            }
            return BackendResponse(status, bytes.toByteArray().toString(Charsets.UTF_8))
        } catch (error: BackendException) {
            throw error
        } catch (_: Exception) {
            throw BackendException(0, "network_failure")
        } finally { connection.disconnect() }
    }
}
private const val MAX_RESPONSE_BYTES = 16 * 1024 * 1024 // Oversized pages fail safely; callers can lower page size.

/** Blocking API: callers must use a background IO dispatcher. Tokens stay in caller memory. */
class BackendClient(baseUrl: String, allowLocalHttp: Boolean = false, private val transport: BackendTransport = JdkBackendTransport()) {
    private val base: URI = URI(baseUrl.trimEnd('/'))
    init {
        require(base.host != null && base.userInfo == null && base.query == null && base.fragment == null && base.path.isNullOrEmpty()) { "Backend URL must contain only scheme, host, and optional port" }
        require(base.scheme == "https" || (allowLocalHttp && base.scheme == "http" && base.host in setOf("localhost", "127.0.0.1", "[::1]"))) { "HTTPS is required except explicit localhost development" }
    }
    /** Local records are isolated by server origin as well as authenticated user. */
    fun localAccountId(session: Session): String {
        val port = if (base.port == -1) (if (base.scheme == "https") 443 else 80) else base.port
        val origin = "${base.scheme.lowercase()}://${base.host.lowercase()}:$port"
        val digest = MessageDigest.getInstance("SHA-256").digest(origin.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it.toInt() and 255) }
        return "$digest:${session.accountId}"
    }
    fun register(email: String, password: String): Session = authenticate("register", email, password)
    fun login(email: String, password: String): Session = authenticate("login", email, password)
    private fun authenticate(action: String, email: String, password: String) = session(request("POST", "/v1/auth/$action", body = buildJsonObject { put("email", email); put("password", password) }))
    fun renew(token: String): Session = session(request("POST", "/v1/auth/renew", token))
    fun logout(token: String) { request("POST", "/v1/auth/logout", token) }
    fun push(token: String, mutation: Mutation): MutationAck {
        UUID.fromString(mutation.operationId); UUID.fromString(mutation.noteId)
        require(mutation.baseRevision in 0 until Long.MAX_VALUE)
        require(mutation.kind in setOf("upsert", "delete") && (mutation.kind == "upsert") == (mutation.note != null))
        val body = buildJsonObject {
            put("operation_id", mutation.operationId); put("note_id", mutation.noteId)
            put("base_revision", mutation.baseRevision); put("kind", mutation.kind)
            mutation.note?.let { note -> put("note", buildJsonObject {
                put("title", note.title); put("body", note.body); put("priority", note.priority)
                put("pinned", note.pinned); put("archived", note.archived)
            }) }
        }
        val value = request("POST", "/v1/sync/operations", token, body, mutation.noteId)
        return parse { MutationAck(value.text("operation_id"), value.text("note_id"), value.number("revision"), value.flag("deleted")) }
    }
    fun pull(token: String, after: Long = 0, limit: Int = 100): SyncPage {
        require(after >= 0 && limit in 1..500)
        val value = request("GET", "/v1/sync/changes?after=$after&limit=$limit", token)
        return parse { SyncPage(value.getValue("changes").jsonArray.map { remote(it.jsonObject) }, value.number("cursor"), value.flag("has_more")) }
    }
    private fun session(value: JsonObject) = parse {
        require(value.text("token_type") == "bearer")
        val token = value.text("access_token")
        require(token.matches(Regex("[A-Za-z0-9_-]{20,120}")))
        val id = value.text("user_id"); UUID.fromString(id)
        val expiry = value.number("expires_in"); require(expiry > 0)
        Session(id, token, expiry)
    }
    private fun request(method: String, path: String, token: String? = null, body: JsonObject? = null, conflictId: String? = null): JsonObject {
        val headers = mutableMapOf("Accept" to "application/json")
        token?.let { require(it.matches(Regex("[A-Za-z0-9_-]{20,120}"))); headers["Authorization"] = "Bearer $it" }
        if (body != null) headers["Content-Type"] = "application/json"
        val response = try { transport.execute(HttpBackendRequest(base.resolve(path), method, headers, body?.toString())) } catch (e: BackendException) { throw e } catch (_: Exception) { throw BackendException(0, "network_failure") }
        if (response.body.toByteArray(Charsets.UTF_8).size > MAX_RESPONSE_BYTES) throw BackendException(response.status, "response_too_large")
        if (response.status !in 200..299) {
            val detail = runCatching { Json.parseToJsonElement(response.body).jsonObject["detail"] }.getOrNull()
            val rawCode = when (detail) { is JsonPrimitive -> detail.content; is JsonObject -> (detail["code"] as? JsonPrimitive)?.content; else -> null }
            // Never copy arbitrary server messages (which can contain private data) to exceptions.
            val code = rawCode?.takeIf { it in ERROR_CODES } ?: "http_error"
            val current = (detail as? JsonObject)?.get("current") as? JsonObject
            val remote = if (current != null && conflictId != null) parse { remote(current, conflictId) } else null
            throw BackendException(response.status, code, remote)
        }
        return if (response.status == 204) buildJsonObject {} else parse { Json.parseToJsonElement(response.body).jsonObject }
    }
    private fun remote(value: JsonObject, fallbackId: String? = null): RemoteNote {
        val deleted = value.flag("deleted")
        val payload = if (deleted) null else value.getValue("payload").jsonObject.let {
            NotePayload(it.text("title"), it.text("body"), it.text("priority"), it.flag("pinned"), it.flag("archived"))
        }
        val id = fallbackId ?: value.text("id")
        UUID.fromString(id)
        val revision = value.number("revision")
        require(revision > 0)
        payload?.let { require(it.priority in setOf("low", "normal", "high")) }
        return RemoteNote(id, revision, deleted, payload)
    }
    private fun <T> parse(block: () -> T): T = try { block() } catch (_: Exception) { throw BackendException(0, "invalid_response") }
    private fun JsonObject.text(key: String) = getValue(key).jsonPrimitive.let { require(it.isString); it.content }
    private fun JsonObject.number(key: String) = getValue(key).jsonPrimitive.long
    private fun JsonObject.flag(key: String) = getValue(key).jsonPrimitive.boolean
    private companion object {
        val ERROR_CODES = setOf("authentication_required", "invalid_session", "invalid_credentials", "registration_unavailable", "database_not_configured", "revision_conflict", "note_deleted", "note_not_found", "operation_id_reused", "rate_limited")
    }
}
