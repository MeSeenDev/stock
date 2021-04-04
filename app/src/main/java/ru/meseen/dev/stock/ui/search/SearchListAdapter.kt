package ru.meseen.dev.stock.ui.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.meseen.dev.stock.R
import ru.meseen.dev.stock.data.db.entitys.SearchItem
import ru.meseen.dev.stock.databinding.SearchItemBinding

class SearchListAdapter(private val listener: OnSearchItemClick) :
    ListAdapter<SearchItem, RecyclerView.ViewHolder>(DIFF_MATCHER) {

    companion object {
        private val DIFF_MATCHER = object : DiffUtil.ItemCallback<SearchItem>() {
            override fun areItemsTheSame(oldItem: SearchItem, newItem: SearchItem): Boolean =
                oldItem == newItem

            override fun getChangePayload(oldItem: SearchItem, newItem: SearchItem): Any? = Any()

            override fun areContentsTheSame(oldItem: SearchItem, newItem: SearchItem): Boolean =
                oldItem.description == newItem.description && oldItem.symbol == newItem.symbol

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val searchItemView =
            LayoutInflater.from(parent.context).inflate(R.layout.search_item, parent, false)
        return SearchItemVH(searchItemView, listener)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is SearchItemVH) {
            holder.bind(getItem(position))
        }
    }
}

class SearchItemVH(private val view: View, private val listener: OnSearchItemClick) :
    RecyclerView.ViewHolder(view) {

    private val vb by viewBinding(SearchItemBinding::bind, R.id.search_fragment_item_parent)

    fun bind(searchItem: SearchItem) {
        view.setOnClickListener { listener.itemClick(searchItem ,view) }
        vb.tvDescription.text = searchItem.description
        vb.tvDisplaySymbol.text = searchItem.displaySymbol
        vb.tvType.text = searchItem.type
        backgroundColorize()
    }

    private fun backgroundColorize() {
        if (adapterPosition >= 0 && adapterPosition % 2 == 0) {
            vb.mtrlCardCheckedLayerId.setBackgroundResource(R.color.background)
        } else {
            vb.mtrlCardCheckedLayerId.setBackgroundResource(R.color.background_item_accent)
        }
    }

}

interface OnSearchItemClick {
    fun itemClick(item: SearchItem,view: View)
}