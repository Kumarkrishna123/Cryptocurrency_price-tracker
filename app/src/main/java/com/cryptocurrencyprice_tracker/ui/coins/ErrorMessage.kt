package com.cryptocurrencyprice_tracker.ui.coins

import retrofit2.HttpException
import java.io.IOException

fun Throwable.toUserMessage(): String = when {
    this is IOException -> "No internet connection. Check your network and try again."
    this is HttpException && code() == 429 ->
        "Too many requests right now. Wait a few seconds and retry."
    this is HttpException -> "The price service is having trouble (error ${code()})."
    else -> "Something went wrong. Please try again."
}
