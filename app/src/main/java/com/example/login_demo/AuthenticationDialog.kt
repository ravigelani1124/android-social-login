package com.example.login_demo

import android.app.Dialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient

class AuthenticationDialog(context: Context?, private val listener: AuthenticationListener) :
    Dialog(context!!) {
    lateinit var request_url: String
    lateinit var redirect_url: String
    lateinit var code_url: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.auth_dialog)
        redirect_url = context?.resources?.getString(R.string.redirect_url)!!
        code_url = context?.resources?.getString(R.string.code_url)!!
        request_url = context.resources.getString(R.string.base_url) +
                "oauth/authorize/?client_id=" +
                context.resources.getString(R.string.client_id) +
                "&redirect_uri=" + redirect_url +
                "&scope=user_profile,user_media&response_type=code"
//                "&response_type=token&display=touch&scope=public_content"

        initializeWebView()
    }

    private fun initializeWebView() {
        val webView = findViewById<WebView>(R.id.webView)
        webView.settings.javaScriptEnabled = true
        Log.e("Request Url--", request_url)
        Log.e("redirect_url--", redirect_url)
        webView.loadUrl(request_url)
        webView.webViewClient = webViewClient
    }

    var webViewClient: WebViewClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            if (url.startsWith(redirect_url)) {
                if (url.contains("code=")) {
                    val uri = Uri.parse(url)
                    Log.e("uri with code", uri.toString())
                    var access_token = uri.encodedQuery
                    access_token = access_token!!.substring(access_token.lastIndexOf("=") + 1)
                    Log.e("access_token", access_token)
                    listener.onTokenReceived(access_token)
                    dismiss()
                } else if (url.contains("?error")) {
                    Log.e("access_token", "getting error fetching access token")
                    dismiss()
                }
                return true
            }
            return false
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
//            if (url.contains("access_token=")) {

        }
    }


}