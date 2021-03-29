package ru.meseen.dev.stock.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.meseen.dev.stock.data.Repository
import ru.meseen.dev.stock.data.SearchStockRepo
import ru.meseen.dev.stock.data.StockMainRepo
import ru.meseen.dev.stock.data.TradeStockRepo
import ru.meseen.dev.stock.data.db.daos.StockDao
import ru.meseen.dev.stock.data.network.service.FinnhubService
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class RepoModule {

    @Provides
    @Singleton
    fun providesRepository(sd: StockDao, nw: FinnhubService): Repository = Repository(sd, nw)

    @Provides
    @Singleton
    fun providesStockRepository(repository: Repository): StockMainRepo = repository

    @Provides
    @Singleton
    fun providesSearchRepository(repository: Repository): SearchStockRepo = repository

    @Provides
    @Singleton
    fun providesTradeRepository(repository: Repository): TradeStockRepo = repository

}