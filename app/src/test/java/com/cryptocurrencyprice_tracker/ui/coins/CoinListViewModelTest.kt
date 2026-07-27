package com.cryptocurrencyprice_tracker.ui.coins

import com.cryptocurrencyprice_tracker.coin
import com.cryptocurrencyprice_tracker.data.CoinRepository
import com.cryptocurrencyprice_tracker.domain.Coin
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class CoinListViewModelTest {

    private val repository: CoinRepository = mockk()

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun happyPath(coins: List<Coin>) {
        every { repository.observeCoins() } returns flowOf(coins)
        coEvery { repository.refreshCoins() } returns Unit
    }

    @Test
    fun `puts cached coins on screen`() = runTest {
        val btc = coin(id = "bitcoin")
        happyPath(listOf(btc))

        val viewModel = CoinListViewModel(repository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf(btc), state.allCoins)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `blocking error when the refresh fails and nothing is cached`() = runTest {
        every { repository.observeCoins() } returns flowOf(emptyList())
        coEvery { repository.refreshCoins() } throws IOException("no route to host")

        val viewModel = CoinListViewModel(repository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("No internet connection. Check your network and try again.", state.error)
        assertNull(state.refreshError)
        assertFalse(state.isLoading)
    }

    @Test
    fun `snackbar error when the refresh fails but stale rows are showing`() = runTest {
        every { repository.observeCoins() } returns flowOf(listOf(coin()))
        coEvery { repository.refreshCoins() } throws IOException("offline")

        val viewModel = CoinListViewModel(repository)
        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertEquals(
            "No internet connection. Check your network and try again.",
            state.refreshError,
        )
        assertNull(state.error)
        assertEquals(1, state.allCoins.size)
    }

    @Test
    fun `retires the snackbar message once shown`() = runTest {
        every { repository.observeCoins() } returns flowOf(listOf(coin()))
        coEvery { repository.refreshCoins() } throws IOException("offline")

        val viewModel = CoinListViewModel(repository)
        advanceUntilIdle()
        viewModel.onRefreshErrorShown()

         assertNull(viewModel.uiState.value.refreshError)
    }

    @Test
    fun `search matches the name case-insensitively`() = runTest {
        happyPath(listOf(coin(id = "bitcoin", name = "Bitcoin", symbol = "BTC"), coin(id = "ethereum", name = "Ethereum", symbol = "ETH")))

        val viewModel = CoinListViewModel(repository)
        advanceUntilIdle()
        viewModel.onQueryChange("ETHER")

        assertEquals(listOf("ethereum"), viewModel.uiState.value.visibleCoins.map { it.id })
    }

    @Test
    fun `search also matches the ticker symbol`() = runTest {
        happyPath(listOf(coin(id = "bitcoin", name = "Bitcoin", symbol = "BTC"), coin(id = "ethereum", name = "Ethereum", symbol = "ETH")))

        val viewModel = CoinListViewModel(repository)
        advanceUntilIdle()

        viewModel.onQueryChange("btc")

        assertEquals(listOf("bitcoin"), viewModel.uiState.value.visibleCoins.map { it.id })
    }

    @Test
    fun `market cap sort preserves the order the database returned`() = runTest {
        happyPath(listOf(coin(id = "a", price = 1.0), coin(id = "b", price = 500.0)))

        val viewModel = CoinListViewModel(repository)
        advanceUntilIdle()


        assertEquals(listOf("a", "b"), viewModel.uiState.value.visibleCoins.map { it.id })
    }

    @Test
    fun `price sort puts the most expensive coin first`() = runTest {
        happyPath(listOf(coin(id = "cheap", price = 1.0), coin(id = "pricey", price = 500.0)))

        val viewModel = CoinListViewModel(repository)
        advanceUntilIdle()
        viewModel.onSortChange(CoinSort.PriceDesc)

        assertEquals(listOf("pricey", "cheap"), viewModel.uiState.value.visibleCoins.map { it.id })
    }

    @Test
    fun `change sort puts the biggest gainer first`() = runTest {
        happyPath(listOf(coin(id = "loser", change24h = -8.0), coin(id = "winner", change24h = 12.0)))

        val viewModel = CoinListViewModel(repository)
        advanceUntilIdle()
        viewModel.onSortChange(CoinSort.ChangeDesc)

        assertEquals(listOf("winner", "loser"), viewModel.uiState.value.visibleCoins.map { it.id })
    }

    @Test
    fun `distinguishes an empty search result from an empty cache`() = runTest {
        happyPath(listOf(coin(id = "bitcoin", name = "Bitcoin", symbol = "BTC")))

        val viewModel = CoinListViewModel(repository)
        advanceUntilIdle()
        viewModel.onQueryChange("dogecoin")

        val state = viewModel.uiState.value

        assertTrue(state.visibleCoins.isEmpty())
        assertTrue(state.isEmptySearchResult)
    }

    @Test
    fun `ignores a refresh while one is already running`() = runTest {
        happyPath(listOf(coin()))


        val viewModel = CoinListViewModel(repository)
        viewModel.refresh(isPullToRefresh = true)

        advanceUntilIdle()


        coVerify(exactly = 1) { repository.refreshCoins() }
    }
}
