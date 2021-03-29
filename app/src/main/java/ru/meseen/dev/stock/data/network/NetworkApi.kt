package ru.meseen.dev.stock.data.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import ru.meseen.dev.stock.data.network.service.FinnhubService
import java.util.concurrent.TimeUnit

object NetworkApi {

    private const val WSS_URL = "wss://ws.finnhub.io?token=c19p7f748v6obcihhqcg"
    private const val TOKEN = "sandbox_c19p7f748v6obcihhqd0"
    private const val BASE_URL = "https://finnhub.io/api/v1/"
    private const val API_KEY_HEADER = "X-Finnhub-Token"

    private val json by lazy {
        Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
    }

    private val contentType = "application/json".toMediaType()

    private val converter = json.asConverterFactory(contentType)

    private val authenticator =
        Authenticator { route, response ->
            response.request.newBuilder().header(
                API_KEY_HEADER,
                TOKEN
            ).build()
        }

    private val okHttpClient =
        OkHttpClient().newBuilder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .authenticator(authenticator)
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build()


    private val retrofit: Retrofit = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl(BASE_URL)
        .addConverterFactory(converter)
        .build()

    val finnhubService = retrofit.create(FinnhubService::class.java)

    val tradeWebSocketListener = TradeWebSokets(json)
    object TradeWebSocket : WebSocket by okHttpClient.newWebSocket(
        Request.Builder()
        .url(WSS_URL)
        .build(),tradeWebSocketListener){

    }


}