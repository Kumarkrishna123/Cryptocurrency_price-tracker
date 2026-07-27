package com.cryptocurrencyprice_tracker.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.cryptocurrencyprice_tracker.domain.Coin
import com.cryptocurrencyprice_tracker.ui.format.formatCompactUsd
import com.cryptocurrencyprice_tracker.ui.format.formatPercent
import com.cryptocurrencyprice_tracker.ui.format.formatPrice
import com.cryptocurrencyprice_tracker.ui.format.formatPriceOrDash

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinDetailScreen(
    onBack: () -> Unit,
    viewModel: CoinDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.refreshError) {
        val message = state.refreshError ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.onRefreshErrorShown()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.coin?.name ?: "Coin") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            val coin = state.coin
            val error = state.error
            when {
                state.isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                )

                error != null -> MessageState(
                    message = error,
                    onRetry = viewModel::refresh,
                    modifier = Modifier.align(Alignment.Center),
                )

                coin == null || state.notFound -> MessageState(
                    message = "This coin is no longer in the tracked list.",
                    onRetry = null,
                    modifier = Modifier.align(Alignment.Center),
                )

                else -> CoinDetailContent(coin)
            }
        }
    }
}

@Composable
private fun CoinDetailContent(coin: Coin) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = coin.imageUrl,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
            )
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(text = coin.name, style = MaterialTheme.typography.headlineSmall)
                Text(
                    text = coin.symbol,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = formatPrice(coin.price),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
        )
        val isUp = coin.change24h >= 0
        Text(
            text = "${formatPercent(coin.change24h)} (24h)",
            style = MaterialTheme.typography.bodyLarge,
            color = if (isUp) Color(0xFF12805C) else MaterialTheme.colorScheme.error,
        )

        Spacer(Modifier.height(24.dp))
        HorizontalDivider()

        StatRow("Market cap rank", coin.marketCapRank?.let { "#$it" } ?: "—")
        StatRow("Market cap", formatCompactUsd(coin.marketCap))
        StatRow("24h volume", formatCompactUsd(coin.totalVolume))
        StatRow("24h high", formatPriceOrDash(coin.high24h))
        StatRow("24h low", formatPriceOrDash(coin.low24h))
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
    }
    HorizontalDivider()
}

@Composable
private fun MessageState(
    message: String,
    onRetry: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(24.dp),
    ) {
        Text(
            text = message,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
        )
        if (onRetry != null) {
            TextButton(onClick = onRetry) { Text("Retry") }
        }
    }
}
