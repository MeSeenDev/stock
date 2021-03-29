package ru.meseen.dev.stock.data.db.entitys

import android.provider.BaseColumns
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.meseen.dev.stock.data.db.RoomDataStorage.Companion.TABLE_SEARCHED_WORDS

@Entity(tableName = TABLE_SEARCHED_WORDS)
data class SearchedWordEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = BaseColumns._ID)
    val _id:Long? = null,
    @ColumnInfo(name = "word")
    val word: String
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SearchedWordEntity

        if (word != other.word) return false

        return true
    }

    override fun hashCode(): Int {
        return word.hashCode()
    }
}