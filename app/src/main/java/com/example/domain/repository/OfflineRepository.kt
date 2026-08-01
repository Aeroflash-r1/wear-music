package com.example.domain.repository

import com.example.domain.model.NetworkState
import kotlinx.coroutines.flow.Flow

interface OfflineRepository {
    fun isOffline(): Flow<Boolean>
    fun getNetworkState(): Flow<NetworkState>
    fun setForceOfflineMode(enabled: Boolean)
    fun isForceOfflineMode(): Flow<Boolean>
}
