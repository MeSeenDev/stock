package ru.meseen.dev.stock.data.db.entitys

import android.os.Parcel
import android.os.Parcelable
import android.provider.BaseColumns
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.meseen.dev.stock.data.db.RoomDataStorage.Companion.TABLE_STOCK_MAIN
import ru.meseen.dev.stock.data.network.pojo.QuoteResponse

@Entity(tableName = TABLE_STOCK_MAIN)
data class StockMainEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = BaseColumns._ID)
    val _id: Long? = null,
    @ColumnInfo(name = "symbol")
    val symbol: String,
    @ColumnInfo(name = "description")
    val description: String? = null,
    @ColumnInfo(name = "open_price")
    val open_price: Double?,
    @ColumnInfo(name = "high_price")
    val high_price: Double?,
    @ColumnInfo(name = "low_price")
    val low_price: Double?,
    @ColumnInfo(name = "current_price")
    val current_price: Double?,
    @ColumnInfo(name = "prev_close_price")
    val prev_close_price: Double?,
    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean,
    @ColumnInfo(name = "timestamp")
    val timestamp: Long
) :Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readValue(Long::class.java.classLoader) as? Long,
        parcel.readString().toString(),
        parcel.readString(),
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readByte() != 0.toByte(),
        parcel.readLong()
    )

    constructor(
        quote: QuoteResponse,
        symbol: String,
        description: String?,
        favorite: Boolean = false,
        id: Long? = null
    ) : this(
        _id = id,
        symbol = symbol,
        description = description,
        open_price = quote.open_price,
        high_price = quote.high_price,
        low_price = quote.low_price,
        current_price = quote.current_price,
        prev_close_price = quote.pc,
        isFavorite = favorite,
        timestamp = System.currentTimeMillis()
    )

    constructor(
        stockMainEntity: StockMainEntity,
        favorite: Boolean = stockMainEntity.isFavorite
    ) : this(

        _id = stockMainEntity._id,
        symbol = stockMainEntity.symbol,
        description = stockMainEntity.description,
        open_price = stockMainEntity.open_price,
        high_price = stockMainEntity.high_price,
        low_price = stockMainEntity.low_price,
        current_price = stockMainEntity.current_price,
        prev_close_price = stockMainEntity.prev_close_price,
        isFavorite = favorite,
        timestamp = stockMainEntity.timestamp
    )

    constructor(
        quote: QuoteResponse,
        searchItem: SearchItem,
        favorite: Boolean = false,
        id: Long? = null
    ) : this(
        _id = id,
        symbol = searchItem.symbol!!,
        description = searchItem.description,
        open_price = quote.open_price,
        high_price = quote.high_price,
        low_price = quote.low_price,
        current_price = quote.current_price,
        prev_close_price = quote.pc,
        isFavorite = favorite,
        timestamp = System.currentTimeMillis()
    )


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StockMainEntity

        if (symbol != other.symbol) return false
        if (description != other.description) return false
        if (open_price != other.open_price) return false
        if (high_price != other.high_price) return false
        if (low_price != other.low_price) return false
        if (current_price != other.current_price) return false
        if (prev_close_price != other.prev_close_price) return false
        if (timestamp != other.timestamp) return false

        return true
    }

    override fun hashCode(): Int {
        var result = symbol.hashCode()
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (open_price?.toString().hashCode())
        result = 31 * result + (high_price?.toString().hashCode())
        result = 31 * result + (low_price?.toString().hashCode())
        result = 31 * result + (current_price?.toString().hashCode())
        result = 31 * result + (prev_close_price?.toString().hashCode())
        result = 31 * result + timestamp.hashCode()
        return result
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(_id)
        parcel.writeString(symbol)
        parcel.writeString(description)
        parcel.writeValue(open_price)
        parcel.writeValue(high_price)
        parcel.writeValue(low_price)
        parcel.writeValue(current_price)
        parcel.writeValue(prev_close_price)
        parcel.writeByte(if (isFavorite) 1 else 0)
        parcel.writeLong(timestamp)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<StockMainEntity> {
        override fun createFromParcel(parcel: Parcel): StockMainEntity {
            return StockMainEntity(parcel)
        }

        override fun newArray(size: Int): Array<StockMainEntity?> {
            return arrayOfNulls(size)
        }
    }


}