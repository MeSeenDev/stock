package ru.meseen.dev.stock.data

import android.util.Log
import androidx.annotation.StringRes
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import okhttp3.WebSocket
import org.json.JSONObject
import ru.meseen.dev.stock.R
import ru.meseen.dev.stock.data.db.daos.StockDao
import ru.meseen.dev.stock.data.db.entitys.*
import ru.meseen.dev.stock.data.network.NetworkApi
import ru.meseen.dev.stock.data.network.pojo.DataItem
import ru.meseen.dev.stock.data.network.service.FinnhubService
import javax.inject.Inject

class Repository @Inject constructor(
    private val stockDao: StockDao,
    private val networkApi: FinnhubService
) : StockMainRepo, SearchStockRepo, TradeStockRepo {

    companion object {
        const val TAG = "Repository"
    }

    private val coroutineExceptionHandler = CoroutineExceptionHandler { context, throwable ->
        throwable.message?.let { message ->
            _loadingStatus.error(message)
        }
        Log.wtf(
            TAG, "WTF: coroutineExceptionHandler ${throwable.message} " +
                    "${System.lineSeparator()} " +
                    throwable.stackTrace.contentToString()
        )
    }
    private val repositoryScope by lazy {
        CoroutineScope(SupervisorJob() + Dispatchers.IO + coroutineExceptionHandler)
    }

    private val _loadingStatus by lazy { MutableStateFlow<Response>(Response.Loading("Loading")) }

    private fun MutableStateFlow<Response>.error(message: String) {
        value = Response.Error(message)
    }

    private fun MutableStateFlow<Response>.loading(message: String) {
        value = Response.Loading(message)
    }

    private fun MutableStateFlow<Response>.success(message: String) {
        value = Response.Success(message)
    }

    override fun getLoadingStatus(): StateFlow<Response> = _loadingStatus

    override fun getWatchStock(): Flow<List<StockMainEntity>> =
        stockDao.readWatchMain()

    override fun getFavoriteStock(): Flow<List<StockMainEntity>> =
        stockDao.readFavoriteMain()

    override fun switchStockList(stockMainEntity: StockMainEntity) {
        repositoryScope.launch {
            val switchedStockEntity = StockMainEntity(stockMainEntity, !stockMainEntity.isFavorite)
            stockDao.insert(switchedStockEntity)
        }
    }

    override fun refreshStock() {
        repositoryScope.launch {
            var count = 0L
            _loadingStatus.loading("Start refreshing")
            stockDao.readAllStock().forEach { stock ->
                val response = networkApi.getQuote(stock.symbol)
                val newStock =
                    StockMainEntity(
                        quote = response,
                        symbol = stock.symbol,
                        description = stock.description,
                        id = stock._id,
                        favorite = stock.isFavorite
                    )
                stockDao.insert(newStock)
                count++
            }
            _loadingStatus.success("Refreshed $count fields updated")
        }
    }

    override fun deleteStock(stockId: Long) {
        repositoryScope.launch {
            val itemDeleted = stockDao.deleteStockById(stockId)
            _loadingStatus.success("$itemDeleted item successfully deleted")
        }
    }

    override fun clearStockTable() {
        repositoryScope.launch {
            val itemDeleted = stockDao.clearStockMain()
            _loadingStatus.success("$itemDeleted item successfully deleted")
        }
    }


    override fun addNewStock(searchItem: SearchItem, isFavorite: Boolean): LongArray {
        _loadingStatus.loading("Start Searching")
        var quantity: LongArray = longArrayOf(-1)
        repositoryScope.launch {
            val quote = searchItem.symbol?.let {
                return@let if (isSymbolExists(it)) {
                    networkApi.getQuote(it)
                } else {
                    _loadingStatus.error("Stock Already Exists")
                    return@launch
                }
            }
            quote?.let {
                val stockMainEntity = StockMainEntity(it, searchItem, isFavorite)
                quantity = stockDao.insert(stockMainEntity)
            }
            _loadingStatus.success("New Stock added")
        }
        return quantity
    }

    private suspend fun isSymbolExists(symbol: String): Boolean =
        !stockDao.readAllStock().map { it.symbol }.contains(symbol)


    private val _listSearchItems by lazy { MutableStateFlow(listOf<SearchItem>()) }

    override fun getResultsList(): StateFlow<List<SearchItem>> =
        _listSearchItems

    override fun getCurrentStocks(): Flow<List<StockMainEntity>> = flow {
        stockDao.readAllStock()
    }

    override fun search(query: String) {
        _loadingStatus.loading("Start Searching")
        repositoryScope.launch {
            val searchResult = networkApi.search(query)
            val searchItems = searchResult.resultSearch
                ?.filterNotNull()
                ?.map { result ->
                    SearchItem(result)
                }
            searchItems?.let { items ->
                _listSearchItems.value = items
                addSearchedWord(query)
                _loadingStatus.success("New Stock added")
            }
        }
    }

    private fun CoroutineScope.addSearchedWord(query: String) {
        launch {
            val curSearchedWords = stockDao.readAllSearchedWords()
            if (curSearchedWords.size > 19) {
                stockDao.deleteSearchedWord(curSearchedWords.first())
            }
            if (curSearchedWords.find { it.word == query } == null) {
                stockDao.insert(SearchedWordEntity(word = query))
            }
        }
    }

    override fun lastSearchedWords(): Flow<List<SearchedWordEntity>> =
        stockDao.readSearchedWords()

    private val _candles by lazy { MutableStateFlow(StockCandles()) }

    override fun getCandles(): StateFlow<StockCandles> = _candles


    override suspend fun requeryCandles(
        symbol: String,
        timeFrame: TimeFrame,
        timePeriod: TimePeriod
    ) {
        _loadingStatus.loading("Start Loading Candles")
        try {
            val currentTime: Long = System.currentTimeMillis() / 1000
            val from = currentTime - timePeriod.timestamp
            val candleResponse =
                networkApi.getSymbolCandles(symbol, timeFrame.resolution, from, currentTime)
            _loadingStatus.loading("Start Loading in  _candles ${candleResponse.status_response}")
            _candles.value = StockCandles(candleResponse)
            if (candleResponse.status_response == "no_data") {
                _loadingStatus.error("Have no candles for period")
            } else {
                _loadingStatus.success("New Stock is added ${candleResponse.status_response}")
            }
        } catch (thr: Throwable) {
            _loadingStatus.error("New Stock is not added:reason-> $thr")
        }
    }

    override fun clearCandles() {
        _candles.value = StockCandles()
    }


    private val _tradeWebSocketResponse by lazy { MutableStateFlow(TradeSocketData()) }
    override fun getTradeSocketData(): StateFlow<TradeSocketData> = _tradeWebSocketResponse

    private var tradeWebSocket: WebSocket? = null

    override fun startWebSocket() {
        tradeWebSocket = NetworkApi.tradeWebSocket()
    }

    override suspend fun sendWebSocket(symbol: String) {
        val symbolSocketRequest = JSONObject()
        symbolSocketRequest.put("type", "subscribe")
        symbolSocketRequest.put("symbol", symbol)

        tradeWebSocket?.send(symbolSocketRequest.toString())
        NetworkApi.tradeWebSocketListener.tradeStateFlow.collectLatest { result ->
            if (result.isSuccess) {
                _tradeWebSocketResponse.value =
                    result.getOrNull()?.data?.foldToTrade() ?: TradeSocketData()
                _loadingStatus.success("WebSocket receive Success")
            } else {
                _loadingStatus.error("Error with webSocket:  ${result.exceptionOrNull()}")
            }
        }
    }

    private fun List<DataItem?>.foldToTrade(): TradeSocketData {
        val symbol = get(0)?.symbol ?: ""
        val price = (filterNotNull()
            .map { it.price ?: 0.0 }
            .fold(0.0) { oz, os -> oz + os }) / size

        val time = get(this.lastIndex)?.time ?: 0L
        return TradeSocketData(symbol = symbol, price = price, time = time)
    }


    override fun stopSocket() {
        tradeWebSocket?.close(1001, "Accepted close")
    }


}


