package ru.meseen.dev.stock.data

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
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

    override fun getCandlesArrays(
        symbol: String,
        resolution: String,
        from: Long,
        to: Long
    ): Flow<StockCandles> = flow {
        _loadingStatus.value = Result.Loading("Start Searching")
        val candleResponse = networkApi.getSymbolCandles(symbol, resolution, from, to)
        emit(StockCandles(candleResponse))
        _loadingStatus.value = Result.Success("New Stock added")
    }.flowOn(Dispatchers.IO).catch { cause: Throwable ->
        cause.message?.let { _loadingStatus.value = Result.Error(it) }
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

    fun getCandlesArrays(
        symbol: String,
        resolution: String,
        from: Long,
        to: Long
    ): Flow<StockCandles>
}

