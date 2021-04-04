package ru.meseen.dev.stock

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import ru.meseen.dev.stock.data.RepoStatus
import ru.meseen.dev.stock.data.Response
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

    companion object{
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val connectionObserver = ConnectionObserver(context = applicationContext)

        val statusBar = Snackbar.make(binding.container, "", Snackbar.LENGTH_INDEFINITE).apply {
            setAction("Hide") { this.dismiss() }
            setActionTextColor(view.context.getColorCompat(R.color.white))
        }

        connectionObserver.observe(this){ connectionState->
            if(connectionState is ConnectionState.Error.Network){
                if(!statusBar.isShown){
                    statusBar.apply {
                        setText(connectionState.error)
                        setBackgroundTint(view.context.getColorCompat(R.color.red_color))
                        setTextColor(view.context.getColorCompat(R.color.white))
                    }.show()
                }
            }else if(connectionState is ConnectionState.Connected.Network){
                if(statusBar.isShown) {
                    statusBar.apply {
                        setText(connectionState.status)
                        setBackgroundTint(view.context.getColorCompat(R.color.background_accent))
                        setTextColor(view.context.getColorCompat(R.color.white))
                    }.dismiss()
                }
            }
        }
        lifecycleScope.launchWhenCreated {
            repoStatus.getLoadingStatus().collectLatest { response ->
                if (response is Response.Error) {
                    Toast.makeText(applicationContext, response.error, Toast.LENGTH_LONG).show()
                }
            }
        }
        val host: NavHostFragment = supportFragmentManager
            .findFragmentById(R.id.container) as NavHostFragment? ?: return

    }
}