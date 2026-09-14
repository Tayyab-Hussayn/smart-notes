from fastapi import FastAPI
from starlette.middleware.trustedhost import TrustedHostMiddleware

from .config import Settings


def create_app(settings: Settings | None = None) -> FastAPI:
    settings = settings or Settings.from_env()
    app = FastAPI(
        title="Smart Sticky API",
        docs_url=None,
        redoc_url=None,
        openapi_url=None,
    )
    app.add_middleware(TrustedHostMiddleware, allowed_hosts=list(settings.allowed_hosts))

    @app.get("/health/live")
    async def live() -> dict[str, str]:
        # Liveness only: this intentionally makes no database/readiness claim.
        return {"status": "alive"}

    return app
