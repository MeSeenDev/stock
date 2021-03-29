package ru.meseen.dev.stock.data.db.daos

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.meseen.dev.stock.data.db.entitys.SearchedWordEntity
import ru.meseen.dev.stock.data.db.entitys.StockMainEntity

@Dao
interface StockDao {

    @Query("SELECT * FROM TABLE_STOCK_MAIN WHERE is_favorite LIKE :isFavorite")
    fun readStockMain(isFavorite: Boolean): Flow<List<StockMainEntity>>

    @Query("SELECT * FROM TABLE_STOCK_MAIN WHERE is_favorite LIKE 1")
    fun readFavoriteMain(): Flow<List<StockMainEntity>>

    @Query("SELECT * FROM TABLE_STOCK_MAIN WHERE is_favorite LIKE 0")
    fun readWatchMain(): Flow<List<StockMainEntity>>

    @Query("SELECT * FROM TABLE_STOCK_MAIN")
    fun readStockMain(): Flow<List<StockMainEntity>>

    @Query("SELECT * FROM TABLE_STOCK_MAIN")
    fun readAllStock(): List<StockMainEntity>

    @Query("SELECT * FROM TABLE_SEARCHED_WORDS")
    fun readSearchedWords(): Flow<List<SearchedWordEntity>>

    @Query("SELECT * FROM TABLE_SEARCHED_WORDS")
    fun readAllSearchedWords(): List<SearchedWordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg stockMain: StockMainEntity): LongArray

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(word: SearchedWordEntity): Long

    @Delete
    suspend fun deleteSearchedWord(word: SearchedWordEntity): Int

    @Query("DELETE FROM  TABLE_SEARCHED_WORDS WHERE _id LIKE :id")
    suspend fun deleteSearchedWordById(id: Long): Int

    @Delete
    suspend fun deleteStock(stockMain: StockMainEntity): Int

    @Query("DELETE FROM TABLE_STOCK_MAIN WHERE _id LIKE :id")
    suspend fun deleteStockById(id: Long): Int

    @Query("DELETE FROM TABLE_STOCK_MAIN")
    suspend fun clearStockMain()

}