package ru.meseen.dev.stock.ui.details

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.github.mikephil.charting.charts.CandleStickChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.CandleData
import com.github.mikephil.charting.data.CandleDataSet
import com.github.mikephil.charting.data.CandleEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.transition.MaterialContainerTransform
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
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
import kotlin.math.absoluteValue

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


    override fun onCreate(savedInstanceState: Bundle?) {
        stokeEntity?.let {
            viewModel.startSocket()
            viewModel.sendWebSocket(it.symbol)
        }
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ViewCompat.setTransitionName(view, getString(R.string.trade_fragment_transition_name))
        setTransition()
        initCandlesLoading(savedInstanceState)
        setupToolbar(view)
        stokeEntity?.let { setTextStock(it) }
        val candlesStick: CandlesStick = Candles(vb.candleStickChart)
        observeCandles(candlesStick)
        observeWebSocket()

        viewModel.candleFilter.observe(viewLifecycleOwner) { timePeriod ->
            timePeriod?.let {
                when (it) {
                    TimePeriod.DAY -> {
                        if (vb.chipDay.isChecked) return@observe else vb.chipDay.isChecked = true
                    }
                    TimePeriod.MONTH -> {
                        if (vb.chipMonth.isChecked) return@observe else vb.chipMonth.isChecked =
                            true
                    }
                    TimePeriod.YEAR -> {
                        if (vb.chipYear.isChecked) return@observe else vb.chipYear.isChecked = true
                    }
                }
            }
        }
        vb.chipDay.setOnClickListener(chipClickListener)
        vb.chipMonth.setOnClickListener(chipClickListener)
        vb.chipYear.setOnClickListener(chipClickListener)

    }

    private val chipClickListener = View.OnClickListener { view ->
        when (view.id) {
            R.id.chip_day -> {
                stokeEntity?.symbol?.let {
                    viewModel.requeryCandles(it, TimeFrame.FIFTEEN_M, TimePeriod.DAY)
                }
            }
            R.id.chip_month -> {
                stokeEntity?.symbol?.let {
                    viewModel.requeryCandles(it, TimeFrame.DAY, TimePeriod.MONTH)
                }
            }
            R.id.chip_year -> {
                stokeEntity?.symbol?.let {
                    viewModel.requeryCandles(it, TimeFrame.DAY, TimePeriod.YEAR)
                }
            }
        }
    }

    private fun observeCandles(candlesStick: CandlesStick) {
        lifecycleScope.launchWhenCreated {
            viewModel.curCandles
                .collectLatest { stockCandles ->
                    candlesStick.bindCandles(stockCandles)
                }
        }
    }

    private fun observeWebSocket() {
        lifecycleScope.launch {
            viewModel.tradeWebSocket.collectLatest { trade ->
                if (trade.price != null && stokeEntity?.current_price != null) {
                    setPriceText(trade.price)
                    setDiffTextPrices(
                        current_price = trade.price,
                        prev_close_price = stokeEntity!!.prev_close_price!!
                    )
                }
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

    private fun setTextStock(stoke: StockMainEntity) {
        vb.tvStockSymbol.text = stoke.symbol
        vb.toolbarTradeTitle.text = stoke.description
        if (stoke.current_price != null && stoke.prev_close_price != null) {
            setPriceText(stoke.current_price)
            setDiffTextPrices(stoke.current_price, stoke.prev_close_price)
        }
    }

    private fun setDiffTextPrices(
        current_price: Double,
        prev_close_price: Double
    ) {
        val diffPrice = "${
            current_price
                .subtractPrices(prev_close_price)
                .round(2)
                .absoluteValue
        } %"
        vb.tvDiffStock.text = diffPrice
        val colorDiff = requireContext()
            .getStockTextColor(prev_close_price > current_price)
        vb.tvDiffStock.setTextColor(colorDiff)
        setupArrowDiffPrice(colorDiff, prev_close_price, current_price)
    }

    private fun setPriceText(current_price: Double) {
        val curPrice = "${current_price.round(2)} $"
        vb.tvDescription.text = curPrice
    }

    private fun setupArrowDiffPrice(
        colorDiff: Int,
        prev_close_price: Double,
        current_price: Double
    ) {
        val drawable =
            ResourcesCompat.getDrawable(
                requireContext().resources,
                R.drawable.ic_round_arrow,
                null
            )
        drawable?.setTint(colorDiff)
        val arrowDirection = if (prev_close_price > current_price) 0f else 180f
        vb.ivArrow.animate().apply {
            rotation(arrowDirection)
            duration = 300L
        }.start()
        vb.ivArrow.setImageDrawable(drawable)
        vb.ivArrow.setColorFilter(colorDiff)
    }

    private fun setTransition() {
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.container
            duration = resources.getInteger(R.integer.def_motion_duration).toLong()
            scrimColor = Color.TRANSPARENT
        }
    }

    override fun onPause() {
        viewModel.stopSocket()
        super.onPause()
    }
}

