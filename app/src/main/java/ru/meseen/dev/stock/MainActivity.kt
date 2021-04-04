package ru.meseen.dev.stock

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.meseen.dev.stock.data.RepoStatus
import ru.meseen.dev.stock.data.Response
import ru.meseen.dev.stock.data.SearchStockRepo
import ru.meseen.dev.stock.data.db.entitys.SearchItem
import ru.meseen.dev.stock.data.network.ConnectionObserver
import ru.meseen.dev.stock.data.network.ConnectionState
import ru.meseen.dev.stock.databinding.MainActivityBinding
import ru.meseen.dev.stock.ui.utils.getColorCompat
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(R.layout.main_activity) {

    private val binding by viewBinding(MainActivityBinding::bind, R.id.container)

    @Inject
    lateinit var repoStatus: RepoStatus

    companion object {
        private const val TAG = "MainActivity"
        private const val PREFS_KEY = "PREFS_KEY"
        private const val FIRST_START_PREFS_KEY = "FIRST_START_PREFS_KEY"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val connectionObserver = ConnectionObserver(context = applicationContext)
        networkObserve(connectionObserver)
        val preferences = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
            val isFrstTime = preferences.getBoolean(FIRST_START_PREFS_KEY,true)
            if (isFrstTime){
                initialSymbols()
                preferences.edit().apply {
                    putBoolean(FIRST_START_PREFS_KEY, false)
                }.apply()
            }
        repoStatusObserve()
        val host: NavHostFragment = supportFragmentManager
            .findFragmentById(R.id.container) as NavHostFragment? ?: return

    }

    private fun initialSymbols() {
        val cheat = repoStatus as SearchStockRepo
        val pairInitSymbols = listOf(
            "AAPL" to ("APPLE INC" to false),
            "YNDX" to ("YANDEX NV-A" to true),
            "MSFT" to ("MICROSOFT CORP" to false),
            "MVIS" to ("MICROVISION INC" to false),
            "UPWK" to ("UPWORK INC" to false),
            "SABR" to ("SABRE CORP" to false),
            "AMZN" to ("AMAZON.COM INC" to true),
            "GOOG" to ("ALPHABET INC-CL C" to false),
            "FB" to ("FACEBOOK INC-CLASS A" to false),
            "BABA" to ("ALIBABA GROUP HOLDING-SP ADR" to false),
            "WMT" to ("WALMART INC" to false),
            "MA" to ("MASTERCARD INC - A" to false),
            "NKE" to ("NIKE INC -CL B" to false),
            "KO" to ("COCA-COLA CO/THE" to false),
            "PEP" to ("PEPSICO INC" to false),
            "ORCL" to ("ORACLE CORP" to true),
            "AVGO" to ("BROADCOM INC" to false),
            "QCOM" to ("QUALCOMM INC" to true),
            "MS" to ("MORGAN STANLEY" to false),
            "IBM" to ("INTL BUSINESS MACHINES CORP" to false),
            "TSLA" to ("TESLA INC" to true)
        )
        lifecycleScope.launch {
            pairInitSymbols.forEach { pair ->
                delay(300)
                cheat.addNewStock(
                    SearchItem(
                        symbol = pair.first,
                        description = pair.second.first
                    ), pair.second.second
                )
            }
        }

    }

    private fun networkObserve(connectionObserver: ConnectionObserver) {
        val statusBar = Snackbar.make(binding.container, "", Snackbar.LENGTH_INDEFINITE).apply {
            setAction("Hide") { this.dismiss() }
            setActionTextColor(view.context.getColorCompat(R.color.white))
        }

        connectionObserver.observe(this) { connectionState ->
            if (connectionState is ConnectionState.Error.Network) {
                if (!statusBar.isShown) {
                    statusBar.apply {
                        setText(connectionState.error)
                        setBackgroundTint(view.context.getColorCompat(R.color.red_color))
                        setTextColor(view.context.getColorCompat(R.color.white))
                    }.show()
                }
            } else if (connectionState is ConnectionState.Connected.Network) {
                if (statusBar.isShown) {
                    statusBar.apply {
                        setText(connectionState.status)
                        setBackgroundTint(view.context.getColorCompat(R.color.background_accent))
                        setTextColor(view.context.getColorCompat(R.color.white))
                    }.dismiss()
                }
            }
        }
    }

    private fun repoStatusObserve() {
        lifecycleScope.launchWhenCreated {
            repoStatus.getLoadingStatus().collectLatest { response ->
                if (response is Response.Error) {
                    Toast.makeText(binding.container.context, response.error, Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    }
}