package ru.meseen.dev.stock.data

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import ru.meseen.dev.stock.data.db.daos.StockDao
import ru.meseen.dev.stock.data.db.entitys.SearchItem
import ru.meseen.dev.stock.data.db.entitys.SearchedWordEntity
import ru.meseen.dev.stock.data.db.entitys.StockCandles
import ru.meseen.dev.stock.data.db.entitys.StockMainEntity
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
            _loadingStatus.value = Result.Error(message)
        }
        Log.wtf(
            TAG, "WTF: coroutineExceptionHandler ${throwable.message} " +
                    "${System.lineSeparator()} " +
                    throwable.stackTrace.contentToString()
        )
    }
    private val repositoryScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO + coroutineExceptionHandler)

    private val _loadingStatus = MutableStateFlow<Result>(Result.Loading("Loading"))


    override fun getLoadingStatus(): StateFlow<Result> = _loadingStatus

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
            _loadingStatus.value = Result.Loading("Start refreshing ")
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
            _loadingStatus.value = Result.Success("Refreshed $count fields updated")
        }
    }

    override fun deleteStock(stockId: Long) {
        repositoryScope.launch {
            val itemDeleted = stockDao.deleteStockById(stockId)
            _loadingStatus.value = Result.Success("$itemDeleted item successfully deleted")
        }
    }

    override fun clearStockTable() {
        repositoryScope.launch {
            val itemDeleted = stockDao.clearStockMain()
            _loadingStatus.value = Result.Success("$itemDeleted item successfully deleted")
        }
    }


    override fun addNewStock(searchItem: SearchItem, isFavorite: Boolean): LongArray {
        _loadingStatus.value = Result.Loading("Start Searching")
        Log.d(TAG, "addNewStock: $searchItem")
        var quantity: LongArray = longArrayOf(-1)
        repositoryScope.launch {
            val quote = searchItem.symbol?.let { networkApi.getQuote(it) }
            quote?.let {
                val stockMainEntity = StockMainEntity(it, searchItem, isFavorite)
                quantity = stockDao.insert(stockMainEntity)
            }
            _loadingStatus.value = Result.Success("New Stock added")
        }
        return quantity
    }

    private val _listSearchItems = MutableStateFlow(listOf<SearchItem>())

    override fun getResultsList(): StateFlow<List<SearchItem>> =
        _listSearchItems

    override fun getCurrentStocks(): Flow<List<StockMainEntity>> = flow {
        stockDao.readAllStock()
    }

    override fun search(query: String) {
        _loadingStatus.value = Result.Loading("Start Searching")
        repositoryScope.launch {
            val searchResult = networkApi.search(query)
            val searchItems = searchResult.resultSearch
                ?.filterNotNull()
                ?.map { result ->
                    SearchItem(result)
                }
            searchItems?.let { items ->
                _listSearchItems.value = items
                _loadingStatus.value = Result.Success("New Stock added")
                addSearchedWord(query)
            }
        }
    }

    private fun CoroutineScope.addSearchedWord(query: String) {
        launch {
            val curSearchedWords = stockDao.readAllSearchedWords()
            if (curSearchedWords.size > 19) {
                stockDao.deleteSearchedWord(curSearchedWords.last())
            }
            if (curSearchedWords.find { it.word == query } == null) {
                stockDao.insert(SearchedWordEntity(word = query))
            }
        }
    }

    override fun lastSearchedWords(): Flow<List<SearchedWordEntity>> =
        stockDao.readSearchedWords()

    private val _candles = MutableStateFlow(StockCandles())

    override fun getCandles(): StateFlow<StockCandles> = _candles


    override fun requeryCandles(
        symbol: String,
        timeFrame: TimeFrame,
        timePeriod: TimePeriod
    ) {
        _loadingStatus.value = Result.Loading("Start Loading Candles")
        repositoryScope.launch {
            val currentTime: Long = System.currentTimeMillis() / 1000
            Log.d(TAG, "getCandlesArrays: $currentTime")

            val from = currentTime - timePeriod.timestamp
            Log.d(TAG, "requeryCandles: symbol: ${symbol}, resolution: ${timeFrame.resolution}, from: $from, currentTime: $currentTime}")
            val candleResponse = networkApi.getSymbolCandles(symbol, timeFrame.resolution, from, currentTime)
            Log.d(TAG, "getSymbolCandles: ${candleResponse.open_prices} ")
            Log.d(TAG, "getSymbolCandles: ${_candles.value.open_prices} ")
            _loadingStatus.value =
                Result.Loading("Post loading stat ${candleResponse.status_response}")
            _candles.value = StockCandles(candleResponse)
            _loadingStatus.value =
                Result.Success("New Stock added ${candleResponse.status_response}")
        }
    }

    override fun clearCandles() {
        _candles.value = StockCandles()
    }


}


sealed class Result {
    data class Success(val success: String) : Result()
    data class Loading(val loading: String) : Result()
    data class Error(val error: String) : Result()
}

interface RepoStatus {
    fun getLoadingStatus(): StateFlow<Result>

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

    fun requeryCandles(
        symbol: String,
        timeFrame: TimeFrame,
        timePeriod: TimePeriod
    )
    fun clearCandles()
}


enum class TimePeriod(val timestamp: Long) {
    DAY(86400L), MONTH(2592000), YEAR(31536000)
}

enum class TimeFrame(val resolution: String) {
    ONE_M("1"), FIVE_M("5"), FIFTEEN_M("15"), THIRTY_M("30")
    , HOUR("60"), DAY("D"), WEEK("W"), MONTH("M")
}
