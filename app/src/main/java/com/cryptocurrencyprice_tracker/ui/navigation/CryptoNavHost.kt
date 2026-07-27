package com.cryptocurrencyprice_tracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cryptocurrencyprice_tracker.ui.coins.CoinListScreen
import com.cryptocurrencyprice_tracker.ui.detail.CoinDetailScreen

@Composable
fun CryptoNavHost(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = CoinListRoute,
        modifier = modifier,
    ) {
        composable<CoinListRoute> {
            CoinListScreen(
                onCoinClick = { coinId -> navController.navigate(CoinDetailRoute(coinId)) },
            )
        }

        composable<CoinDetailRoute> {
            CoinDetailScreen(
                onBack = navController::popBackStack,
            )
        }
    }
}
