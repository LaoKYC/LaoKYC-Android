package com.codecamp.laokycmodule.repositories

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.auth0.android.jwt.JWT
import com.codecamp.laokycmodule.model.ModelClaims
import com.codecamp.laokycmodule.oauth.AuthManager
import com.codecamp.laokycmodule.oauth.SharedPreferencesRepository
import com.codecamp.laokycmodule.services.IClaimService
import com.codecamp.laokycmodule.services.IOIDCConfig
import com.google.gson.Gson
import net.openid.appauth.*
import java.lang.NullPointerException

class ClaimService(var oidcConfig: IOIDCConfig) : IClaimService {

    private var mAuthService: AuthorizationService? = null


    //private var _firstName : String = ""
    override var allClaims: String = ""
    override var idToken: String = ""
    override var accessToken: String = ""
    override var firstName: String = ""
    override var familyName: String  = ""
    override var phoneNumber: String = ""
    override var picture: String = ""
    override var userID: String = ""
    override var preferredUsername: String = ""
    override var gender: String = ""
    override var account: String = ""
    override var factor: String = ""
    override var sub: String = ""
    override var isLogOut: Boolean = true

    override fun ExtractClaims(context: Context, intent: Intent) {




        val resp = AuthorizationResponse.fromIntent(intent)
        val ex =
            AuthorizationException.fromIntent(intent)

        val authManager = AuthManager.getInstance(context , oidcConfig)
        try {
            authManager.setAuthState(resp, ex)
        } catch (e:Exception) {

        }

        // OpenID
        // Check State
        // LogOut - Share Pref

        if (resp != null) {
            val clientSecretPost =
                ClientSecretPost(authManager.auth.clientSecret)
            val tokenRequest = TokenRequest.Builder(
                authManager.authConfig,
                authManager.auth.clientId
            )
                .setAuthorizationCode(resp.authorizationCode)
                .setRedirectUri(Uri.parse(authManager.auth.redirectUri))
                .setRefreshToken(resp.accessToken)
                .setCodeVerifier(SharedPreferencesRepository(context).codeVerifier)
                .build()
            mAuthService = authManager.authService

            mAuthService!!.performTokenRequest(
                tokenRequest,
                clientSecretPost,
                AuthorizationService.TokenResponseCallback { response, ex ->

                    if (ex == null) {
                        isLogOut = false
                        authManager.updateAuthState(response, ex)
                        // MyApp.Token = authManager.authState.idToken
                        accessToken = authManager.authState.accessToken.toString()

                        val jwt = JWT(authManager.authState.idToken!!)
                        val allClaimsx =
                            jwt.claims
                        val gson = Gson()
                        val _allClaims = gson.toJson(allClaimsx)
                        val _result = gson.fromJson(_allClaims, ModelClaims::class.java)

                        idToken = jwt.toString()
                        allClaims = allClaimsx.toString()
                        phoneNumber = _result!!.phone!!.value.toString()
                        firstName = _result!!.name!!.value.toString()


                        try {
                            familyName = _result!!.familyName!!.value.toString()
                        } catch (e : NullPointerException) {
                            familyName = ""
                        }

                        preferredUsername = _result!!.preferredUsername!!.value.toString()
                        userID = _result!!.sub!!.value.toString()
                        account = _result!!.account!!.value.toString()
                       // gender = _result!!.gender!!.toString()
                        picture = "https://gateway.sbg.la/api/render/MyPhoto/" + preferredUsername + "?"
                        sub = _result!!.sub!!.value.toString()
                        try {
                            factor = _result!!.factor!!.value.toString()
                        } catch (e : KotlinNullPointerException) {
                            factor = ""
                        }




                    }
                })
            // authorization completed
        } else {
            // authorization failed, check ex for more details
            /*val loginIntent = Intent(this@LoginOnResultActivity, LoginActivity::class.java)
            startActivity(loginIntent)*/

        }
    }

}