package ru.meseen.dev.stock.data.network.pojo

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @see <a href="https://finnhub.io/docs/api/quote">Quote</a>
 */
@Serializable
data class QuoteResponse(

    @SerialName("c")
    val current_price: Double? = null,

    @SerialName("pc")
    val pc: Double? = null,

    @SerialName("t")
    val timestamp: Long? = null,

    @SerialName("h")
    val high_price: Double? = null,

    @SerialName("l")
    val low_price: Double? = null,

    @SerialName("o")
    val open_price: Double? = null
)

