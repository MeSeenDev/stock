package ru.meseen.dev.stock.ui.main.vp2.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import ru.meseen.dev.stock.ui.main.vp2.FavoriteListFragment
import ru.meseen.dev.stock.ui.main.vp2.WatchListFragment

class ViewPager2Adapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> WatchListFragment.getInstance()
            else -> FavoriteListFragment.getInstance()
        }
    }
}