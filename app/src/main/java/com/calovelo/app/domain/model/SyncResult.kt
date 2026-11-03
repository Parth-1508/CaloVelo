package com.calovelo.app.domain.model

enum class WearableAccess {
    READY,
    PERMISSION_REQUIRED,
    PROVIDER_INSTALL_OR_UPDATE,
    UNAVAILABLE
}

sealed interface SyncResult {
    data class Success(val stats: HealthStats) : SyncResult
    data class Blocked(val access: WearableAccess) : SyncResult
    data class Failure(val message: String) : SyncResult
}
