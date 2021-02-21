package com.omer.nasuhi.loodoscryptocurrency.ui.main.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.omer.nasuhi.loodoscryptocurrency.R
import com.omer.nasuhi.loodoscryptocurrency.ui.fragments.main.MainActivity
import com.omer.nasuhi.loodoscryptocurrency.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private lateinit var loadingSpinner: ProgressBar
    private lateinit var loadingTextView: TextView

    @Inject
    lateinit var splashViewModel: SplashViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        this.window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        supportActionBar?.hide()

        loadingSpinner = findViewById(R.id.loadingSpinner)

        setupObservers()
        signInFirebase()
        navigateMainActivity()
    }


    private fun signInFirebase() {
        splashViewModel.signInFirebase()
    }

    private fun setupObservers() {
        //this is for firebase auth via email and password
        splashViewModel.firebaseUser.observe(this, { response ->
            when (response.status) {
                Resource.Status.SUCCESS -> {
                    splashViewModel.checkApiStatus(this)
                }
                Resource.Status.ERROR -> {
                    loadingSpinner.visibility = View.GONE
                    AlertDialog.Builder(this)
                        .setMessage(response.message)
                        .setCancelable(false)
                        .setPositiveButton("Try Again") { dialog, _ ->
                            dialog.dismiss()
                            splashViewModel.signInFirebase()
                        }.show()
                }
                Resource.Status.LOADING -> {
                    loadingSpinner.visibility = View.VISIBLE
                }
            }
        })

        //this is for api status which using for coin data
        splashViewModel.apiStatus.observe(this, { response ->
            when (response.status) {
                Resource.Status.SUCCESS -> {
                    loadingSpinner.visibility = View.GONE
                    loadingTextView.visibility = View.GONE
                    navigateMainActivity()
                }
                Resource.Status.ERROR -> {
                    loadingSpinner.visibility = View.GONE
                    AlertDialog.Builder(this)
                        .setMessage(response.message)
                        .setCancelable(false)
                        .setPositiveButton("Try Again") { dialog, _ ->
                            dialog.dismiss()
                            splashViewModel.checkApiStatus(this)
                        }.show()
                }
                Resource.Status.LOADING -> {
                    loadingSpinner.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun navigateMainActivity() {
        Handler().postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }, 2000)
    }
}