package it.polito.mad.g01_timebanking

import android.content.ContentValues.TAG
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.ui.*
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import it.polito.mad.g01_timebanking.UserKey.HASTOBEEMPTY
import it.polito.mad.g01_timebanking.databinding.ActivityMainBinding
import it.polito.mad.g01_timebanking.helpers.FileHelper
import it.polito.mad.g01_timebanking.ui.profile.ProfileViewModel
import it.polito.mad.g01_timebanking.ui.timeslotdetails.TimeSlotDetailsViewModel
import it.polito.mad.g01_timebanking.ui.timeslotlist.TimeSlotListFragment
import it.polito.mad.g01_timebanking.ui.timeslotlist.TimeSlotListViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private lateinit var signUpRequest: BeginSignInRequest

    private val REQ_ONE_TAP = 2
    private var showOneTapUI = true

    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Activity creates the VMs that will be used by fragments
        val detailsVM = ViewModelProvider(this)[TimeSlotDetailsViewModel::class.java]
        val listVM = ViewModelProvider(this)[TimeSlotListViewModel::class.java]
        val profileVM = ViewModelProvider(this)[ProfileViewModel::class.java]

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        // Get views from navigation drawer
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        val navHeader = navView.getHeaderView(0)

        // Get fab view to set listener
        val fab = findViewById<FloatingActionButton>(R.id.fab)

        fab.setOnClickListener {
            detailsVM.prepareNewAdvertisement(listVM.count())
            navController.navigate(
                R.id.action_nav_your_offers_to_nav_edit_time_slot,
                bundleOf(HASTOBEEMPTY to true)
            )
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show()
        }

        // This code hides the FAB when it is NOT showing the TimeSlotListFragment
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when ((destination as FragmentNavigator.Destination).className) {
                // show fab in recipe fragment
                TimeSlotListFragment::class.qualifiedName -> {
                    fab.visibility = View.VISIBLE
                }
                // hide on other fragments
                else -> {
                    fab.visibility = View.GONE
                }
            }

        }

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_show_profile, R.id.nav_your_offers
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        /* This code fetch user info to be shown on navigation bar */
        val nameProfileTextView = navHeader.findViewById<TextView>(R.id.profileNameTextView)
        val emailProfileTextView = navHeader.findViewById<TextView>(R.id.profileEmailTextView)
        val profilePicture = navHeader.findViewById<ImageView>(R.id.navHeaderProfilePicture)

        profileVM.user.observe(this) {
            if (it.profilePicturePath != UserKey.PROFILE_PICTURE_PATH_PLACEHOLDER) {
                FileHelper.readImage(it.profilePicturePath, profilePicture)
            }
            nameProfileTextView.text = it.fullName
            emailProfileTextView.text = it.email
        }

        navController.addOnDestinationChangedListener{ _, destination, _ ->
            if (destination.id==R.id.nav_edit_profile || destination.id==R.id.nav_edit_time_slot)
                findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar).navigationIcon= null
        }


        oneTapSignIn()
    }

    private fun oneTapSignIn() {
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
                    Log.e(TAG, "Couldn't start One Tap UI: ${e.localizedMessage}")
                }
            }
            .addOnFailureListener(this) { e ->
                // No saved credentials found. Launch the One Tap sign-up flow, or
                // do nothing and continue presenting the signed-out UI.

                /* Starting sign-up flow */
                Log.d(TAG, e.localizedMessage)
                oneTapClient.beginSignIn(signUpRequest)
                    .addOnSuccessListener(this) { result ->
                        try {
                            Log.d(TAG, "Starting sign-up request")
                            startIntentSenderForResult(
                                result.pendingIntent.intentSender, REQ_ONE_TAP,
                                null, 0, 0, 0
                            )
                        } catch (e: IntentSender.SendIntentException) {
                            Log.e(TAG, "Couldn't start One Tap UI: ${e.localizedMessage}")
                        }
                    }
                    .addOnFailureListener(this) { e ->
                        // No Google Accounts found. Just continue presenting the signed-out UI.
                        // Signed-out UI is unwanted so the app is closed
                        Toast.makeText(this,"Google account is necessary to use the app. Add at least one to your device",Toast.LENGTH_SHORT).show()
                        finish()
                        Log.d(TAG, e.localizedMessage)
                    }
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        /** Test **/
//        val navController = findNavController(R.id.nav_host_fragment_content_main)
//        val count = supportFragmentManager.backStackEntryCount
//
//        println("Contatore sta a: $count")
//        if (count == 0) {
//            return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
//        }
//        else onBackPressed()
//        return false
        /** Test **/


        //onBackPressed()
        //return false
        //This is the good way if fragments are opened from push notification

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
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
                    val password = credential.password
                    Log.d(TAG, "usernane: $username \t password: $password")
                    when {
                        idToken != null -> {
                            // Got an ID token from Google. Use it to authenticate
                            // with firebase

                            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                            auth.signInWithCredential(firebaseCredential)
                                .addOnCompleteListener(this) { task ->
                                    if (task.isSuccessful) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d(TAG, "signInWithCredential:success")
                                        val user = auth.currentUser
                                        //updateUI(user)
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w(TAG, "signInWithCredential:failure", task.exception)
                                        //updateUI(null)
                                    }
                                }

                            Log.d(TAG, "Got ID token.")
                        }
                        password != null -> {
                            // Got a saved username and password. Use them to authenticate
                            // with your backend.
                            Log.d(TAG, "Got password.")
                        }
                        else -> {
                            // Shouldn't happen.
                            Log.d(TAG, "No ID token or password!")
                        }
                    }
                } catch (e: ApiException) {
                    Toast.makeText(this,"Google account is necessary to use the app.",Toast.LENGTH_SHORT).show()
                    finish()
                    Log.d(TAG, "Api exception thrown from activityResult: ${e.message}")
                }
            }
        }
    }

}