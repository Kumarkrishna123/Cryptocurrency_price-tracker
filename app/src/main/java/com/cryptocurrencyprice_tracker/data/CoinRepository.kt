package com.cryptocurrencyprice_tracker.data

import com.cryptocurrencyprice_tracker.data.local.CoinDao
import com.cryptocurrencyprice_tracker.data.remote.CoinGeckoApi
import com.cryptocurrencyprice_tracker.domain.Coin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoinRepository @Inject constructor(
    private val api: CoinGeckoApi,
    private val dao: CoinDao,
) {

    fun observeCoins(): Flow<List<Coin>> =
        dao.observeAll().map { entities -> entities.map { it.toCoin() } }

    fun observeCoin(id: String): Flow<Coin?> =
        dao.observeById(id).map { it?.toCoin() }

    suspend fun refreshCoins() {
        val response = api.getMarkets()
        dao.replaceAll(response.mapIndexed { index, dto -> dto.toEntity(index) })
    }
}
