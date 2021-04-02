package ru.meseen.dev.stock.ui.details

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.CandleData
import com.github.mikephil.charting.data.CandleDataSet
import com.github.mikephil.charting.data.CandleEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.transition.MaterialContainerTransform
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import ru.meseen.dev.stock.R
import ru.meseen.dev.stock.data.TimeFrame
import ru.meseen.dev.stock.data.TimePeriod
import ru.meseen.dev.stock.data.db.entitys.StockCandles
import ru.meseen.dev.stock.data.db.entitys.StockMainEntity
import ru.meseen.dev.stock.databinding.TradeFragmentBinding
import ru.meseen.dev.stock.ui.utils.getColorCompat
import ru.meseen.dev.stock.ui.utils.getStockTextColor
import ru.meseen.dev.stock.ui.utils.round
import ru.meseen.dev.stock.ui.utils.subtractPrices
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
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
        initCandlesLoading(savedInstanceState)
        setupToolbar(view)
        stokeEntity?.let { setTextStock(it) }
        candlesSetup(view)

        val candles = mutableListOf<CandleEntry>()
        lifecycleScope.launchWhenCreated {
            viewModel.curCandles
                .collectLatest { stockCandles ->
                    bindCandles(stockCandles, candles, view)
                }
        }


    }

    private fun setupToolbar(view: View) {
        vb.toolbarNavHome.setOnClickListener {
            Navigation.findNavController(view).popBackStack()
        }
        stokeEntity?.description?.let { vb.toolbarTrade.title = it }
    }

    private fun initCandlesLoading(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            stokeEntity?.symbol?.let { symbol ->
                viewModel.requeryCandles(symbol, TimeFrame.FIFTEEN_M, TimePeriod.DAY)
            }
        }
    }

    private fun candlesSetup(view: View) {
        vb.candleStickChart.apply {
            isHighlightPerDragEnabled = true
            setDrawBorders(false)
            requestDisallowInterceptTouchEvent(true)
            description.isEnabled = false
        }
        vb.candleStickChart.xAxis.apply {
            setDrawLabels(true)
            labelCount = 4
            granularity = 1F
            position = XAxis.XAxisPosition.BOTTOM
            textColor = view.context.getColorCompat(R.color.white)
            setDrawAxisLine(false)
            setDrawGridLines(false)// disable x axis grid lines
            setAvoidFirstLastClipping(true)
        }
        vb.candleStickChart.axisLeft.apply {
            setDrawLabels(false)
            setDrawAxisLine(false)
            setDrawGridLines(false)
            isGranularityEnabled = true
            granularity = 1f

        }
        vb.candleStickChart.axisRight.apply {
            setDrawGridLines(false)
            setDrawAxisLine(false)
            textColor = Color.WHITE
        }
        vb.candleStickChart.legend.apply {
            isEnabled = false
        }


    }


    private fun bindCandles(
        stockCandles: StockCandles,
        candles: MutableList<CandleEntry>,
        view: View
    ) {
        val openPrice = stockCandles.open_prices
        val closePrice = stockCandles.close_prices
        val highPrices = stockCandles.high_prices
        val lowPrices = stockCandles.low_prices
        val timestamp = stockCandles.timestamp
        if (isNullOrEmpty(openPrice, closePrice, highPrices, lowPrices, timestamp)
        ) {
            for (i in openPrice!!.indices) {
                candles.add(
                    CandleEntry(
                        i.toFloat(), highPrices!![i]!!,
                        lowPrices!![i]!!,
                        openPrice[i]!!,
                        closePrice!![i]!!
                    )
                )
            }
            submitDataSet(candles, view, timestamp)
        }
    }

    private fun submitDataSet(
        candles: MutableList<CandleEntry>,
        view: View,
        timestamp: List<Long?>?
    ) {
        val dataSet = getCandleDataSet(candles, view)
        val candleData = CandleData(dataSet)
        vb.candleStickChart.xAxis.apply {
            valueFormatter = timeValueFormatter(timestamp)
        }
        vb.candleStickChart.data = candleData
        vb.candleStickChart.invalidate()
    }

    private val locale = Locale.getDefault()
    private fun timeValueFormatter(
        timestamp: List<Long?>?
    ) = object : ValueFormatter() {
        private val simpleDateFormat = SimpleDateFormat("H:mm EEE", locale)

        override fun getFormattedValue(value: Float): String {
            return formatInput(timestamp!![value.toInt()])
        }

        private fun formatInput(value: Long?): String {
            return if (value != null) simpleDateFormat.format(Date(value.secondToMillis())) else ""
        }

        private fun Long.secondToMillis(): Long = this.times(1000)

    }

    private fun getCandleDataSet(
        candles: MutableList<CandleEntry>,
        view: View
    ) = CandleDataSet(candles, " Temps").apply {
        color = view.context.getColorCompat(R.color.white)
        shadowColor = view.context.getColorCompat(R.color.background_accent)
        decreasingColor = view.context.getColorCompat(R.color.red_color)
        increasingColor = view.context.getColorCompat(R.color.green_color)
        neutralColor = view.context.getColorCompat(R.color.colorAccent)
        shadowWidth = 1f
        decreasingPaintStyle = Paint.Style.FILL
        increasingPaintStyle = Paint.Style.FILL
        setDrawValues(false)
    }


    private fun isNullOrEmpty(
        openPrice: List<Float?>?,
        closePrice: List<Float?>?,
        highPrices: List<Float?>?,
        lowPrices: List<Float?>?,
        timestamp: List<Long?>?
    ) = (!openPrice.isNullOrEmpty()
            && !closePrice.isNullOrEmpty()
            && !highPrices.isNullOrEmpty()
            && !lowPrices.isNullOrEmpty() && !timestamp.isNullOrEmpty())

    private fun setTextStock(stoke: StockMainEntity) {
        vb.tvStockSymbol.text = stoke.symbol
        vb.toolbarTradeTitle.text = stoke.description
        if (stoke.current_price != null && stoke.prev_close_price != null) {
            setTextCurPrices(stoke.current_price, stoke.prev_close_price)
        }
    }

    private fun setTextCurPrices(
        current_price: Double,
        prev_close_price: Double
    ) {
        vb.tvDescription.text = current_price.toString()
        val diffPrice = "${
            current_price
                .subtractPrices(prev_close_price)
                .round(2)
        } %"
        vb.tvDiffStock.text = diffPrice
        vb.tvDiffStock.setTextColor(
            requireContext()
                .getStockTextColor(prev_close_price > current_price)
        )
    }

    private fun setTransition() {
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.container
            duration = resources.getInteger(R.integer.def_motion_duration).toLong()
            scrimColor = Color.TRANSPARENT
        }
    }

}
