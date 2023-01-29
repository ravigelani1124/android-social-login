package com.example.login_demo

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.SigningInfo
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.webkit.CookieManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.login_demo.Network.ApiCall
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.linecorp.linesdk.LoginDelegate
import com.linecorp.linesdk.LoginListener
import com.linecorp.linesdk.Scope
import com.linecorp.linesdk.auth.LineAuthenticationParams
import com.linecorp.linesdk.auth.LineLoginResult
import com.squareup.picasso.Picasso
import com.twitter.sdk.android.core.*
import com.twitter.sdk.android.core.identity.TwitterAuthClient
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.apache.commons.lang3.RandomStringUtils
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import java.util.concurrent.TimeUnit

/* Google Integration in :jai.gujaral@gmail.com --- pass: infollp#$%567 , as Login demo*/
/* Instagram and Facebook Integration in :jai.gujaral@gmail.com --- pass: infollp#$%5678 , as Login demo*/
/*  Line Integration in :jai.gujaral@gmail.com --- pass: Test@12345 , as Line Login*/
/*  Twitter Integration in :sundar.malya@gmail.com --- pass: sundar@!321 , as DemoLogin121*/

class MainActivity : AppCompatActivity(), AuthenticationListener {
    public var token: String? = ""
    public var code: String? = ""
    lateinit var appPreferences: AppPreferences
    lateinit var authenticationDialog: AuthenticationDialog
    internal var mTwitterAuthClient: TwitterAuthClient? = null
    lateinit var mGoogleSignInClient: GoogleSignInClient
    val RC_SIGN_IN: Int = 101
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        appPreferences = AppPreferences(this)

        //check already have access token
        token = appPreferences.getString(AppPreferences.TOKEN);
        if (token != null) {
            getUserInfoByAccessToken(token!!);
        }

        btnInsta.setOnClickListener {
            if (token != null) {
                Instagramlogout()
            } else {
                authenticationDialog = AuthenticationDialog(this, this)
                authenticationDialog.setCancelable(true)
                authenticationDialog.show()
            }
        }

        btnTwitter.setOnClickListener {
            if (btnTwitter.text.toString().equals("Logout", true)) {
                twitterLogout()
            } else {
                initTwitter()
            }

        }
        btnLine.setOnClickListener {
            LineLogin()
        }

        btnGoogle.setOnClickListener {
            if (btnGoogle.text.toString().equals("Logout", true)) {
                googleSignOut()
            } else {
                initGoogle()
            }
        }

