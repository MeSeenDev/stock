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

