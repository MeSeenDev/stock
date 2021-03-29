package ru.meseen.dev.stock.data.network.pojo

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @see <a href="https://finnhub.io/docs/api/symbol-search">symbol-search</a>
 */
@Serializable
data class SearchResponse(

    @SerialName("result")
    val resultSearch: List<ResultSearchItem?>? = null,

    @SerialName("count")
    val count: Int? = null
)

@Serializable
data class ResultSearchItem(

    @SerialName("displaySymbol")
    val displaySymbol: String? = null,

    @SerialName("symbol")
    val symbol: String? = null,

    @SerialName("description")
    val description: String? = null,

    @SerialName("type")
    val type: String? = null
)
