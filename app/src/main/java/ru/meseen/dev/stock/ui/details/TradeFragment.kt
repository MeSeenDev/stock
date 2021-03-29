package ru.meseen.dev.stock.ui.details

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.transition.MaterialContainerTransform
import ru.meseen.dev.stock.R
import ru.meseen.dev.stock.databinding.TradeFragmentBinding
import ru.meseen.dev.stock.ui.utils.getStockTextColor
import ru.meseen.dev.stock.ui.utils.round
import ru.meseen.dev.stock.ui.utils.subtractPrices

class TradeFragment : Fragment(R.layout.trade_fragment) {

    companion object {
        const val SYMBOL_TRADE = "SYMBOL_TRADE"
        private const val TAG = "TradeFragment"
    }

    private val args by navArgs<TradeFragmentArgs>()
    private val stokeEntity by lazy { args.stock }

    private val vb by viewBinding(TradeFragmentBinding::bind, R.id.trade_parent)

    private val viewModel by viewModels<TradeViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ViewCompat.setTransitionName(view, getString(R.string.trade_fragment_transition_name))
        setTransition()
        Log.d(TAG, "onViewCreated: $stokeEntity")
        stokeEntity?.let{ stoke ->
            vb.tvStockSymbol.text = stoke.symbol
            vb.tvDescription.text = stoke.description

            if(stoke.prev_close_price != null && stoke.current_price!= null){
                val temp = "${stoke.current_price
                    ?.subtractPrices(stoke.prev_close_price!!)
                    ?.round(2)
                    .toString()} %"
                vb.tvDiffStock.text = temp
                vb.tvDiffStock.setTextColor(view.context
                    .getStockTextColor(stoke.prev_close_price!! > stoke.current_price!!))
            }

        }
    }

    private fun setTransition() {
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.container
            duration = resources.getInteger(R.integer.def_motion_duration).toLong()
            scrimColor = Color.TRANSPARENT
        }
    }
}