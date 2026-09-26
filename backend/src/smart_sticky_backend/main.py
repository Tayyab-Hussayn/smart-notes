from fastapi import FastAPI
from starlette.middleware.trustedhost import TrustedHostMiddleware

from .config import Settings
from .database import Database
from .api import router
from .limits import RequestLimits
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse


def create_app(settings: Settings | None = None) -> FastAPI:
    settings = settings or Settings.from_env()
    app = FastAPI(
        title="Smart Sticky API",
        docs_url=None,
        redoc_url=None,
        openapi_url=None,
    )
    app.add_middleware(TrustedHostMiddleware, allowed_hosts=list(settings.allowed_hosts))
    app.add_middleware(RequestLimits)
    database = Database(settings.database_url) if settings.database_url else None
    app.include_router(router(database))

    @app.exception_handler(RequestValidationError)
    async def invalid_request(request, exception):
        # Pydantic errors include rejected input; never echo passwords or notes.
        return JSONResponse(status_code=422, content={"code": "invalid_request"})

    @app.exception_handler(Exception)
    async def unavailable(request, exception):
        return JSONResponse(status_code=503, content={"code": "service_unavailable"})

    # Handle driver failures inside ExceptionMiddleware so SQL diagnostics aren't
    # re-raised through the server's traceback logger (which may include values).
    try:
        from psycopg import Error as DatabaseError
    except ImportError:
        pass
    else:
        app.add_exception_handler(DatabaseError, unavailable)

    @app.middleware("http")
    async def privacy_headers(request, call_next):
        response = await call_next(request)
        response.headers["Cache-Control"] = "no-store"
        response.headers["X-Content-Type-Options"] = "nosniff"
        return response

    @app.get("/health/live")
    async def live() -> dict[str, str]:
        # Liveness only: this intentionally makes no database/readiness claim.
        return {"status": "alive"}

    return app
