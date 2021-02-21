package com.omer.nasuhi.loodoscryptocurrency.ui.fragments.detail

import android.app.Activity
import android.os.Bundle
import android.text.Html
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.omer.nasuhi.loodoscryptocurrency.R
import com.omer.nasuhi.loodoscryptocurrency.data.models.response.CoinDetailResponse
import com.omer.nasuhi.loodoscryptocurrency.ui.fragments.main.MainActivity
import com.omer.nasuhi.loodoscryptocurrency.ui.main.MainViewModel
import com.omer.nasuhi.loodoscryptocurrency.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class FRDetailCoin : Fragment(R.layout.frag_coin_detail) {


    private lateinit var coinImage: ImageView
    private lateinit var refreshInterval: EditText
    private lateinit var refreshIntervalDone: Button
    private lateinit var coinName: TextView
    private lateinit var hashAlgorithm: TextView
    private lateinit var currentPrice: TextView
    private lateinit var priceChangePercentage: TextView
    private lateinit var coinDescription: TextView
    private lateinit var addFavorite: LinearLayout

    @Inject
    lateinit var mainViewModel: MainViewModel

    private val args: FRDetailCoinArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.frag_coin_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        coinImage = view.findViewById(R.id.imCoinImage)
        coinName = view.findViewById(R.id.tvDetailCoinName)
        hashAlgorithm = view.findViewById(R.id.tvHashAlg)
        currentPrice = view.findViewById(R.id.tvCurrentPrice)
        priceChangePercentage = view.findViewById(R.id.tvDailyChange)
        refreshInterval = view.findViewById(R.id.etRepeat)
        refreshIntervalDone = view.findViewById(R.id.btSave)
        addFavorite = view.findViewById(R.id.llAddFav)
        coinDescription = view.findViewById(R.id.tvDesc)

        coinDescription.movementMethod = ScrollingMovementMethod()


        (requireActivity() as MainActivity).supportActionBar?.title = "Coin Detail"

        setupObserver()
        setupIntervalChange()
        setupAddFavorite()

        mainViewModel.getCoinsDetail(requireContext(), args.coinResponse.coinId)
    }


    private fun setupObserver() {
        //api return a coin detail and update coin detail
        mainViewModel.coinDetail.observe(viewLifecycleOwner, { response ->
            when (response.status) {
                Resource.Status.SUCCESS -> {
                    (requireActivity() as MainActivity).hideLoadingView()
                    if (response.data != null)
                        setCoinDetail(response.data)
                }
                Resource.Status.ERROR -> {
                    (requireActivity() as MainActivity).hideLoadingView()
                    AlertDialog.Builder(requireContext())
                        .setMessage(response.message)
                        .setPositiveButton("Try Again") { dialog, _ ->
                            dialog.dismiss()
                            mainViewModel.getCoinsDetail(requireContext(), args.coinResponse.coinId)
                        }.show()
                }
                Resource.Status.LOADING -> {
                    (requireActivity() as MainActivity).showLoadingView()
                }
            }
        })

        // if user click on add favorite fab than add clicked coin to firebase firestore
        mainViewModel.addFavoriteCoin.observe(viewLifecycleOwner, { response ->
            when (response.status) {
                Resource.Status.SUCCESS -> {
                    (requireActivity() as MainActivity).hideLoadingView()
                    Toast.makeText(requireContext(), response.data, Toast.LENGTH_SHORT).show()
                }
                Resource.Status.ERROR -> {
                    (requireActivity() as MainActivity).hideLoadingView()
                    AlertDialog.Builder(requireContext())
                        .setMessage(response.message)
                        .setPositiveButton("Try Again") { dialog, _ ->
                            dialog.dismiss()
                            addToFavoriteCoins()
                        }.show()
                }
                Resource.Status.LOADING -> {
                    (requireActivity() as MainActivity).showLoadingView()
                }
            }
        })

        // if user come from favorite coins fragment and click on add favorite fab than delete clicked coin from firebase firestore
        mainViewModel.deleteFavoriteCoin.observe(viewLifecycleOwner, { response ->
            when (response.status) {
                Resource.Status.SUCCESS -> {
                    (requireActivity() as MainActivity).hideLoadingView()
                    Toast.makeText(requireContext(), response.data, Toast.LENGTH_SHORT).show()
                }
                Resource.Status.ERROR -> {
                    (requireActivity() as MainActivity).hideLoadingView()
                    AlertDialog.Builder(requireContext())
                        .setMessage(response.message)
                        .setPositiveButton("Try Again") { dialog, _ ->
                            dialog.dismiss()
                            deleteFromFavoriteCoins()
                        }.show()
                }
                Resource.Status.LOADING -> {
                    (requireActivity() as MainActivity).showLoadingView()
                }
            }
        })
    }

    private fun setupIntervalChange() {
        refreshIntervalDone.setOnClickListener {
            val interval = refreshInterval.text.toString().toIntOrNull()

            if (refreshInterval.text.isEmpty())
                Toast.makeText(
                    requireContext(),
                    "Lütfen yenileme dakikası girin...",
                    Toast.LENGTH_SHORT
                ).show()

            if (interval != null) {
                setRefreshInterval(interval)
                Toast.makeText(
                    requireContext(),
                    "Yenileme dakikası $interval dakika olarak ayarlandı.",
                    Toast.LENGTH_SHORT
                ).show()
                refreshInterval.clearFocus()

                val inputMethodManager: InputMethodManager =
                    requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(requireView().windowToken, 0)
            }
        }
    }

    private fun setRefreshInterval(interval: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            while (true) {
                mainViewModel.getCoinsDetail(requireContext(), args.coinResponse.coinId)
                // 1 second = 1000 millisecond
                // 1 minute = 60000 millisecond
                val delay = interval * 60000
                delay(delay.toLong())
            }
        }
    }

    private fun setupAddFavorite() {
        addFavorite.setOnClickListener {

            if (args.fromFavoriteCoinsFragment)
                deleteFromFavoriteCoins()
            else
                addToFavoriteCoins()


        }
    }

    private fun addToFavoriteCoins() {
        val coin = hashMapOf(
            "id" to args.coinResponse.coinId,
            "name" to args.coinResponse.name,
            "symbol" to args.coinResponse.symbol
        )
        Toast.makeText(
            requireContext(),
            "Favorilere eklendi...",
            Toast.LENGTH_SHORT
        ).show()
        mainViewModel.saveFavoriteCoin(coin)
    }

    private fun deleteFromFavoriteCoins() {
        val coin = hashMapOf(
            "id" to args.coinResponse.coinId,
            "name" to args.coinResponse.name,
            "symbol" to args.coinResponse.symbol
        )

        Toast.makeText(
            requireContext(),
            "Favorilerden silindi...",
            Toast.LENGTH_SHORT
        ).show()

        mainViewModel.deleteFavoriteCoin(coin)
    }


    private fun setCoinDetail(coinDetailResponse: CoinDetailResponse) {
        coinName.text = args.coinResponse.name
        Glide.with(requireContext()).load(coinDetailResponse.image?.imageLarge).into(coinImage)

        if (coinDetailResponse.hashing_algorithm != null)
            hashAlgorithm.text = coinDetailResponse.hashing_algorithm
        else
            hashAlgorithm.text = "Hash alg. bulunamadı"

        if (coinDetailResponse.marketData?.current_price?.usd != null)
            currentPrice.text = coinDetailResponse.marketData.current_price.usd.toString() + " $"
        else
            currentPrice.text = "Güncel fiyat verisi yok"

        if (coinDetailResponse.marketData?.priceChancePercentage_24h != null)
            if (coinDetailResponse.marketData?.priceChancePercentage_24h < 0) {
                priceChangePercentage.setTextColor(getResources().getColor(R.color.general_red));
                currentPrice.setTextColor(getResources().getColor(R.color.general_red));

                priceChangePercentage.text =
                    coinDetailResponse.marketData.priceChancePercentage_24h.toString()
            } else {
                priceChangePercentage.setTextColor(getResources().getColor(R.color.general_blue));
                currentPrice.setTextColor(getResources().getColor(R.color.general_blue));
                priceChangePercentage.text = "+" +
                        coinDetailResponse.marketData.priceChancePercentage_24h.toString()
            }

        if (coinDetailResponse.description?.description_en != null && coinDetailResponse.description.description_en != "") {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                coinDescription.text = Html.fromHtml(
                    coinDetailResponse.description.description_en,
                    Html.FROM_HTML_MODE_COMPACT
                )
            } else {
                coinDescription.text = Html.fromHtml(coinDetailResponse.description.description_en)
            }
        } else {
            coinDescription.text = "Coin hakkında genel bilgi bulunamadı..."
        }
    }

}

