package com.cryptocurrencyprice_tracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface CoinDao {

    @Query("SELECT * FROM coins ORDER BY listPosition ASC")
    fun observeAll(): Flow<List<CoinEntity>>

    @Query("SELECT * FROM coins WHERE id = :id")
    fun observeById(id: String): Flow<CoinEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(coins: List<CoinEntity>)

    @Query("DELETE FROM coins")
    suspend fun deleteAll()

    @Transaction
    suspend fun replaceAll(coins: List<CoinEntity>) {
        deleteAll()
        upsertAll(coins)
    }
}
