package com.cryptocurrencyprice_tracker.ui.detail

import androidx.lifecycle.SavedStateHandle
import com.cryptocurrencyprice_tracker.coin
import com.cryptocurrencyprice_tracker.data.CoinRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
class CoinDetailViewModelTest {

    private val repository: CoinRepository = mockk()

    private val savedState = SavedStateHandle(mapOf("coinId" to "bitcoin"))

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `reads the coin id from the route and loads that coin`() = runTest {
        val btc = coin(id = "bitcoin")
        every { repository.observeCoin("bitcoin") } returns flowOf(btc)

        val viewModel = CoinDetailViewModel(repository, savedState)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(btc, state.coin)
        assertFalse(state.isLoading)
        assertFalse(state.notFound)
        verify { repository.observeCoin("bitcoin") }
    }

    @Test
    fun `reports not found when the coin left the cache`() = runTest {
        every { repository.observeCoin("bitcoin") } returns flowOf(null)

        val viewModel = CoinDetailViewModel(repository, savedState)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.notFound)
        assertNull(state.coin)
        assertFalse(state.isLoading)
    }

    @Test
    fun `blocking error when a refresh fails with nothing cached`() = runTest {
        every { repository.observeCoin("bitcoin") } returns flowOf(null)
        coEvery { repository.refreshCoins() } throws IOException("offline")

        val viewModel = CoinDetailViewModel(repository, savedState)
        advanceUntilIdle()
        viewModel.refresh()
        advanceUntilIdle()

        assertEquals(
            "No internet connection. Check your network and try again.",
            viewModel.uiState.value.error,
        )
        assertNull(viewModel.uiState.value.refreshError)
    }

    @Test
    fun `snackbar error when a refresh fails but the coin is on screen`() = runTest {
        every { repository.observeCoin("bitcoin") } returns flowOf(coin())
        coEvery { repository.refreshCoins() } throws IOException("offline")

        val viewModel = CoinDetailViewModel(repository, savedState)
        advanceUntilIdle()
        viewModel.refresh()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(
            "No internet connection. Check your network and try again.",
            state.refreshError,
        )
        assertNull(state.error)
        assertEquals("bitcoin", state.coin?.id)
    }

    @Test
    fun `ignores a refresh while one is already running`() = runTest {
        every { repository.observeCoin("bitcoin") } returns flowOf(coin())
        coEvery { repository.refreshCoins() } returns Unit

        val viewModel = CoinDetailViewModel(repository, savedState)
        advanceUntilIdle()

        viewModel.refresh()
        viewModel.refresh()
        advanceUntilIdle()

        coVerify(exactly = 1) { repository.refreshCoins() }
    }
}
