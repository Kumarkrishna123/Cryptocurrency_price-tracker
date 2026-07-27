package com.cryptocurrencyprice_tracker.data

import com.cryptocurrencyprice_tracker.data.local.CoinEntity
import com.cryptocurrencyprice_tracker.data.remote.CoinDto
import com.cryptocurrencyprice_tracker.domain.Coin

internal fun CoinDto.toEntity(listPosition: Int) = CoinEntity(
    id = id,
    symbol = symbol.uppercase(),
    name = name,
    imageUrl = image,
    price = currentPrice ?: 0.0,
    change24h = priceChangePercentage24h ?: 0.0,
    marketCap = marketCap,
    marketCapRank = marketCapRank,
    totalVolume = totalVolume,
    high24h = high24h,
    low24h = low24h,
    listPosition = listPosition,
)

internal fun CoinEntity.toCoin() = Coin(
    id = id,
    symbol = symbol,
    name = name,
    imageUrl = imageUrl,
    price = price,
    change24h = change24h,
    marketCap = marketCap,
    marketCapRank = marketCapRank,
    totalVolume = totalVolume,
    high24h = high24h,
    low24h = low24h,
)
