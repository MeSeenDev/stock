package ru.meseen.dev.stock.data.db.entitys

import ru.meseen.dev.stock.data.network.pojo.StockCandlesResponse

data class StockCandles(
    val close_prices: List<Double?>? = null,

    val status_response: String? = null,

    val timestamp: List<Int?>? = null,

    val volume_datas: List<Int?>? = null,

    val high_prices: List<Double?>? = null,

    val low_prices: List<Double?>? = null,

    val open_prices: List<Double?>? = null
) {
    constructor(traStockCandles: StockCandlesResponse) : this(
        close_prices = traStockCandles.close_prices,
        status_response = traStockCandles.status_response,
        timestamp = traStockCandles.timestamp,
        volume_datas = traStockCandles.volume_datas,
        high_prices = traStockCandles.high_prices,
        low_prices = traStockCandles.low_prices,
        open_prices = traStockCandles.open_prices
    )

}