private class Candles(val candleView: CandleStickChart) : CandlesStick {
    private val context = candleView.context

    init {
        candleView.apply {
            isHighlightPerDragEnabled = true
            setDrawBorders(false)
            requestDisallowInterceptTouchEvent(true)
            description.isEnabled = false
        }
        candleView.xAxis.apply {
            setDrawLabels(true)
            labelCount = 4
            granularity = 1F
            position = XAxis.XAxisPosition.BOTTOM
            textColor = context.getColorCompat(R.color.white)
            setDrawAxisLine(false)
            setDrawGridLines(false)// disable x axis grid lines
            setAvoidFirstLastClipping(true)
        }
        candleView.axisLeft.apply {
            setDrawLabels(false)
            setDrawAxisLine(false)
            setDrawGridLines(false)
            isGranularityEnabled = true
            granularity = 1f

        }
        candleView.axisRight.apply {
            setDrawGridLines(false)
            setDrawAxisLine(false)
            textColor = Color.WHITE
        }
        candleView.legend.apply {
            isEnabled = false
        }

    }

    private val locale = Locale.getDefault()
    val simpleDateFormat = SimpleDateFormat("H:mm EEE dd", locale)
    private fun timeValueFormatter(
        timestamp: List<Long?>?
    ) = object : ValueFormatter() {

        override fun getFormattedValue(value: Float): String {
            return try {
                formatInput(timestamp!![value.toInt()])
            } catch (ia: IndexOutOfBoundsException) {
                ""
            }
        }

        private fun formatInput(value: Long?): String {
            return if (value != null) simpleDateFormat.format(Date(value.secondToMillis())) else ""
        }

        private fun Long.secondToMillis(): Long = this.times(1000)

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

    override fun bindCandles(stockCandles: StockCandles) {
        val candles = mutableListOf<CandleEntry>()
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
                        i.toFloat(),
                        highPrices!![i]!!,
                        lowPrices!![i]!!,
                        openPrice[i]!!,
                        closePrice!![i]!!
                    )
                )
            }
            submitDataSet(candles, timestamp)
        }
    }

    private fun submitDataSet(
        candles: MutableList<CandleEntry>,
        timestamp: List<Long?>?
    ) {
        val dataSet = getCandleDataSet(candles)
        val candleData = CandleData(dataSet)
        candleView.xAxis.apply {
            valueFormatter = timeValueFormatter(timestamp)
        }
        candleView.data = candleData
        candleView.invalidate()
    }

    private fun getCandleDataSet(
        candles: MutableList<CandleEntry>
    ) =
        CandleDataSet(candles, " Temps").apply {
            color = context.getColorCompat(R.color.white)
            shadowColor = context.getColorCompat(R.color.background_accent)
            decreasingColor = context.getColorCompat(R.color.red_color)
            increasingColor = context.getColorCompat(R.color.green_color)
            neutralColor = context.getColorCompat(R.color.colorAccent)
            shadowWidth = 1f
            decreasingPaintStyle = Paint.Style.FILL
            increasingPaintStyle = Paint.Style.FILL
            valueTextColor = context.getColorCompat(R.color.white)
            setDrawValues(true)
        }


}

interface CandlesStick {
    fun bindCandles(stockCandles: StockCandles)
}
