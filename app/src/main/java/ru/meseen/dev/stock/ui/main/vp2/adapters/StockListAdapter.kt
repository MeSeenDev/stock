package ru.meseen.dev.stock.ui.main.vp2.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.meseen.dev.stock.R
import ru.meseen.dev.stock.data.db.entitys.StockMainEntity
import ru.meseen.dev.stock.ui.main.vp2.adapters.vhs.StockVH

class StockListAdapter(
    private val onClick: OnStockClickItem,
    private val onLongClick: OnLongClickStockItem
) : ListAdapter<StockMainEntity, RecyclerView.ViewHolder>(DIFF_STOCK_ITEM) {


    companion object {
        private const val TAG = "StockListAdapter"
        private val DIFF_STOCK_ITEM = object : DiffUtil.ItemCallback<StockMainEntity>() {
            override fun areItemsTheSame(
                oldItem: StockMainEntity,
                newItem: StockMainEntity
            ): Boolean = oldItem == newItem

            override fun areContentsTheSame(
                oldItem: StockMainEntity,
                newItem: StockMainEntity
            ): Boolean = newItem.symbol == oldItem.symbol
                    && newItem.timestamp == oldItem.timestamp

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val stockView =
            LayoutInflater.from(parent.context).inflate(R.layout.stock_item, parent, false)
        return StockVH(stockView, onClick, onLongClick)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is StockVH) {
            holder.bind(getItem(position))
        }
    }
}

interface OnStockClickItem {
    fun stockClick(stockMainEntity: StockMainEntity, view: View)
}

interface OnLongClickStockItem {
    fun stockLongClick(stockMainEntity: StockMainEntity,view : View)
}

