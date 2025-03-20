package com.example.sciencequest.Login

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.sciencequest.MainActivity
import com.example.sciencequest.MyAppWidgetProvider
import com.example.sciencequest.R
import com.example.sciencequest.databinding.FragmentLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private val RC_SIGN_IN = 9001
    private val TAG = "LoginFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)


        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        if (user != null && user.isEmailVerified) {

                            // Get user details and store them in SharedPreferences
                            storeUserDetails(user)

//                            // Proceed to the next screen
//                            Toast.makeText(requireContext(), "Login successful", Toast.LENGTH_SHORT).show()
//                            val sharedPreferences = requireContext().getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
//                            sharedPreferences.edit().putBoolean("isLoggedIn", true).apply()
//                            findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                        } else {
                            // Email not verified
                            Toast.makeText(requireContext(), "Please verify your email before logging in.", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        binding.googleSignInButton.setOnClickListener {
//            val signInIntent = googleSignInClient.signInIntent
//            startActivityForResult(signInIntent, RC_SIGN_IN)
            findNavController().navigate(R.id.action_loginFragment_to_signupFragment)
        }

        binding.signupTextView.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signupFragment)
        }

        binding.forgotPasswordTextView.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.result
            if (account != null) {
                firebaseAuthWithGoogle(account.idToken!!)
            } else {
                Toast.makeText(requireContext(), "Google Sign-In failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Google Sign-In successful", Toast.LENGTH_SHORT).show()

                    // Get user details and store them in SharedPreferences
                    val user = auth.currentUser
                    if (user != null) {
                        storeUserDetails(user)
                    }

//                    // In your LoginFragment, after successful login:
//                    val sharedPreferences = requireContext().getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
//                    sharedPreferences.edit().putBoolean("isLoggedIn", true).apply()
//
//                    findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                } else {
                    Toast.makeText(requireContext(), "Authentication Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun storeUserDetails(user: FirebaseUser){
        // Fetch username from Realtime Database using user UID
        val database = FirebaseDatabase.getInstance()
        val userRef = database.getReference("Users").child(user.uid)

        userRef.get().addOnSuccessListener { snapshot ->
            // Check if the username exists in the database
            val username = snapshot.child("username").getValue(String::class.java)
            val role = snapshot.child("role").getValue(String::class.java)

            if (username != null) {
                // Store user details in SharedPreferences
                val sharedPreferences = requireContext().getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
                sharedPreferences.edit().apply {
                    putString("userId", user.uid)
                    putString("userEmail", user.email)
                    putString("userDisplayName", username)
                    putString("userRole", role)

                    apply()
                }

                if (user.isEmailVerified) {
                    // Proceed to the next screen
                    Toast.makeText(requireContext(), "Login successful", Toast.LENGTH_SHORT).show()
                    val sharedPreferences = requireContext().getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
                    sharedPreferences.edit().putBoolean("isLoggedIn", true).apply()

                    // Remove the streak listener from MainActivity
                    (activity as? MainActivity)?.setupFirebaseListener(user.uid)

                    // Update the widget to reflect the proper number of streaks
                    val appWidgetManager = AppWidgetManager.getInstance(requireContext())
                    val appWidgetIds = appWidgetManager.getAppWidgetIds(
                        ComponentName(requireContext(), MyAppWidgetProvider::class.java)
                    )
                    for (appWidgetId in appWidgetIds) {
                        MyAppWidgetProvider.Companion.fetchAndUpdateStreak(
                            requireContext(),
                            appWidgetManager,
                            appWidgetId,
                            user.uid
                        )
                    }

                    if (role == "admin") {
                        findNavController().navigate(R.id.action_loginFragment_to_adminHomeFragment)
                    } else {
                        findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                    }
                } else {
                    // Email not verified
                    Toast.makeText(requireContext(), "Please verify your email before logging in.", Toast.LENGTH_LONG).show()
                }
            } else {
                // Handle case where username is not found
                Toast.makeText(requireContext(), "Username not found in database", Toast.LENGTH_LONG).show()
            }
        }.addOnFailureListener { exception ->
            Log.e(TAG, "Error fetching user data: ${exception.message}")
            Toast.makeText(requireContext(), "Error fetching user details", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
