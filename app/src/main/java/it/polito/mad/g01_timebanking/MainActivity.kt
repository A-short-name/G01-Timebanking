package it.polito.mad.g01_timebanking

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.ui.*
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // https://firebase.google.com/docs/auth/android/google-signin#kotlin+ktx_1
        val signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    // Your server's client ID, not your Android client ID.
                    .setServerClientId(getString(R.string.g01_web_client_id))
                    // Only show accounts previously used to sign in.
                    .setFilterByAuthorizedAccounts(true)
                    .build())
            .build()


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
}