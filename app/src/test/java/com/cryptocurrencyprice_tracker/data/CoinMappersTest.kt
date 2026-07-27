package com.cryptocurrencyprice_tracker.data

import com.cryptocurrencyprice_tracker.data.remote.CoinDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CoinMappersTest {

    @Test
    fun `uppercases the symbol so rows render consistently`() {
        val entity = CoinDto(id = "bitcoin", symbol = "btc", name = "Bitcoin").toEntity(0)

        assertEquals("BTC", entity.symbol)
    }

    @Test
    fun `falls back to zero for the always-displayed price and change`() {
        val entity = CoinDto(id = "x", currentPrice = null, priceChangePercentage24h = null)
            .toEntity(0)

        assertEquals(0.0, entity.price, 0.0)
        assertEquals(0.0, entity.change24h, 0.0)
    }

    @Test
    fun `keeps optional stats null rather than inventing zeros`() {
        val entity = CoinDto(id = "x").toEntity(0)

        assertNull(entity.marketCap)
        assertNull(entity.marketCapRank)
        assertNull(entity.totalVolume)
        assertNull(entity.high24h)
        assertNull(entity.low24h)
    }

    @Test
    fun `records list position so the DB can reproduce the API ordering`() {
        val entity = CoinDto(id = "ethereum").toEntity(listPosition = 3)

        assertEquals(3, entity.listPosition)
    }

    @Test
    fun `carries every field through DTO to entity to domain`() {
        val dto = CoinDto(
            id = "bitcoin",
            symbol = "btc",
            name = "Bitcoin",
            image = "https://example.com/btc.png",
            currentPrice = 65_000.0,
            priceChangePercentage24h = -2.5,
            marketCap = 1_280_000_000_000.0,
            marketCapRank = 1,
            totalVolume = 25_000_000_000.0,
            high24h = 66_000.0,
            low24h = 64_000.0,
        )

        val coin = dto.toEntity(0).toCoin()

        assertEquals("bitcoin", coin.id)
        assertEquals("BTC", coin.symbol)
        assertEquals("Bitcoin", coin.name)
        assertEquals("https://example.com/btc.png", coin.imageUrl)
        assertEquals(65_000.0, coin.price, 0.0)
        assertEquals(-2.5, coin.change24h, 0.0)
        assertEquals(1_280_000_000_000.0, coin.marketCap!!, 0.0)
        assertEquals(1, coin.marketCapRank)
        assertEquals(25_000_000_000.0, coin.totalVolume!!, 0.0)
        assertEquals(66_000.0, coin.high24h!!, 0.0)
        assertEquals(64_000.0, coin.low24h!!, 0.0)
    }
}
