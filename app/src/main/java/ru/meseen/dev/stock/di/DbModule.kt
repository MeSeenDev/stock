package ru.meseen.dev.stock.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.meseen.dev.stock.data.db.RoomDataStorage
import ru.meseen.dev.stock.data.db.RoomDataStorage.Companion.STOCK_TABLE_NAME
import ru.meseen.dev.stock.data.db.daos.StockDao
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DbModule {

    @Provides
    @Singleton
    fun provideAppDb(
        @ApplicationContext context: Context
    ): RoomDataStorage  = Room
            .databaseBuilder(
                context,
                RoomDataStorage::class.java,
                STOCK_TABLE_NAME
            )
            .fallbackToDestructiveMigration()
            .build()


    @Provides
    @Singleton
    fun providesArticlesDao(
        db: RoomDataStorage
    ): StockDao =
        db.stockDao()


}