        btnFacebook.setOnClickListener {
            facebookInit()
        }
        Utility.getFaceBookHashKey(this)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode === RC_SIGN_IN) {
            Log.e("onActiviry Result", "Google ")
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }else if (mTwitterAuthClient != null) {
            Log.e("onActiviry Result", "Twitter ")
            mTwitterAuthClient!!.onActivityResult(requestCode, resultCode, data)
        }
        loginDeligate.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data);

    }

    //TODO: Instagram Login-----------------------
    override fun onTokenReceived(auth_token: String?) {
        if (auth_token == null)
            return;
//        appPreferences.putString(AppPreferences.TOKEN, auth_token);
        code = auth_token;
        getUserToken(code!!);
    }

    private fun getUserInfoByAccessToken(token: String) {
        val task = RequestInstagramAPI(this)
        task.execute()
    }

    private fun getUserToken(token: String) {
        val task = RequestTokenAPI(this)
        task.execute()
    }

    companion object {
        val interceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .readTimeout(600, TimeUnit.SECONDS)
            .connectTimeout(600, TimeUnit.SECONDS)
            .build()

        class RequestInstagramAPI(private var activity: MainActivity) :
            AsyncTask<Void?, String?, String?>() {

            override fun onPostExecute(response: String?) {
                super.onPostExecute(response)
                if (response != null) {
                    try {
                        val jsonObject = JSONObject(response)
                        Log.e("User info response", jsonObject.toString())
//                            val jsonData = jsonObject.getJSONObject("data")
                        if (jsonObject.has("id")) {

                            activity.appPreferences.putString(
                                AppPreferences.USER_ID,
                                jsonObject.getString("id")
                            )
                            activity.appPreferences.putString(
                                AppPreferences.USER_NAME,
                                jsonObject.getString("username")
                            )
                            /* activity.appPreferences.putString(
                                 AppPreferences.PROFILE_PIC,
                                 jsonData.getString("profile_picture")
                             )*/

                            //TODO: сохранить еще данные
                            activity.Instagramlogin()
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                } else {
                    val toast =
                        Toast.makeText(activity, "Ошибка входа!", Toast.LENGTH_LONG)
                    toast.show()
                }
            }

            override fun doInBackground(vararg params: Void?): String? {
                // Try with Retrofit
                val retrofit: Retrofit = Retrofit.Builder()
                    .client(okHttpClient)
                    .baseUrl(activity.getResources().getString(R.string.get_user_info_url))
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                val objApiInterface: ApiCall = retrofit.create<ApiCall>(ApiCall::class.java)

                val call: Call<JsonObject?>? =
                    objApiInterface.getUserInfo("id,username", activity.token)
                var responce: String = ""
                call!!.enqueue(object : Callback<JsonObject?> {
                    override fun onResponse(
                        call: Call<JsonObject?>,
                        response: Response<JsonObject?>
                    ) {

                        try {
                            val jsonObject = response.body()!!.asJsonObject
//                            Log.e("Token response", jsonObject.toString())
                            responce = jsonObject.toString()
                            onPostExecute(responce)

                        } catch (e: Exception) {

                            Log.e("onResponse", "There is an error")
                            e.printStackTrace()
                            //generateNoteOnSD(getApplicationContext(),"getManualLocationCatch",e.toString(),new LatLng(lat,lng));
                        }
                    }

                    override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                        Log.e("onFailure", t.toString())

                    }
                })
                return null
            }
        }

        class RequestTokenAPI(private var activity: MainActivity) :
            AsyncTask<Void?, String?, String?>() {

            override fun onPostExecute(response: String?) {
                super.onPostExecute(response)
                if (response != null) {
                    try {
                        val jsonObject = JSONObject(response)
                        Log.e("Token response", jsonObject.toString())
                        if (jsonObject.has("access_token")) {
                            activity.token = jsonObject.optString("access_token")
                            activity.appPreferences.putString(AppPreferences.TOKEN, activity.token);
                            activity.getUserInfoByAccessToken(activity.token!!);
                        }


                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                } else {
                    val toast =
                        Toast.makeText(activity, "Ошибка входа!", Toast.LENGTH_LONG)
                    toast.show()
                }
            }

            override fun doInBackground(vararg params: Void?): String? {

//                Try with Retrofit
                var access_token_url: String =
                    activity.getResources().getString(R.string.access_token_url)

                val retrofit: Retrofit = Retrofit.Builder()
                    .client(okHttpClient)
                    .baseUrl(access_token_url)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                val objApiInterface: ApiCall = retrofit.create<ApiCall>(ApiCall::class.java)

                val call: Call<JsonObject?>? = objApiInterface.getRequestToken(
                    activity.getResources().getString(
                        R.string.client_id
                    ),
                    activity.getResources().getString(R.string.app_secret),
                    "authorization_code",
                    activity.getResources().getString(R.string.redirect_url),
                    activity.code
                )
                var responce: String = ""
                call!!.enqueue(object : Callback<JsonObject?> {
                    override fun onResponse(
                        call: Call<JsonObject?>,
                        response: Response<JsonObject?>
                    ) {

                        try {
                            val jsonObject = response.body()!!.asJsonObject
//                            Log.e("Token response", jsonObject.toString())
                            responce = jsonObject.toString()
                            onPostExecute(responce)

                        } catch (e: Exception) {

                            Log.e("onResponse", "There is an error")
                            e.printStackTrace()
                            //generateNoteOnSD(getApplicationContext(),"getManualLocationCatch",e.toString(),new LatLng(lat,lng));
                        }
                    }

                    override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                        Log.e("onFailure", t.toString())

                    }
                })
                return null
            }
        }
    }

    fun Instagramlogin() {
        btnInsta!!.text = "LOGOUT"
        info!!.visibility = View.VISIBLE
//        val pic: ImageView = findViewById(R.id.pic)
//        Picasso.get().load(appPreferences.getString(AppPreferences.PROFILE_PIC)).into(pic)
        val id: TextView = findViewById(R.id.id)
        id.text = appPreferences.getString(AppPreferences.USER_ID)
        val name: TextView = findViewById(R.id.name)
        name.text = appPreferences.getString(AppPreferences.USER_NAME)
    }

    fun Instagramlogout() {
        btnInsta!!.text = "INSTAGRAM LOGIN"
        token = null
        info!!.visibility = View.GONE
        appPreferences.clear()
    }


    //TODO: Twitter Login-----------------------
    fun initTwitter() {
        val config = TwitterConfig.Builder(this)
            .logger(DefaultLogger(Log.DEBUG))//enable logging when app is in debug mode
            .twitterAuthConfig(
                TwitterAuthConfig(
                    getString(R.string.twitter_api_key),
                    getString(R.string.twitter_secret_key)
                )
            )
            //pass the created app Consumer KEY and Secret also called API Key and Secret
            .debug(true)//enable debug mode
            .build()

//finally initialize twitter with created configs
        Twitter.initialize(config)
        mTwitterAuthClient = TwitterAuthClient()

        twitterLogin()
    }

    private fun getTwitterSession(): TwitterSession? {

        //NOTE : if you want to get token and secret too use uncomment the below code
        /*TwitterAuthToken authToken = session.getAuthToken();
        String token = authToken.token;
        String secret = authToken.secret;*/

        return TwitterCore.getInstance().sessionManager.activeSession
    }

    fun twitterLogin() {
        if (getTwitterSession() == null) {
            mTwitterAuthClient!!.authorize(
                this,
                object : com.twitter.sdk.android.core.Callback<TwitterSession>() {
                    override fun success(twitterSessionResult: Result<TwitterSession>) {
                        Toast.makeText(this@MainActivity, "Success", Toast.LENGTH_SHORT).show()
                        val twitterSession = twitterSessionResult.data
                        fetchTwitterEmail(twitterSession)

                    }

                    override fun failure(e: TwitterException) {
                        Toast.makeText(this@MainActivity, "Failure", Toast.LENGTH_SHORT).show()
                    }
                })
        } else {//if user is already authenticated direct call fetch twitter email api
            fetchTwitterEmail(getTwitterSession())
        }
    }

    fun twitterLogout() {
        Toast.makeText(this@MainActivity, "Logout Successfull.", Toast.LENGTH_SHORT)
            .show()
//                    mTwitterAuthClient!!.cancelAuthorize()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().flush()
        }
        TwitterCore.getInstance().getSessionManager().clearActiveSession()
        btnTwitter.setText("Twitter Login")
    }

    fun fetchTwitterEmail(twitterSession: TwitterSession?) {
        mTwitterAuthClient?.requestEmail(
            twitterSession,
            object : com.twitter.sdk.android.core.Callback<String>() {
                override fun success(result: Result<String>) {
                    //here it will give u only email and rest of other information u can get from TwitterSession

                    Log.e("twitterLogin:userId", "" + twitterSession!!.userId)
                    Log.e("twitterLogin:userName", "" + twitterSession!!.userName)
                    Log.e("twitterLogin:data", "" + result.data)

                    btnTwitter.setText("Logout")
                    var userId = twitterSession!!.userId
                    var username = twitterSession!!.userName
                    var email = result.data
                    var token = twitterSession.userId.toString()
                    var str = "Now you are successfully login with twitter \n\n"
                    var tokenStr = ""
                    var usernameStr = ""
                    var emailStr = ""
                    if (token != null || token != "") {
                        tokenStr = "User Id : " + token + "\n\n"
                    }

                    if (username != null || username != "") {
                        usernameStr = "Username : " + username + "\n\n"
                    }
                    info!!.visibility = View.VISIBLE
//        val pic: ImageView = findViewById(R.id.pic)
//        Picasso.get().load(appPreferences.getString(AppPreferences.PROFILE_PIC)).into(pic)
                    val id: TextView = findViewById(R.id.id)
                    id.text = token
                    val name: TextView = findViewById(R.id.name)
                    name.text = username + "\n" + email
//                if (email != null || email != "") {
//                    emailStr = "Email ID : " + email + "\n\n"
//                }

//                txtViewDetails!!.setText("" + str + tokenStr + usernameStr + emailStr)

                }

                override fun failure(exception: TwitterException) {
                    Toast.makeText(
                        this@MainActivity,
                        "Failed to authenticate. Please try again.",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            })
    }


    //TODO : Line Login Integration..
    var loginDeligate: LoginDelegate = LoginDelegate.Factory.create()
    private fun LineLogin() {

        line_login_btn.enableLineAppAuthentication(true)
        line_login_btn.setLoginDelegate(loginDeligate)
        line_login_btn.setChannelId(getString(R.string.line_channel_id));
        // set up required scopes and nonce.
        line_login_btn.setAuthenticationParams(
            LineAuthenticationParams.Builder()
                .scopes(Arrays.asList(Scope.PROFILE)) // .nonce("<a randomly-generated string>") // nonce can be used to improve security
                // set nonce
                .nonce(RandomStringUtils.randomAlphanumeric(16))

                .build()
        )
        line_login_btn.addLoginListener(object : LoginListener {
            override fun onLoginSuccess(result: LineLoginResult) {

                try {
//                    val jsonObject: JSONObject = JSONObject(result.toString())
                    Log.e("Line Login--", result.toString())
                    Log.e("Line user_id--", result.lineProfile?.userId)
                    Log.e("Line user_name--", result.lineProfile?.displayName)
                    Log.e("Line profile_pic--", result.lineProfile?.pictureUrl.toString())
                    info!!.visibility = View.VISIBLE

                    Picasso.get().load(result.lineProfile?.pictureUrl.toString()).into(pic)
                    id.text = result.lineProfile?.userId
                    name.text = result.lineProfile?.displayName
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
                Toast.makeText(this@MainActivity, "Login success", Toast.LENGTH_SHORT).show()
            }

            override fun onLoginFailure(result: LineLoginResult?) {
                Toast.makeText(this@MainActivity, "Login failure", Toast.LENGTH_SHORT).show()
            }
        })

        line_login_btn.callOnClick()
    }

    //    TODO: Google Login Integration
    fun initGoogle() {

        // Configure sign-in to request the user's ID, email address, and basic
// profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val gso: GoogleSignInOptions =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        // Check for existing Google Sign In account, if the user is already signed in
// the GoogleSignInAccount will be non-null.
        val account: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            updateUI(account)
        } else {
            googleSignin()
        }

    }

    fun googleSignin() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    fun googleSignOut() {


        mGoogleSignInClient.signOut().addOnCompleteListener(
            this
        ) { Toast.makeText(this, "You are logout from google", Toast.LENGTH_LONG).show() }
    }

    private fun updateUI(acct: GoogleSignInAccount?) {

        if (acct != null) {
            val personName = acct.displayName
            val personGivenName = acct.givenName
            val personFamilyName = acct.familyName
            val personEmail = acct.email
            val personId = acct.id
            val personPhoto: Uri? = acct.photoUrl

            Log.e("GOOGLE LOGIN USER ID=", personId)
            Log.e("GOOGLE LOGIN NAME=", personName)
            Log.e("GOOGLE LOGIN email=", personEmail)
            Log.e("GOOGLE LOGIN photo=", personPhoto.toString())

            Picasso.get().load(personPhoto).into(pic)
            id.text = personId
            name.text = personName + "\n" + personEmail

            googleSignOut()
        }
    }


    private fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount? = task.getResult(ApiException::class.java)
            Log.e("Google signInResult:", "Success")
            // Signed in successfully, show authenticated UI.
            updateUI(account)
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.e("Google signInResult:", "failed code=" + e.getStatusCode())
            updateUI(null)
        }
    }

