package com.cryptocurrencyprice_tracker.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cryptocurrencyprice_tracker.data.CoinRepository
import com.cryptocurrencyprice_tracker.domain.Coin
import com.cryptocurrencyprice_tracker.ui.coins.toUserMessage
import com.cryptocurrencyprice_tracker.ui.navigation.CoinDetailRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CoinDetailUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val coin: Coin? = null,
    val error: String? = null,
    val refreshError: String? = null,
    val notFound: Boolean = false,
)

@HiltViewModel
class CoinDetailViewModel @Inject constructor(
    private val repository: CoinRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val coinId: String = checkNotNull(
        savedStateHandle[CoinDetailRoute::coinId.name]
    ) { "CoinDetailScreen requires a coinId argument" }

    private val _uiState = MutableStateFlow(CoinDetailUiState())
    val uiState: StateFlow<CoinDetailUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        observeCoin()
    }

    private fun observeCoin() {
        viewModelScope.launch {
            repository.observeCoin(coinId).collect { coin ->
                _uiState.update {
                    it.copy(
                        coin = coin,
                        isLoading = false,
                        notFound = coin == null,
                    )
                }
            }
        }
    }

    fun refresh() {
        if (loadJob?.isActive == true) return

        loadJob = viewModelScope.launch {
            val hasData = _uiState.value.coin != null
            _uiState.update { it.copy(isRefreshing = true, error = null) }
            try {
                repository.refreshCoins()
                _uiState.update { it.copy(isRefreshing = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val message = e.toUserMessage()
                _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        error = if (hasData) null else message,
                        refreshError = if (hasData) message else null,
                    )
                }
            }
        }
    }

    fun onRefreshErrorShown() {
        _uiState.update { it.copy(refreshError = null) }
    }
}
