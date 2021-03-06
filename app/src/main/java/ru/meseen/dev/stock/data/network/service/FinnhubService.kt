package ru.meseen.dev.stock.data.network.service

import retrofit2.http.GET
import retrofit2.http.Query
import ru.meseen.dev.stock.data.network.pojo.CompanyProfileResponse
import ru.meseen.dev.stock.data.network.pojo.QuoteResponse
import ru.meseen.dev.stock.data.network.pojo.SearchResponse
import ru.meseen.dev.stock.data.network.pojo.StockCandlesResponse

interface FinnhubService {

    @GET("quote")
    suspend fun getQuote(
        @Query(value = "symbol") symbol: String
    ): QuoteResponse

    @GET("search")
    suspend fun search(
        @Query(value = "q") query: String
    ): SearchResponse

    @GET("stock/profile2")
    suspend fun getCompanyProfile(
        @Query(value = "symbol") symbol: String,
        @Query(value = "isin") isin: String,
        @Query(value = "cusip") cusip: String
    ): CompanyProfileResponse

    @GET("stock/candle")
    suspend fun getSymbolCandles(
        @Query(value = "symbol") symbol: String,
        @Query(value = "resolution") resolution: String,
        @Query(value = "from") from: Long,
        @Query(value = "to") to: Long,
    ): StockCandlesResponse


}