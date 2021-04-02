package ru.meseen.dev.stock.data.network.pojo

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @see <a href="https://finnhub.io/docs/api/stock-candles>Stock Candles</a>
 */
@Serializable
data class StockCandlesResponse(

    @SerialName("c")
    val close_prices: List<Float?>? = null,

    @SerialName("s")
    val status_response: String? = null,

    @SerialName("t")
    val timestamp: List<Long?>? = null,

    @SerialName("v")
    val volume_datas: List<Long?>? = null,

    @SerialName("h")
    val high_prices: List<Float?>? = null,

    @SerialName("l")
    val low_prices: List<Float?>? = null,

    @SerialName("o")
    val open_prices: List<Float?>? = null
)