sealed class Response {
    data class Success(val success: String) : Response()
    data class Loading(val loading: String) : Response()
    data class Error(val error: String) : Response()
}

interface RepoStatus {
    fun getLoadingStatus(): StateFlow<Response>

}

interface StockMainRepo : RepoStatus {
    fun getWatchStock(): Flow<List<StockMainEntity>>
    fun getFavoriteStock(): Flow<List<StockMainEntity>>
    fun switchStockList(stockMainEntity: StockMainEntity)
    fun refreshStock()
    fun deleteStock(stockId: Long)
    fun clearStockTable()
}

interface SearchStockRepo : RepoStatus {
    fun addNewStock(searchItem: SearchItem, isFavorite: Boolean = false): LongArray
    fun getResultsList(): StateFlow<List<SearchItem>>
    fun getCurrentStocks(): Flow<List<StockMainEntity>>
    fun search(query: String)
    fun lastSearchedWords(): Flow<List<SearchedWordEntity>>
}

interface TradeStockRepo : RepoStatus {
    fun getCandles(): StateFlow<StockCandles>
    suspend fun requeryCandles(
        symbol: String,
        timeFrame: TimeFrame,
        timePeriod: TimePeriod
    )

    fun clearCandles()
    fun startWebSocket()
    suspend fun sendWebSocket(symbol: String)
    fun getTradeSocketData(): StateFlow<TradeSocketData>
    fun stopSocket()
}


enum class TimePeriod(val timestamp: Long, @StringRes stringResName: Int) {
    DAY(86400L, R.string.day), MONTH(2592000, R.string.month), YEAR(31536000, R.string.year)
}

enum class TimeFrame(val resolution: String) {
    ONE_M("1"), FIVE_M("5"), FIFTEEN_M("15"), THIRTY_M("30"), HOUR("60"), DAY("D"), WEEK("W"), MONTH(
        "M"
    )
}
