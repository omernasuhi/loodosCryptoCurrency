package com.omer.nasuhi.loodoscryptocurrency.ui.fragments.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.omer.nasuhi.loodoscryptocurrency.R
import com.omer.nasuhi.loodoscryptocurrency.adapters.CoinListAdapter
import com.omer.nasuhi.loodoscryptocurrency.data.models.response.CoinResponse
import com.omer.nasuhi.loodoscryptocurrency.ui.fragments.main.MainActivity
import com.omer.nasuhi.loodoscryptocurrency.ui.main.MainViewModel
import com.omer.nasuhi.loodoscryptocurrency.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FRListCoins : Fragment(R.layout.frag_coin_list) {

    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var btFav: Button

    private lateinit var coinListData: List<CoinResponse>

    @Inject
    lateinit var mainViewModel: MainViewModel

    @Inject
    lateinit var coinListAdapter: CoinListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.frag_coin_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchView = view.findViewById(R.id.fr_coin_list_search_view)
        recyclerView = view.findViewById(R.id.rcCoinList)
        btFav = view.findViewById(R.id.btFav)

        (requireActivity() as MainActivity).supportActionBar?.title = getString(R.string.app_name)

        setupObservers()
        setupRecyclerView()
        setupSearchView()
        setupFavoriteFab()

        mainViewModel.getCoinsList(requireContext())

        coinListAdapter.setOnItemClickListener { coinItem ->
            val bundle = Bundle().apply {
                putSerializable("coinResponse", coinItem)
                putBoolean("fromFavoriteCoinsFragment", false)
            }

            findNavController().navigate(R.id.action_coinListFragment_to_coinDetailFragment, bundle)
        }
    }

    private fun setupObservers() {
        //if api return a coin list than show that list via recycler view and save local database
        mainViewModel.coinList.observe(viewLifecycleOwner, { response ->
            when (response.status) {
                Resource.Status.SUCCESS -> {
                    (requireActivity() as MainActivity).hideLoadingView()
                    coinListData = response.data!!
                    coinListAdapter.differ.submitList(coinListData)
                    recyclerView.scheduleLayoutAnimation()

                    mainViewModel.insertCoinList(coinListData)
                }
                Resource.Status.ERROR -> {
                    (requireActivity() as MainActivity).hideLoadingView()
                    AlertDialog.Builder(requireContext())
                        .setMessage(response.message)
                        .setCancelable(false)
                        .setPositiveButton("Try Again") { dialog, _ ->
                            dialog.dismiss()
                            mainViewModel.getCoinsList(requireContext())
                        }.show()
                }
                Resource.Status.LOADING -> {
                    (requireActivity() as MainActivity).showLoadingView()
                }
            }
            response.status = null
        })

        //if api return a coin list than save that list to local database for searching
        mainViewModel.insertCoinList.observe(viewLifecycleOwner, { response ->
            when (response.status) {
                Resource.Status.SUCCESS -> {
                    (requireActivity() as MainActivity).hideLoadingView()
                }
                Resource.Status.ERROR -> {
                    (requireActivity() as MainActivity).hideLoadingView()
                    AlertDialog.Builder(requireContext())
                        .setMessage(response.message)
                        .setCancelable(false)
                        .setPositiveButton("Try Again") { dialog, _ ->
                            dialog.dismiss()
                            mainViewModel.insertCoinList(coinListData)
                        }.show()
                }
                Resource.Status.LOADING -> {
                    (requireActivity() as MainActivity).showLoadingView()
                }
            }
            response.status = null
        })

        //if user use search than local database return search result
        mainViewModel.filteredCoinList.observe(viewLifecycleOwner, { response ->
            when (response.status) {
                Resource.Status.SUCCESS -> {
                    (requireActivity() as MainActivity).hideLoadingView()
                    coinListAdapter.differ.submitList(response.data)
                    recyclerView.scheduleLayoutAnimation()
                }
                Resource.Status.ERROR -> {
                    (requireActivity() as MainActivity).hideLoadingView()
                    AlertDialog.Builder(requireContext())
                        .setMessage(response.message)
                        .setCancelable(false)
                        .setPositiveButton("OK") { dialog, _ ->
                            dialog.dismiss()
                        }.show()
                }
                Resource.Status.LOADING -> {
                    (requireActivity() as MainActivity).showLoadingView()
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        runLayoutAnimation()
    }

    private fun runLayoutAnimation() {
        val layoutAnimationController =
            AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_animation_fall_down)
        recyclerView.layoutAnimation = layoutAnimationController
        recyclerView.scheduleLayoutAnimation()
    }

    private fun setupRecyclerView() {
        val layoutAnimationController =
            AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_animation_fall_down)
        recyclerView.apply {
            adapter = coinListAdapter
            layoutManager = LinearLayoutManager(requireContext())
            layoutAnimation = layoutAnimationController
        }
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null && query != "") {
                    mainViewModel.filterCoinList(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText == "") {
                    coinListAdapter.differ.submitList(coinListData)
                    recyclerView.scheduleLayoutAnimation()
                }
                return true
            }
        })
    }

    private fun setupFavoriteFab() {
        btFav.setOnClickListener {
            findNavController().navigate(R.id.action_coinListFragment_to_favoriteCoinsFragment)
        }
    }
}