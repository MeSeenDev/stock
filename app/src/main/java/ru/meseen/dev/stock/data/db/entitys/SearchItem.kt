package ru.meseen.dev.stock.data.db.entitys

import ru.meseen.dev.stock.data.network.pojo.ResultSearchItem


data class SearchItem(
    val displaySymbol: String? = null,
    val symbol: String? = null,
    val description: String? = null,
    val type: String? = null
) {
    constructor(resultSearchItem: ResultSearchItem) : this(
        displaySymbol = resultSearchItem.displaySymbol,
        symbol = resultSearchItem.symbol,
        description = resultSearchItem.description,
        type = resultSearchItem.type
    )
}