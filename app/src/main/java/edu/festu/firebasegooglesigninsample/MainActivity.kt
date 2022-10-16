package edu.festu.firebasegooglesigninsample

import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import edu.festu.firebasegooglesigninsample.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private val REQ_ONE_TAP = 2 // Can be any integer unique to the Activity.

    private lateinit var  oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private lateinit var binding: ActivityMainBinding
    private val showOneTapUI = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        oneTapClient = Identity.getSignInClient(this);

        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    // Your server's client ID, not your Android client ID.
                    .setServerClientId("327017658790-u30puodvi8207sd8t54uvje80a40cac8.apps.googleusercontent.com")
                    // Only show accounts previously used to sign in.
                    .setFilterByAuthorizedAccounts(true)
                    .build()
            )
            .build()
        binding.button.setOnClickListener {
            startActivity(Intent(this, GoogleSignInActivity::class.java))
//            oneTapClient.beginSignIn(signInRequest)
        }
        binding.oneTap.setOnClickListener {
            oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener(
                    this
                ) { result ->
                    try {
                        Log.d(TAG, "MAY BE SUCCESS")
                        startIntentSenderForResult(
                            result.pendingIntent.intentSender, REQ_ONE_TAP,
                            null, 0, 0, 0
                        )
                    } catch (e: SendIntentException) {
                        Log.e(TAG, "Couldn't start One Tap UI: " + e.localizedMessage)
                    }
                }
                .addOnFailureListener(this) { e -> // No saved credentials found. Launch the One Tap sign-up flow, or
                    // do nothing and continue presenting the signed-out UI.
                    Log.d(TAG, e.localizedMessage)
                }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQ_ONE_TAP)
            try {
                val credential = oneTapClient.getSignInCredentialFromIntent(data)
                val idToken = credential.googleIdToken
                when {
                    idToken != null -> {
                        // Got an ID token from Google. Use it to authenticate
                        // with Firebase.
                        Log.d(TAG, "Got ID token.")
                    }
                    else -> {
                        // Shouldn't happen.
                        Log.d(TAG, "No ID token!")
                    }
                }
            } catch (e: ApiException) {
                Log.d(TAG, e.message.toString())
            }
        else {
            Log.d(TAG, "SOMETHING WENT WRONG")
        }
        }
    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }
}