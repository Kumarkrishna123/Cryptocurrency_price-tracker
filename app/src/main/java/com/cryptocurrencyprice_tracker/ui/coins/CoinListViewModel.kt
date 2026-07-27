package com.cryptocurrencyprice_tracker.ui.coins

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cryptocurrencyprice_tracker.data.CoinRepository
import com.cryptocurrencyprice_tracker.domain.Coin
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class CoinSort(val label: String) {
    MarketCap("Market cap"),
    PriceDesc("Price"),
    ChangeDesc("24h change"),
}

data class CoinListUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val query: String = "",
    val sort: CoinSort = CoinSort.MarketCap,
    val error: String? = null,
    val refreshError: String? = null,
    val allCoins: List<Coin> = emptyList(),
) {
    val visibleCoins: List<Coin>
        get() {
            val filtered = if (query.isBlank()) allCoins else allCoins.filter { coin ->
                coin.name.contains(query, ignoreCase = true) ||
                    coin.symbol.contains(query, ignoreCase = true)
            }
            return when (sort) {
                CoinSort.MarketCap -> filtered
                CoinSort.PriceDesc -> filtered.sortedByDescending { it.price }
                CoinSort.ChangeDesc -> filtered.sortedByDescending { it.change24h }
            }
        }

    val isEmptySearchResult: Boolean
        get() = visibleCoins.isEmpty() && allCoins.isNotEmpty()
}

@HiltViewModel
class CoinListViewModel @Inject constructor(
    private val repository: CoinRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CoinListUiState())
    val uiState: StateFlow<CoinListUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        observeCache()
        refresh()
    }

    private fun observeCache() {
        viewModelScope.launch {
            repository.observeCoins().collect { coins ->
                _uiState.update { state ->
                    state.copy(
                        allCoins = coins,
                        isLoading = if (coins.isNotEmpty()) false else state.isLoading,
                        error = if (coins.isNotEmpty()) null else state.error,
                    )
                }
            }
        }
    }

    fun refresh(isPullToRefresh: Boolean = false) {
        if (loadJob?.isActive == true) return

        loadJob = viewModelScope.launch {
            val hasCachedRows = _uiState.value.allCoins.isNotEmpty()
            _uiState.update {
                it.copy(
                    isLoading = !hasCachedRows && !isPullToRefresh,
                    isRefreshing = isPullToRefresh,
                    error = null,
                )
            }
            try {
                repository.refreshCoins()
                _uiState.update { it.copy(isLoading = false, isRefreshing = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val message = e.toUserMessage()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = if (hasCachedRows) null else message,
                        refreshError = if (hasCachedRows) message else null,
                    )
                }
            }
        }
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun onSortChange(sort: CoinSort) {
        _uiState.update { it.copy(sort = sort) }
    }

    fun onRefreshErrorShown() {
        _uiState.update { it.copy(refreshError = null) }
    }
}
