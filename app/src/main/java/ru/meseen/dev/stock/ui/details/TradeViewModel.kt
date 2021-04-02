package ru.meseen.dev.stock.ui.details

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ru.meseen.dev.stock.data.TimeFrame
import ru.meseen.dev.stock.data.TimePeriod
import ru.meseen.dev.stock.data.TradeStockRepo
import javax.inject.Inject

@HiltViewModel
class TradeViewModel @Inject constructor(
    private val repository: TradeStockRepo,
    private val handle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val TAG = "TradeViewModel"
        const val CANDLE_SYMBOL = "SYMBOL_SEARCH"
        const val CANDLE_RESOLUTION = "CANDLE_RESOLUTION"
        const val CANDLE_TIME_PERIOD = "CANDLE_TIME_TERIOD"
    }

    init {
        if (!handle.contains(CANDLE_SYMBOL)) {
            handle.set(CANDLE_SYMBOL, "")
            handle.set(CANDLE_RESOLUTION, TimeFrame.FIFTEEN_M)
            handle.set(CANDLE_TIME_PERIOD, TimePeriod.DAY)
        }
    }

    val loadingStatus = repository.getLoadingStatus()

    val curCandles = repository.getCandles()

    fun startSocket(){

    }

    fun requeryCandles(
        symbol: String,
        timeFrame: TimeFrame,
        timePeriod: TimePeriod
    ) {
        if (isNewQuery(symbol, timeFrame, timePeriod)) {
            repository.requeryCandles(symbol, timeFrame, timePeriod)
        }
    }

    private fun isNewQuery(
        symbol: String,
        timeFrame: TimeFrame,
        timePeriod: TimePeriod
    ): Boolean = (handle.get<String>(CANDLE_SYMBOL) != symbol ||
            handle.get<TimeFrame>(CANDLE_RESOLUTION) != timeFrame ||
            handle.get<TimePeriod>(CANDLE_TIME_PERIOD) != timePeriod
            )


    override fun onCleared() {
        repository.clearCandles()
        super.onCleared()
    }
}