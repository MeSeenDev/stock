package ru.meseen.dev.stock.ui.main

import android.os.Bundle
import android.view.View
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.transition.MaterialSharedAxis
import dagger.hilt.android.AndroidEntryPoint
import ru.meseen.dev.stock.R
import ru.meseen.dev.stock.databinding.MainFragmentBinding
import ru.meseen.dev.stock.ui.main.vp2.adapters.ViewPager2Adapter

@AndroidEntryPoint
class MainFragment : Fragment(R.layout.main_fragment) {

    companion object {
        const val TAG = "MainFragment"
    }

    /*
    private lateinit var ws: WebSocket

    lifecycleScope.launchWhenCreated {
         launch(Dispatchers.IO + coroutineExceptionHandler) {
             try {
                 ApiClient.apiKey["token"] = "sandbox_c19p7f748v6obcihhqd0"
                 val req =
                     Request.Builder().url("wss://ws.finnhub.io?token=c19p7f748v6obcihhqcg")
                         .build()
                 val listenerWeb = TradeWebSokets()
                 ws = ApiClient.client.newWebSocket(req, listenerWeb)
                 val apiClient = DefaultApi()

                 val previos = apiClient.quote("YNDX")


                 val jsonObject = JSONObject()
                 jsonObject.put("type", "subscribe")
                 jsonObject.put("symbol", "AAPL")
                 val jsonObject2 = JSONObject()
                 jsonObject2.put("type", "subscribe")
                 jsonObject2.put("symbol", "YNDX")
                 ws.send(jsonObject.toString())
                 ws.send(jsonObject2.toString())

                 launch(Dispatchers.IO) {
                     listenerWeb.flows.collectLatest { text ->
                         if (text.isSuccess) {
                             text.getOrNull()?.let {
                                 if (it.type != null && it.type == "trade") {
                                     val previosPrice = previos.pc
                                     val curPrice = it.data?.get(0)?.price
                                     withContext(Dispatchers.Main) {
                                         val text = "${it.data?.get(0)?.symbol.toString()} " +
                                                 "- $${it.data?.get(0)?.price} " +
                                                 "- ${it.data?.get(0)?.volume.toString()}\n" +
                                                 "-= ${
                                                     (previosPrice?.let { prev ->
                                                         curPrice?.let { cur ->
                                                             "%.${3}f".format(
                                                                 (cur - prev)
                                                             )
                                                         }
                                                     })
                                                 } =-"
                                       //  binding.message.text = text
                                     }
                                 }
                             }
                         }
                     }
                 }
             } catch (thr: Throwable) {
                 Log.wtf("ERROR", " $coroutineContext : ${thr.localizedMessage}")
             }

         }
     }*/


    private val vb by viewBinding(MainFragmentBinding::bind, R.id.main)

    private val viewModel: MainViewModel by viewModels()



    //{"type":"subscribe","symbol":"AAPL"}
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
        vb.vpStocks.adapter = ViewPager2Adapter(this)
        configTabs()

        vb.mainFab.setOnClickListener {
            host?.apply {
                exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true).apply {
                    duration = resources.getInteger(R.integer.def_motion_duration).toLong()
                }
                reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false).apply {
                    duration = resources.getInteger(R.integer.def_motion_duration).toLong()
                }
            }
            findNavController().navigate(R.id.searchFragment)
        }
    }

    private fun configTabs() {
        TabLayoutMediator(vb.tabs, vb.vpStocks) { tab, position ->
            when (position) {
                0 -> tab.text = "Watchlist"
                else -> tab.text = "Favorites"
            }
        }.attach()
    }


}

