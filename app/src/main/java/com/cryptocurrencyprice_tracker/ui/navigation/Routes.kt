package com.cryptocurrencyprice_tracker.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
data object CoinListRoute

@Serializable
data class CoinDetailRoute(val coinId: String)
