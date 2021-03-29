package ru.meseen.dev.stock.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.meseen.dev.stock.data.db.daos.StockDao
import ru.meseen.dev.stock.data.db.entitys.SearchedWordEntity
import ru.meseen.dev.stock.data.db.entitys.StockMainEntity

@Database(
    entities = [StockMainEntity::class,SearchedWordEntity::class],
    version = 1,
    exportSchema = false
)
abstract class RoomDataStorage : RoomDatabase() {

    abstract fun stockDao(): StockDao

    companion object {
        const val STOCK_TABLE_NAME = "STOCK.db"
        const val TABLE_STOCK_MAIN = "TABLE_STOCK_MAIN"
        const val TABLE_SEARCHED_WORDS = "TABLE_SEARCHED_WORDS"
        const val TABLE_TRADE_DETAILS = "TABLE_TRADE_DETAILS"

    }
}