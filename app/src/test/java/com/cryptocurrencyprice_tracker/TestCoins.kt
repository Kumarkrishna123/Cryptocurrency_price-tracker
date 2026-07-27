package com.cryptocurrencyprice_tracker

import com.cryptocurrencyprice_tracker.domain.Coin

fun coin(
    id: String = "bitcoin",
    symbol: String = "BTC",
    name: String = "Bitcoin",
    imageUrl: String? = null,
    price: Double = 65_000.0,
    change24h: Double = 1.5,
    marketCap: Double? = 1_280_000_000_000.0,
    marketCapRank: Int? = 1,
    totalVolume: Double? = 25_000_000_000.0,
    high24h: Double? = 66_000.0,
    low24h: Double? = 64_000.0,
) = Coin(
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
