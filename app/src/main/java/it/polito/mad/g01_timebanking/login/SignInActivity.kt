package it.polito.mad.g01_timebanking.login

import android.content.ContentValues
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import it.polito.mad.g01_timebanking.MainActivity
import it.polito.mad.g01_timebanking.R


class SignInActivity : AppCompatActivity() {
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private lateinit var signUpRequest: BeginSignInRequest

    private val REQ_ONE_TAP = 2

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_login)

        val signInButton = findViewById<SignInButton>(R.id.sign_in_button)
        signInButton.setSize(SignInButton.SIZE_WIDE)

        signInButton.setOnClickListener{
            signIn()
        }

        signIn()
    }

    private fun signIn() {
        /* Building sign-up request code */
        oneTapClient = Identity.getSignInClient(this)
        signUpRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    // Your server's client ID, not your Android client ID.
                    .setServerClientId(getString(R.string.g01_web_client_id))
                    // Show all accounts on the device.
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .build()

        /* Building Google Sign-in code */
        signInRequest = BeginSignInRequest.builder()
            .setPasswordRequestOptions(
                BeginSignInRequest.PasswordRequestOptions.builder()
                    .setSupported(true)
                    .build()
            )
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    // Your server's client ID, not your Android client ID.
                    .setServerClientId(getString(R.string.g01_web_client_id))
                    // Only show accounts previously used to sign in.
                    .setFilterByAuthorizedAccounts(true)
                    .build()
            )
            // Automatically sign in when exactly one credential is retrieved.
            .setAutoSelectEnabled(true)
            .build()

        /* Starting sign-in request */
        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener(this) { result ->
                try {
                    startIntentSenderForResult(
                        result.pendingIntent.intentSender, REQ_ONE_TAP,
                        null, 0, 0, 0, null
                    )
                } catch (e: IntentSender.SendIntentException) {
                    Log.e(ContentValues.TAG, "Couldn't start One Tap UI: ${e.localizedMessage}")
                }
            }
            .addOnFailureListener(this) { e ->
                // No saved credentials found. Launch the One Tap sign-up flow, or
                // do nothing and continue presenting the signed-out UI.

                /* Starting sign-up flow */
                Log.d(ContentValues.TAG, e.localizedMessage ?: "Starting sign-up flow")
                oneTapClient.beginSignIn(signUpRequest)
                    .addOnSuccessListener(this) { result ->
                        try {
                            Log.d(ContentValues.TAG, "Starting sign-up request")
                            startIntentSenderForResult(
                                result.pendingIntent.intentSender, REQ_ONE_TAP,
                                null, 0, 0, 0
                            )
                        } catch (e: IntentSender.SendIntentException) {
                            Log.e(
                                ContentValues.TAG,
                                "Couldn't start One Tap UI: ${e.localizedMessage}"
                            )
                        }
                    }
                    .addOnFailureListener(this) { _ ->
                        // No Google Accounts found. Just continue presenting the signed-out UI.
                        // Signed-out UI is unwanted so the app is closed
                        Toast.makeText(
                            this,
                            "Google account is necessary to use the app. Add at least one to your device",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.d(ContentValues.TAG, e.localizedMessage ?: "No google accounts found")
                    }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        auth = Firebase.auth

        when (requestCode) {
            REQ_ONE_TAP -> {
                try {
                    val credential = oneTapClient.getSignInCredentialFromIntent(data)
                    val idToken = credential.googleIdToken
                    val username = credential.id
                    when {
                        idToken != null -> {
                            // Got an ID token from Google. Use it to authenticate
                            // with firebase

                            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                            auth.signInWithCredential(firebaseCredential)
                                .addOnCompleteListener(this) { task ->
                                    if (task.isSuccessful) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d(ContentValues.TAG, "signInWithCredential:success")
                                        val user = auth.currentUser

                                        val i = Intent(applicationContext, MainActivity::class.java)
                                        //val b = Bundle()
                                        //b.putString
                                        //startActivity(i,b)
                                        startActivity(i)
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w(ContentValues.TAG, "signInWithCredential:failure", task.exception)
                                        //updateUI(null)
                                    }
                                }
                            Log.d(ContentValues.TAG, "Got ID token.")
                        }
                        else -> {
                            // Shouldn't happen.
                            Log.d(ContentValues.TAG, "No ID token or password!")
                        }
                    }
                } catch (e: ApiException) {
                    Toast.makeText(this,"Google account is necessary to use the app.",Toast.LENGTH_LONG).show()
                    Log.d(ContentValues.TAG, "Api exception thrown from activityResult: ${e.message}")
                }
            }
        }
    }
}