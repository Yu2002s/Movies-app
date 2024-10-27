package com.dongyu.movies.parser

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.dongyu.movies.MoviesApplication
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * webView运行工具，模拟webView运行环境
 */
@SuppressLint("StaticFieldLeak")
object WebViewTool {

    private const val TAG = "WebViewTool"

    private val mHandler = Handler(Looper.getMainLooper())

    private const val TIME_OUT = 15000

    /**
     * 使用静态写法，全局只生成一个，忽略内存泄露
     */
    @JvmStatic
    private var _webView: WebView? = null
    private val webView get() = _webView!!

    private val webViewDialog by lazy {
        MaterialAlertDialogBuilder(MoviesApplication.context)
    }

    private var loadFlag = false

    private var result: String? = null

    private var time = System.currentTimeMillis()

    /**
     * 标记CloudFlare
     */
    private var isCloudFlare = false

    fun load(url: String): String? {
        if (loadFlag) {
            loadFlag = false
        }
        isCloudFlare = false
        loadFlag = true
        initWebView(url)
        Log.i(TAG, "load start")

        time = System.currentTimeMillis()
        while (!isCloudFlare && loadFlag && System.currentTimeMillis() - time < TIME_OUT) {
            // ignore empty body
        }
        loadFlag = false
        Log.i(TAG, "load end")
        mHandler.post {
            webView.stopLoading()
            webView.destroy()
            webView.removeAllViews()
            _webView = null
        }
        return result
    }

    private fun initWebView(url: String) {
        mHandler.post(object : Runnable {
            override fun run() {
                if (_webView != null) {
                    webView.clearHistory()
                    webView.stopLoading()
                    webView.loadUrl(url)
                    return
                }
                if (Looper.myLooper() == Looper.getMainLooper()) {
                    _webView = WebView(MoviesApplication.context)
                    initWebSettings()
                    webView.loadUrl(url)
                } else {
                    _webView = WebView(MoviesApplication.context)
                    initWebSettings()
                    webView.loadUrl(url)
                }
                mHandler.removeCallbacks(this)
            }
        })
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebSettings() {
        webView.settings.apply {
            javaScriptEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
            allowFileAccess = true
            allowContentAccess = true
            userAgentString = BaseParser.USER_AGENT
            setSupportZoom(true)
            displayZoomControls = true
            builtInZoomControls = true
            useWideViewPort = true
            layoutAlgorithm = WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING
            loadWithOverviewMode = true
            domStorageEnabled = true
        }
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(webView, true)
        webView.addJavascriptInterface(InJavaScriptLocalObj(), "local_obj")
        webView.webChromeClient = object : WebChromeClient() {

        }
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest
            ): Boolean {
                Log.i(TAG, "shouldOverrideUrlLoading: " + request.url + ", " + request.isRedirect)
                return false
            }

            override fun onPageFinished(view: WebView, url: String?) {
                super.onPageFinished(view, url)
                view.loadUrl("javascript:(function(){ local_obj.showSource(document.querySelector('html').innerHTML)})();")
                Log.i(TAG, "onPageFinished")
                CookieManager.getInstance().flush()
            }

            override fun onLoadResource(view: WebView, url: String) {
                super.onLoadResource(view, url)
                // 是cloudFlare
                /*if (url.contains("/cdn-cgi")) {
                    isCloudFlare = true
                    view.stopLoading()
                }*/
                Log.i(TAG, "onLoadResource: $url")
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError?
            ) {
                Log.i(TAG, "onReceivedError: " + request.url + ", " + request.isRedirect
                        + ", error: " + error.toString())
                if (request.url.toString() == view.url) {
                    loadFlag = false
                }
                super.onReceivedError(view, request, error)
            }
        }
    }

    class InJavaScriptLocalObj {
        @JavascriptInterface
        fun showSource(html: String?) {
            // Log.i(TAG, "showSource: $html")
            result = html
            loadFlag = false
            isCloudFlare = false
        }
    }
}