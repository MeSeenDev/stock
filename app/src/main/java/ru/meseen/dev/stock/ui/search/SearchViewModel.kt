package ru.meseen.dev.stock.ui.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import ru.meseen.dev.stock.data.SearchStockRepo
import ru.meseen.dev.stock.data.db.entitys.SearchItem
import ru.meseen.dev.stock.data.db.entitys.StockMainEntity
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: SearchStockRepo,
    private val handle: SavedStateHandle
) : ViewModel() {



    val resultsList = repository.getResultsList()

    val statusLoad = repository.getLoadingStatus()

    fun addNewStock(searchItem: SearchItem,isFavorite: Boolean): LongArray =
        repository.addNewStock(searchItem,isFavorite)


    fun getCurrentStocks(): Flow<List<StockMainEntity>> = repository.getCurrentStocks()

    fun search(query: String) = repository.search(query)

    fun lastSearchedWords()  = repository.lastSearchedWords()


}