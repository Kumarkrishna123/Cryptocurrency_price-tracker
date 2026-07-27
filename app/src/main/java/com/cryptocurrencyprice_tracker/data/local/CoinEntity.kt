package com.cryptocurrencyprice_tracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "coins")
data class CoinEntity(
    @PrimaryKey val id: String,
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
    val listPosition: Int,
)
