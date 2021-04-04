package ru.meseen.dev.stock.ui.main.vp2

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import ru.meseen.dev.stock.R
import ru.meseen.dev.stock.data.Response
import ru.meseen.dev.stock.data.db.entitys.StockMainEntity
import ru.meseen.dev.stock.databinding.ListStockBinding
import ru.meseen.dev.stock.ui.details.TradeFragment
import ru.meseen.dev.stock.ui.main.MainFragmentDirections
import ru.meseen.dev.stock.ui.main.vp2.adapters.OnLongClickStockItem
import ru.meseen.dev.stock.ui.main.vp2.adapters.OnStockClickItem
import ru.meseen.dev.stock.ui.main.vp2.adapters.StockListAdapter
import ru.meseen.dev.stock.ui.main.vp2.viewmodels.WatchListViewModel

@AndroidEntryPoint
class WatchListFragment : Fragment(R.layout.list_stock), OnStockClickItem, OnLongClickStockItem {

    private val viewModel by viewModels<WatchListViewModel>()

    private val vb by viewBinding(ListStockBinding::bind, R.id.parent)
    private val adapter = StockListAdapter(onClick = this, onLongClick = this)

    companion object {
        private const val TAG = "WatchListFragment"
        fun getInstance() = WatchListFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            viewModel.invalidateStockList()
        }
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ViewCompat.setTransitionName(view, getString(R.string.watrh_list_transition_name))

        vb.rvStockItem.adapter = adapter
        vb.rvStockItem.layoutManager = LinearLayoutManager(view.context)
        submitList()
        observeRepoStatus()
        swipeRefreshListener()
    }

    private fun swipeRefreshListener() {
        vb.swipeRefresh.setOnRefreshListener {
            viewModel.invalidateStockList()
        }
    }

    private fun submitList() {
        lifecycleScope.launchWhenCreated {
            viewModel.getStockList().collectLatest {
                adapter.submitList(it)
            }
        }
    }

    private fun observeRepoStatus() {
        lifecycleScope.launchWhenCreated {
            viewModel.loadingStatus().asLiveData().observe(viewLifecycleOwner) { result ->
                vb.swipeRefresh.isRefreshing = result is Response.Loading
            }
        }
    }

    override fun stockClick(stockMainEntity: StockMainEntity, view: View) {
        val symbol = Bundle()
        symbol.putString(TradeFragment.SYMBOL_TRADE, stockMainEntity.symbol)
        val extras =
            FragmentNavigatorExtras(view to getString(R.string.trade_fragment_transition_name))
        val directions = MainFragmentDirections.actionMainFragmentToTradeFragment(stockMainEntity)
        findNavController().navigate(directions, extras)
    }

    override fun stockLongClick(stockMainEntity: StockMainEntity, view: View) {
        val menu = PopupMenu(view.context, view)
        menu.inflate(R.menu.main_popup_menu)
        menu.setOnMenuItemClickListener {
            return@setOnMenuItemClickListener when (it.itemId) {
                R.id.addFavorites -> {
                    viewModel.switchStockList(stockMainEntity)
                    true
                }
                R.id.deleteStock -> {
                    stockMainEntity._id?.let { it1 -> viewModel.deleteStock(stockId = it1) }
                    true
                }
                else -> false
            }
        }
        menu.show()
    }
}