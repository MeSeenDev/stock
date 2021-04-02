package ru.meseen.dev.stock.ui.main

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ru.meseen.dev.stock.data.Repository
import ru.meseen.dev.stock.data.StockMainRepo
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: StockMainRepo,
    private val handle: SavedStateHandle
) : ViewModel()

