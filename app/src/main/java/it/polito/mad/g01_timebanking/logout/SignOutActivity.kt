package it.polito.mad.g01_timebanking.logout

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import it.polito.mad.g01_timebanking.MainActivity
import it.polito.mad.g01_timebanking.R
import it.polito.mad.g01_timebanking.login.SignInActivity


class SignOutActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_out)

        auth = Firebase.auth
        auth.signOut()

        val i = Intent(applicationContext, SignInActivity::class.java)
        i.putExtra("fromLogout", true)
        startActivity(i)
    }

}