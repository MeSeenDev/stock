package ru.meseen.dev.stock.ui.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
            newQuery("", TimeFrame.DAY, TimePeriod.MONTH)
        }
    }

    val loadingStatus = repository.getLoadingStatus()

    val tradeWebSocket = repository.getTradeSocketData()

    val curCandles = repository.getCandles()

    val candleFilter = handle.getLiveData<TimePeriod>(CANDLE_TIME_PERIOD)

    fun startSocket() = repository.startWebSocket()

    fun sendWebSocket(symbol: String) {
        viewModelScope.launch(Dispatchers.Default) {
            repository.sendWebSocket(symbol = symbol)
        }
    }

    fun requeryCandles(
        symbol: String,
        timeFrame: TimeFrame,
        timePeriod: TimePeriod
    ) {
        if (isNewQuery(symbol, timeFrame, timePeriod)) {
            newQuery(symbol, timeFrame, timePeriod)
            viewModelScope.launch(Dispatchers.IO) {
                repository.requeryCandles(symbol, timeFrame, timePeriod)
            }
        }
    }

    private fun newQuery(
        symbol: String,
        timeFrame: TimeFrame,
        timePeriod: TimePeriod
    ) {
        handle.set(CANDLE_SYMBOL, symbol)
        handle.set(CANDLE_RESOLUTION, timeFrame)
        handle.set(CANDLE_TIME_PERIOD, timePeriod)
    }

    private fun isNewQuery(
        symbol: String,
        timeFrame: TimeFrame,
        timePeriod: TimePeriod
    ): Boolean = (handle.get<String>(CANDLE_SYMBOL) != symbol ||
            handle.get<TimeFrame>(CANDLE_RESOLUTION) != timeFrame ||
            handle.get<TimePeriod>(CANDLE_TIME_PERIOD) != timePeriod
            )


    fun stopSocket() = repository.stopSocket()

    override fun onCleared() {
        repository.clearCandles()
        super.onCleared()
    }
}