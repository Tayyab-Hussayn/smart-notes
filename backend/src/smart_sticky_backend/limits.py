"""Single-process backstop. Edge limits are additionally required in production."""
import time
from collections import OrderedDict
from starlette.responses import JSONResponse


class RequestLimits:
    def __init__(self, app):
        self.app = app
        self.windows = OrderedDict()

    async def __call__(self, scope, receive, send):
        if scope["type"] != "http":
            return await self.app(scope, receive, send)
        path = scope.get("path", "")
        if path.startswith("/v1/"):
            now = time.monotonic()
            auth = path.startswith("/v1/auth/")
            key = ((scope.get("client") or ("unknown",))[0], auth)
            start, count = self.windows.get(key, (now, 0))
            if now - start >= 60:
                start, count = now, 0
            self.windows[key] = (start, count + 1)
            self.windows.move_to_end(key)
            while len(self.windows) > 10000:
                self.windows.popitem(last=False)
            if count >= (10 if auth else 120):
                return await JSONResponse({"code": "rate_limited"}, 429, headers={"Retry-After": "60"})(scope, receive, send)
        # Buffer at most 128 KiB before JSON decoding, including chunked requests.
        chunks = []
        size = 0
        while True:
            message = await receive()
            if message["type"] == "http.disconnect":
                return
            size += len(message.get("body", b""))
            if size > 131072:
                return await JSONResponse({"code": "request_too_large"}, 413)(scope, receive, send)
            chunks.append(message.get("body", b""))
            if not message.get("more_body", False):
                break
        delivered = False

        async def buffered_receive():
            nonlocal delivered
            if not delivered:
                delivered = True
                return {"type": "http.request", "body": b"".join(chunks), "more_body": False}
            return await receive()

        await self.app(scope, buffered_receive, send)