//    TODO: Facebook Login Integration



    var callbackManager = CallbackManager.Factory.create();
    fun facebookInit() {
        LoginManager.getInstance().logInWithReadPermissions(
            this,
            Arrays.asList("email", "public_profile")
        )
        LoginManager.getInstance().registerCallback(
            callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult?) {
                    Log.e("Facebook:onSuccess:--", Gson().toJson(result))
                    getFacebookUser(result?.accessToken)
                }

                override fun onCancel() {
                    Log.e("facebook", ":onCancel")
                }

                override fun onError(error: FacebookException?) {
                    Log.e("facebook:onError", error.toString())

                    if (error is FacebookAuthorizationException) {
                        if (AccessToken.getCurrentAccessToken() != null) {
                            FacebookSignout()
                        }
                    }
                }

            })
    }

    private fun getFacebookUser(token: AccessToken?) {
        val request = GraphRequest.newMeRequest(
            token,
            object : GraphRequest.GraphJSONObjectCallback {
                override fun onCompleted(
                    user: JSONObject?,
                    response: GraphResponse?
                ) {
                    if(user != null){
                        Log.e("Facebook User: ",user.toString())
                        val picture = Uri.parse(
                            user?.getJSONObject("picture").getJSONObject("data").getString("url")
                        )
                        val personName: String? = user?.getString("name")
                        val user_id: String? = user?.getString("id")
                        var personEmail: String? = null
                        if (user!!.has("email")) {
                            personEmail = user?.getString("email")
                        }

                        Picasso.get().load(picture).into(pic)
                        id.text = user_id
                        name.text = personName + "\n" + personEmail

                        FacebookSignout()
                    }

                }
            })
        val parameters = Bundle()
        parameters.putString("fields", "picture,name,id,email,permissions")
        request.parameters = parameters
        request.executeAsync()
    }

    fun FacebookSignout(){
        LoginManager.getInstance().logOut()
    }


}
