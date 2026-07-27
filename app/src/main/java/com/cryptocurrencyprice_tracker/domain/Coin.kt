package com.cryptocurrencyprice_tracker.domain

data class Coin(
    val id: String,
    val symbol: String,
    val name: String,
    val imageUrl: String?,
    val price: Double,
    val change24h: Double,
    val marketCap: Double?,
    val marketCapRank: Int?,
    val totalVolume: Double?,
    val high24h: Double?,
    val low24h: Double?,
)
