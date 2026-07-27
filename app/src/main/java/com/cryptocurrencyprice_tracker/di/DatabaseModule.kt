package com.cryptocurrencyprice_tracker.di

import android.content.Context
import androidx.room.Room
import com.cryptocurrencyprice_tracker.data.local.CoinDao
import com.cryptocurrencyprice_tracker.data.local.CryptoDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): CryptoDatabase = Room.databaseBuilder(
        context,
        CryptoDatabase::class.java,
        CryptoDatabase.NAME,
    )
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()

    @Provides
    @Singleton
    fun provideCoinDao(database: CryptoDatabase): CoinDao = database.coinDao()
}
