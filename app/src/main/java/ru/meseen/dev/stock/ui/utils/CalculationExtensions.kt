package ru.meseen.dev.stock.ui.utils

import java.math.BigDecimal
import java.math.RoundingMode


/**
 * this is current price
 */
fun Double.subtractPrices(
    prev_close_price: Double
) = 100 * ((this - prev_close_price) / prev_close_price)


/**
 * @param scale Number of digits after decimal point
 */
fun Double.round(scale: Int): Double = try {
    BigDecimal(this.toString()).setScale(scale, RoundingMode.HALF_EVEN).toDouble()
} catch (nfe: NumberFormatException) {
    0.00
}
