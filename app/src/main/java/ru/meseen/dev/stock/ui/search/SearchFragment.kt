package ru.meseen.dev.stock.ui.search

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.SearchView
import androidx.collection.arrayMapOf
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.get
import androidx.core.view.size
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.Slide
import androidx.transition.TransitionManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialFade
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import ru.meseen.dev.stock.R
import ru.meseen.dev.stock.data.Response
import ru.meseen.dev.stock.data.db.entitys.SearchItem
import ru.meseen.dev.stock.databinding.SearchFragmentBinding

@AndroidEntryPoint
class SearchFragment : Fragment(R.layout.search_fragment), OnSearchItemClick {

    private val vb by viewBinding(SearchFragmentBinding::bind, R.id.searchFragment)

    private val viewModel by viewModels<SearchViewModel>()

    private val adapter = SearchListAdapter(this)

    private val chipWords = arrayMapOf<String, String>()

    companion object {
        const val TAG = "SearchBSFragment"
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ViewCompat.setTransitionName(view, getString(R.string.search_list_transition_name))
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner, onBackPressedCallback
        )
        vb.toolbarNavHome.setOnClickListener {
            onBackPressedCallback.handleOnBackPressed()
        }
        setTransition(view)
        vb.searchView.setOnQueryTextListener(queryTextListener)
        vb.searchView.setIconifiedByDefault(false)

        vb.rvSearchList.layoutManager = LinearLayoutManager(view.context)
        vb.rvSearchList.adapter = adapter
        vb.chipGroupSearch.setOnCheckedChangeListener(chipListener)

        setupRefreshStateListener()
        submitSearchList()
        setupSearchedWordsChips()
        setupSwipeRefreshListener()
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            onBackPressed()
        }
    }

    private fun onBackPressed() {
        findNavController().popBackStack()
    }

    private fun setupSwipeRefreshListener() {
        lifecycleScope.launchWhenCreated {
            vb.searchSwiperefreshLayout.setOnRefreshListener {
                chekQuery(vb.searchView.query.toString())
            }
        }
    }

    private fun setupSearchedWordsChips() {
        lifecycleScope.launchWhenCreated {
            viewModel.lastSearchedWords().collectLatest { words ->
                words.forEach { searchWord ->
                    if (chipWords.contains(searchWord.word)) return@forEach
                    chipWords[searchWord.word] = searchWord.word
                    val transform = MaterialFade().apply {
                        duration = resources.getInteger(R.integer.def_fade_duration).toLong()
                    }
                    vb.chipGroupSearch.addView(
                        createChip(requireContext(), word = searchWord.word),
                        0
                    )
                    TransitionManager.beginDelayedTransition(vb.chipGroupSearch, transform)
                }
            }
        }
    }

    private fun createChip(context: Context, word: String): View = Chip(context).apply {
        chipStartPadding = resources.getDimension(R.dimen.search_chip_inner_padding)
        chipEndPadding = resources.getDimension(R.dimen.search_chip_inner_padding)
        text = word
        setOnClickListener(onChipListener)
        chipBackgroundColor = ResourcesCompat.getColorStateList(
            resources,
            R.color.search_chip_color,
            null
        )
        setTextColor(
            ResourcesCompat.getColorStateList(
                resources,
                R.color.search_chip_font_color,
                null
            )
        )
    }

    private fun submitSearchList() {
        lifecycleScope.launchWhenCreated {
            viewModel.resultsList.collectLatest {
                adapter.submitList(it)
            }
        }
    }

    private fun setupRefreshStateListener() {
        lifecycleScope.launchWhenCreated {
            viewModel.statusLoad.collectLatest {
                vb.searchSwiperefreshLayout.isRefreshing = it is Response.Loading
            }
        }
    }

    private val onChipListener = View.OnClickListener { chip ->
        chip as Chip
        vb.searchView.setQuery(chip.text, true)
        vb.searchView.queryHint = chip.text
        vb.searchView.setIconifiedByDefault(false)
    }

    private fun search(query: String) {
        viewModel.search(query)
    }

    private fun chekQuery(query: String) {
        if (query.isNotBlank() && query.isNotEmpty())
            if (query.length >= 2) {
                search(query)
            }
    }

    private val chipListener =
        ChipGroup.OnCheckedChangeListener { group, checkedId ->
            if (checkedId != -1 && checkedId < group.size - 1) {
                val chip = group[checkedId.minus(1)] as Chip
                chekQuery(chip.text.toString())
            }
        }

    private val queryTextListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            query?.let { chekQuery(it) }
            searchTimer.cancel()
            Log.d(TAG, "onQueryTextSubmit: $query")
            return false
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            Log.d(TAG, "onQueryTextChange: $newText")
            newText?.let {
                if (!chipWords.contains(it)) {
                    searchTimer.start()
                }
            }
            return false
        }
    }


    val searchTimer = object : CountDownTimer(5000L, 1000L) {
        override fun onTick(millisUntilFinished: Long) {

        }

        override fun onFinish() {
            chekQuery(vb.searchView.query.toString())
        }
    }

    private fun setTransition(view: View) {
        enterTransition = MaterialContainerTransform().apply {
            startView = requireActivity().findViewById(R.id.main_fab)
            endView = view
            duration = resources.getInteger(R.integer.def_motion_duration).toLong()
            scrimColor = Color.TRANSPARENT
            setAllContainerColors(Color.TRANSPARENT)
        }
        returnTransition = Slide().apply {
            duration = resources.getInteger(R.integer.def_motion_duration).toLong()
            addTarget(R.id.searchFragment)
        }
    }

    override fun itemClick(item: SearchItem, view: View) {
        val dialog = BottomSheetDialog(view.context)
        val bottomSheet =
            LayoutInflater.from(view.context).inflate(R.layout.search_bottom_sheet, vb.root, false)
        bottomSheet.findViewById<MaterialButton>(R.id.btn_add_to_watchlist).apply {
            val temp = "Add '${item.symbol}' to Watchlist"
            text = temp
            setOnClickListener {
                viewModel.addNewStock(item, false)
                dialog.dismiss()
            }
        }
        bottomSheet.findViewById<MaterialButton>(R.id.btn_add_to_favorites).apply {
            val temp = "Add '${item.symbol}' to Favorites"
            text = temp
            setOnClickListener {
                viewModel.addNewStock(item, true)
                dialog.dismiss()

            }
        }
        dialog.setContentView(bottomSheet)
        dialog.show()


    }

    override fun onDestroyView() {
        searchTimer.cancel()
        super.onDestroyView()
    }
}