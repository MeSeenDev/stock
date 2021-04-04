package ru.meseen.dev.stock.data.network

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import ru.meseen.dev.stock.data.network.pojo.TradesResponse

class TradeWebSockets(private val json: Json) : WebSocketListener() {

    companion object {
        const val TAG = "WebSokets"
    }

    val tradeStateFlow = MutableStateFlow(Result.success(TradesResponse()))

    override fun onOpen(webSocket: WebSocket, response: Response) {
        Log.d(TAG, "onOpen: $response")
        Log.d(TAG, "onOpen: ${webSocket.request()}")
        Log.d(TAG, "onOpen:handshake ${response.handshake}")
        Log.d(TAG, "onOpen:message ${response.message}")
        Log.d(TAG, "onOpen:headers ${response.headers}")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        Log.d(TAG, "onMessage: $text")
        tradeStateFlow.value = try{
            val obj = json.parseToJsonElement(text)
            val trade = json.decodeFromJsonElement<TradesResponse>(obj)
            Result.success(trade)
        } catch (ser: SerializationException){
            Result.failure(ser)
        }


    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Log.d(TAG, "onFailure:localizedMessage ${t.localizedMessage}")
        Log.d(TAG, "onFailure:headers ${response?.headers}")
        Log.d(TAG, "onFailure:body ${response?.body}")
        Log.d(TAG, "onFailure: ${response?.body}")
        tradeStateFlow.value = Result.failure(t)
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        Log.d(TAG, "onClosed: $code , $reason }")
    }
}

