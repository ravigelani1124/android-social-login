package com.example.login_demo

interface AuthenticationListener {
    fun onTokenReceived(auth_token: String?)
}