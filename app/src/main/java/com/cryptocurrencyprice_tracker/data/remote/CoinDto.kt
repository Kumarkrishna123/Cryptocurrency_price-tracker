package com.cryptocurrencyprice_tracker.data.remote

import com.google.gson.annotations.SerializedName

data class CoinDto(
    @SerializedName("id") val id: String = "",
    @SerializedName("symbol") val symbol: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("image") val image: String? = null,
    @SerializedName("current_price") val currentPrice: Double? = null,
    @SerializedName("price_change_percentage_24h") val priceChangePercentage24h: Double? = null,
    @SerializedName("market_cap") val marketCap: Double? = null,
    @SerializedName("market_cap_rank") val marketCapRank: Int? = null,
    @SerializedName("total_volume") val totalVolume: Double? = null,
    @SerializedName("high_24h") val high24h: Double? = null,
    @SerializedName("low_24h") val low24h: Double? = null,
)
