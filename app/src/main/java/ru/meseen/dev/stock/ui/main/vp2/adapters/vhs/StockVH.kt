package ru.meseen.dev.stock.ui.main.vp2.adapters.vhs

import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.meseen.dev.stock.R
import ru.meseen.dev.stock.data.db.entitys.StockMainEntity
import ru.meseen.dev.stock.databinding.StockItemBinding
import ru.meseen.dev.stock.ui.main.vp2.adapters.OnLongClickStockItem
import ru.meseen.dev.stock.ui.main.vp2.adapters.OnStockClickItem
import ru.meseen.dev.stock.ui.utils.getCardBackGroundColor
import ru.meseen.dev.stock.ui.utils.getStockTextColor
import ru.meseen.dev.stock.ui.utils.round
import ru.meseen.dev.stock.ui.utils.subtractPrices
import java.math.BigDecimal
import java.math.RoundingMode

class StockVH(
    private val view: View,
    private val onClick: OnStockClickItem,
    private val onLongClick: OnLongClickStockItem
) : RecyclerView.ViewHolder(view), View.OnClickListener, View.OnLongClickListener {

    private val vb by viewBinding(StockItemBinding::bind)

    private lateinit var stockItem: StockMainEntity

    fun bind(item: StockMainEntity) {
        stockItem = item
        ViewCompat.setTransitionName(view, "stock_item_${stockItem._id}")
        setClickListeners()
        vb.tvSymbol.text = item.description
        val formatText = "${item.current_price?.round(2)} $"
        vb.tvPrice.text = formatText
        setTextDiff(item)
        setBackgroundColor()
    }

    private fun setBackgroundColor() {

        adapterPosition.let { id ->
            vb.stockItemRoot.setBackgroundColor(
                vb.root.context.getCardBackGroundColor((id % 2) == 0))
        }
    }

    private fun setClickListeners() {
        view.setOnClickListener(this)
        view.setOnLongClickListener(this)
    }

    private fun setTextDiff(item: StockMainEntity) {
        if (item.prev_close_price != null && item.current_price != null) {
            val diffPrice = item.current_price.subtractPrices(item.prev_close_price)
            val temp =
                "${diffPrice.round(2)} %"
            vb.tvDiff.text = temp
            setDiffTextColor(item.prev_close_price, item.current_price)
        }
    }


    private fun setDiffTextColor(prev_close_price: Double, current_price: Double) {
        vb.tvDiff.setTextColor(
            vb.root.context.getStockTextColor(prev_close_price > current_price)
        )
    }

    override fun onClick(v: View?) {
        v?.let { onClick.stockClick(stockItem, it) }
    }

    override fun onLongClick(v: View?): Boolean {
        v?.let {
            onLongClick.stockLongClick(stockItem, it)
            return true
        }
        return false
    }

}

