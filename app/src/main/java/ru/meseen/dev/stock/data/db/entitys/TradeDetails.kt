package ru.meseen.dev.stock.data.db.entitys

import androidx.room.Entity
import kotlinx.serialization.SerialName
import ru.meseen.dev.stock.data.db.RoomDataStorage.Companion.TABLE_TRADE_DETAILS


class TradeDetails(
    val current_price: Double? = null,

    val pc: Double? = null,

    val timestamp: Long? = null,

    val high_price: Double? = null,

    val low_price: Double? = null,

    val open_price: Double? = null
)