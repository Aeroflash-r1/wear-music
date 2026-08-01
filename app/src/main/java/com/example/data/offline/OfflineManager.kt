package com.example.data.offline

import com.example.domain.model.NetworkState
import com.example.domain.repository.OfflineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineManager @Inject constructor(
    private val networkMonitor: NetworkMonitor
) : OfflineRepository {

    private val _forceOffline = MutableStateFlow(false)

    override fun isOffline(): Flow<Boolean> = combine(
        networkMonitor.networkState,
        _forceOffline.asStateFlow()
    ) { netState, forced ->
        forced || netState == NetworkState.DISCONNECTED
    }

    override fun getNetworkState(): Flow<NetworkState> = networkMonitor.networkState

    override fun setForceOfflineMode(enabled: Boolean) {
        _forceOffline.value = enabled
    }

    override fun isForceOfflineMode(): Flow<Boolean> = _forceOffline.asStateFlow()
}
