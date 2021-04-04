package ru.meseen.dev.stock.data.db.entitys

import ru.meseen.dev.stock.data.network.pojo.DataItem


data class TradeSocketData(
    val price: Double? = null,
    val symbol: String? = null,
    val time: Long? = null
) {
    constructor(trade: DataItem?) : this(
        price = trade?.price,
        symbol = trade?.symbol,
        time = trade?.time
    )
}