package ru.meseen.dev.stock.ui.main.vp2.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ru.meseen.dev.stock.data.StockMainRepo
import ru.meseen.dev.stock.data.db.entitys.StockMainEntity
import javax.inject.Inject

@HiltViewModel
class WatchListViewModel @Inject constructor(
    private val repository: StockMainRepo,
    private val handle: SavedStateHandle
) : ViewModel() {

    fun loadingStatus() = repository.getLoadingStatus()

    fun getStockList() = repository.getWatchStock()

    fun invalidateStockList() = repository.refreshStock()

    fun deleteStock(stockId: Long) = repository.deleteStock(stockId)

    fun switchStockList(stockMainEntity: StockMainEntity) = repository.switchStockList(stockMainEntity)

}