package com.omer.nasuhi.loodoscryptocurrency.ui.fragments.favorite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.omer.nasuhi.loodoscryptocurrency.R
import com.omer.nasuhi.loodoscryptocurrency.adapters.CoinListAdapter
import com.omer.nasuhi.loodoscryptocurrency.ui.fragments.main.MainActivity
import com.omer.nasuhi.loodoscryptocurrency.ui.main.MainViewModel
import com.omer.nasuhi.loodoscryptocurrency.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FRFavoriteCoins : Fragment(R.layout.frag_fav_coins) {


    private lateinit var recyclerView: RecyclerView

    @Inject
    lateinit var mainViewModel: MainViewModel

    @Inject
    lateinit var coinListAdapter: CoinListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.frag_fav_coins, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rcFavs)


        setupObservers()
        setupRecyclerView()

        mainViewModel.getFavoriteCoins()

        coinListAdapter.setOnItemClickListener { coinItem ->
            val bundle = Bundle().apply {
                putSerializable("coinResponse", coinItem)
                putBoolean("fromFavoriteCoinsFragment", true)
            }

            findNavController().navigate(
                R.id.action_favoriteCoinsFragment_to_coinDetailFragment,
                bundle
            )
        }

    }


    private fun setupObservers() {
        mainViewModel.getFavoriteCoins.observe(viewLifecycleOwner, { response ->
            when (response.status) {
                Resource.Status.SUCCESS -> {
                    (requireActivity() as MainActivity).hideLoadingView()
                    coinListAdapter.differ.submitList(response.data!!)
                    recyclerView.scheduleLayoutAnimation()
                }
                Resource.Status.ERROR -> {
                    (requireActivity() as MainActivity).hideLoadingView()
                    AlertDialog.Builder(requireContext())
                        .setMessage("Favori listeniz boş...")
                        .setPositiveButton("Tekrar dene") { dialog, _ ->
                            dialog.dismiss()
                            mainViewModel.getFavoriteCoins()
                        }.setNegativeButton("Tamam") { dialog, _ ->
                            dialog.dismiss()
                        }.show()
                }
                Resource.Status.LOADING -> {
                    (requireActivity() as MainActivity).showLoadingView()
                }
            }
        })
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


}
