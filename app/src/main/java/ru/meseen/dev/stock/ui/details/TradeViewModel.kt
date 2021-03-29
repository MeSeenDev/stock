package ru.meseen.dev.stock.ui.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import ru.meseen.dev.stock.data.TradeStockRepo
import ru.meseen.dev.stock.data.db.entitys.StockCandles
import javax.inject.Inject

@HiltViewModel
class TradeViewModel @Inject constructor(
    private val repository: TradeStockRepo,
    private val handle: SavedStateHandle
) : ViewModel() {


    val loadingStatus = repository.getLoadingStatus()
    val candlesCharts = MutableStateFlow<StockCandles>(StockCandles())

    fun getCandlesArrays(
        symbol: String,
        resolution: String,
        from: Long,
        to: Long
    ) = repository.getCandlesArrays(symbol, resolution, from, to)




}