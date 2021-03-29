package ru.meseen.dev.stock.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.meseen.dev.stock.data.network.NetworkApi
import ru.meseen.dev.stock.data.network.service.FinnhubService
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class NetworkModule {

    @Provides
    @Singleton
    fun providesNetworkApi(): FinnhubService = NetworkApi.finnhubService


}