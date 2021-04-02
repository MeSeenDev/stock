package ru.meseen.dev.stock.ui.utils

import android.content.Context
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.res.ResourcesCompat
import ru.meseen.dev.stock.R


@ColorInt
fun Context.getColorCompat(@ColorRes resId: Int): Int = ResourcesCompat.getColor(
    resources,
    resId,
    null
)

@ColorInt
fun Context.getStockTextColor(isPrevPriceBigger: Boolean): Int = ResourcesCompat.getColor(
    resources,
    if (isPrevPriceBigger)
        R.color.red_color
    else R.color.green_color,
    null
)


@ColorInt
fun Context.getCardBackGroundColor(isViewEven: Boolean): Int =
    ResourcesCompat.getColor(
        resources,
        if (isViewEven) R.color.background_item_accent else R.color.background,
        null
    )
