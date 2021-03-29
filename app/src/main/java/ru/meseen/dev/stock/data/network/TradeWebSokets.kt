package ru.meseen.dev.stock.data.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import ru.meseen.dev.stock.data.network.pojo.TradesResponse

class TradeWebSokets(val json: Json) : WebSocketListener() {

    companion object {
        const val TAG = "WebSokets"
    }

    val flows = MutableSharedFlow<Result<TradesResponse>>()

    override fun onOpen(webSocket: WebSocket, response: Response) {
        Log.d(TAG, "onOpen: $response")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        Log.d(TAG, "onMessage: $text")
        GlobalScope.launch {
            withContext(Dispatchers.Default) {
                val obj = json.parseToJsonElement(text)
                val trade = json.decodeFromJsonElement<TradesResponse>(obj)
                flows.emit(Result.success(trade))
            }
        }


    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Log.d(TAG, "onFailure: $response")
        Log.d(TAG, "onFailure: ${response?.body}")
        GlobalScope.launch {
            flows.emit(Result.failure(t))
        }
    }
}

