package com.smartsticky.platform

enum class Capability { PERSISTENT_SURFACE, NOTIFICATIONS, BACKGROUND_SCHEDULING, SECURE_CREDENTIAL_STORAGE, BILLING, ADS }

sealed class CapabilityStatus {
    data object Supported : CapabilityStatus()
    data class Limited(val reason: String) : CapabilityStatus()
    data class PermissionRequired(val permission: String) : CapabilityStatus()
    data class Unavailable(val reason: String) : CapabilityStatus()
}

/** Adapters must positively establish support; platform name alone grants nothing. */
class CapabilityRegistry(private val detected: Map<Capability, CapabilityStatus>) {
    fun status(capability: Capability): CapabilityStatus = detected[capability]
        ?: CapabilityStatus.Unavailable("This integration is not available in this build")
}
