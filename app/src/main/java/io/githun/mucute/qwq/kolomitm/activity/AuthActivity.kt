package io.githun.mucute.qwq.kolomitm.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import io.githun.mucute.qwq.kolomitm.R
import io.githun.mucute.qwq.kolomitm.databinding.ActivityAuthBinding
import io.githun.mucute.qwq.kolomitm.manager.AccountManager
import net.raphimc.minecraftauth.step.msa.StepMsaDeviceCode

class AuthActivity : BaseActivity() {

    private lateinit var viewBinding: ActivityAuthBinding

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        applyWindowInsets(viewBinding.root)

        val linearProgressIndicator = viewBinding.linearProgressIndicator
        val webView = viewBinding.webView

        webView.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.cacheMode = WebSettings.LOAD_NO_CACHE
            settings.safeBrowsingEnabled = false
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            settings.displayZoomControls = false

            isVerticalScrollBarEnabled = false
            isHorizontalScrollBarEnabled = false

            webChromeClient = object : WebChromeClient() {

                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    linearProgressIndicator.progress = newProgress
                    if (linearProgressIndicator.progress == 100) {
                        linearProgressIndicator.visibility = View.GONE
                    } else {
                        linearProgressIndicator.visibility = View.VISIBLE
                    }
                }

            }

            webViewClient = object : WebViewClient() {

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    return false
                }

            }
        }

        addAccount()
    }

    private fun addAccount() {
        val deviceType = intent.getIntExtra("deviceType", 0)
        AccountManager.addAccount(
            deviceType,
            StepMsaDeviceCode.MsaDeviceCodeCallback {
                runOnUiThread {
                    viewBinding.webView.loadUrl(it.directVerificationUri)
                }
            }
        ) { throwable ->
            throwable?.let {
                it.printStackTrace()
                Toast.makeText(
                    this,
                    getString(R.string.encounter_an_exception, it.message),
                    Toast.LENGTH_SHORT
                )
                    .show()
            } ?: finish()
        }
    }

}