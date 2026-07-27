package com.cryptocurrencyprice_tracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [CoinEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class CryptoDatabase : RoomDatabase() {
    abstract fun coinDao(): CoinDao

    companion object {
        const val NAME = "crypto.db"
    }
}
