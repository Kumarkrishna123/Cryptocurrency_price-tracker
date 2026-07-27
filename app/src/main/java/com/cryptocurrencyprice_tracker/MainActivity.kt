package com.cryptocurrencyprice_tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.cryptocurrencyprice_tracker.ui.navigation.CryptoNavHost
import com.cryptocurrencyprice_tracker.ui.theme.CryptocurrencyPricetrackerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CryptocurrencyPricetrackerTheme {
                CryptoNavHost()
            }
        }
    }
}
