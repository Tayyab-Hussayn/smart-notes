# Android local client

Opt in with `./gradlew -PenableAndroid=true :client:android:assembleDebug`.
Requires Android SDK 35 and Java 17. This module is excluded from SDK-free desktop builds.

The launcher provides local note creation, editing, pinning, archive, recoverable trash,
and explicit wallpaper exposure. Data uses the shared SQLDelight schema and domain.
Android application backup is disabled; the manifest requests no network permissions.

The live wallpaper shows up to four active, explicitly exposed notes. A worker reads
fresh database snapshots every 30 seconds only while visible; selection favors pinned
notes and rotates others through shared selection logic. Hidden/archived/deleted changes
take effect at the next refresh. System wallpaper selection remains a user action.
Some launchers apply live wallpaper to the lock screen too; the UI warns about this.

This is an initial native client, not a completed Android release. Device installation,
OEM launcher behavior, accessibility inspection, process restoration, background lifecycle,
database upgrades, and Android compilation need verification on SDK/device infrastructure.
Reminders, full canvas manipulation, configurable rotation, billing and cloud integration
are not exposed here. Existing shared reminder tables are preserved by schema migrations.
