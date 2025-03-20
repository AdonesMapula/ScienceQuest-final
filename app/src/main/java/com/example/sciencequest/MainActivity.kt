package com.example.sciencequest

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    var userId: String? = null
    private lateinit var streakListener: ValueEventListener
    private lateinit var userStreakRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge content
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT // Ensure status bar is transparent

        setContentView(R.layout.activity_main)

        // Adjust insets for the FragmentContainerView
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.nav_host_fragment)) { view, insets ->
            view.setPadding(0, 0, 0, 0) // No padding to extend behind status bar
            insets
        }

        sharedPreferences = getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        val isLoggedInFromLoginFragment = sharedPreferences.getBoolean("isLoggedInFromLoginFragment", false)

        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.loginFragment, true) // Clear the back stack up to the login fragment
            .build()

        // Set up NavController
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Set up Bottom Navigation with NavController for user and admin
        val bottomNavigationUser: BottomNavigationView = findViewById(R.id.bottom_navigation_user)
        val bottomNavigationAdmin: BottomNavigationView = findViewById(R.id.bottom_navigation_admin)
        bottomNavigationUser.setupWithNavController(navController)
        bottomNavigationAdmin.setupWithNavController(navController)
        bottomNavigationUser.setItemIconTintList(null);
        bottomNavigationAdmin.setItemIconTintList(null);


        // Handle visibility of BottomNavigationView based on destination
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.loginFragment || destination.id == R.id.forgotPasswordFragment || destination.id == R.id.signupFragment || destination.id == R.id.quizDetailFragment || destination.id == R.id.resultFragment) {
                bottomNavigationUser.visibility = View.GONE
                bottomNavigationAdmin.visibility = View.GONE
            } else {
                // Show the correct Bottom Navigation based on role
                val userRole = sharedPreferences.getString("userRole", "user") // Default to "user"
                if (userRole == "admin") {
                    bottomNavigationUser.visibility = View.GONE
                    bottomNavigationAdmin.visibility = View.VISIBLE
                } else {
                    bottomNavigationAdmin.visibility = View.GONE
                    bottomNavigationUser.visibility = View.VISIBLE
                }
            }
        }

        // Handle login flow using SharedPreferences
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        if (!isLoggedIn) {
            navController.navigate(R.id.loginFragment)
        } else {
            // Navigate based on user role (admin or user)
            val userRole = sharedPreferences.getString("userRole", "user")
            if (userRole == "admin") {
                navController.navigate(R.id.adminHomeFragment, null, navOptions)
            } else {
                navController.navigate(R.id.homeFragment, null, navOptions)
                userId = sharedPreferences.getString("userId", null)
                userId?.let {
                    setupFirebaseListener(it) // Set up Firebase listener once user is logged in
                }
            }
        }

    }






    // Set up the Firebase listener once the user is logged in
    private var isStreakBeingUpdated = false

    fun setupFirebaseListener(userId: String) {
        val database = FirebaseDatabase.getInstance()
        userStreakRef = database.getReference("Users").child(userId)

        // Remove existing listener before adding a new one
        if (::streakListener.isInitialized) {
            userStreakRef.removeEventListener(streakListener)
        }

        // Create a new listener
        streakListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("Firebase", "onDataChange called")
                if (isStreakBeingUpdated) return // Prevent update if already in progress

                isStreakBeingUpdated = true

                val streak = snapshot.child("streak").getValue(Int::class.java) ?: 0
                val lastLoginDate = snapshot.child("lastLoginDate").getValue(String::class.java)
                val currentDate = getCurrentDate()

                Log.d("Firebase", "Last login: $lastLoginDate, Current date: $currentDate, Current streak: $streak")

                if (lastLoginDate == null) {
                    // No last login date exists, set initial values
                    userStreakRef.child("lastLoginDate").setValue(currentDate)  // First update last login date
                    userStreakRef.child("streak").setValue(1)  // Set initial streak value
                    saveStreak(this@MainActivity, 1)
                } else {
                    if (isConsecutiveLogin(lastLoginDate, currentDate)) {
                        // Only increment if the login is consecutive
                        val newStreak = streak + 1
                        userStreakRef.child("lastLoginDate").setValue(currentDate)  // Update last login date after incrementing streak
                        userStreakRef.child("streak").setValue(newStreak)

                        saveStreak(this@MainActivity, newStreak)
                    } else if (lastLoginDate != currentDate) {
                        // If it's not consecutive and it's a new login day, reset streak
                        userStreakRef.child("lastLoginDate").setValue(currentDate)  // First update last login date
                        userStreakRef.child("streak").setValue(1)  // Reset streak to 1
                        saveStreak(this@MainActivity, 1)
                    }
                }

                Log.d("StreakTracker", "Updated streak: ${snapshot.child("streak").getValue(Int::class.java)}")

                isStreakBeingUpdated = false // Reset flag
            }


            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Error fetching streak data: ${error.message}")
                isStreakBeingUpdated = false // Reset flag on error
            }
        }

        // Attach the listener
        userStreakRef.addValueEventListener(streakListener)
    }


    // Remove listener when the user logs out
    fun removeStreakListener() {
        userStreakRef.removeEventListener(streakListener)
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())  // Return today's date in YYYY-MM-DD format
    }

    private fun isConsecutiveLogin(lastLoginDate: String, currentDate: String): Boolean {
        if (lastLoginDate.isEmpty()) return false

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        try {
            val lastLogin = dateFormat.parse(lastLoginDate)
            val current = dateFormat.parse(currentDate)

            // Check if the current date is exactly one day after the last login date
            val calendar = Calendar.getInstance()
            calendar.time = lastLogin
            calendar.add(Calendar.DAY_OF_YEAR, 1)

            // Compare the calendar's time with the current date
            return calendar.time == current
        } catch (e: ParseException) {
            e.printStackTrace()
            return false
        }
    }


    fun saveStreak(context: Context, streak: Int) {
        val sharedPreferences = context.getSharedPreferences("StreakPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("streak", streak)
        editor.apply()
    }



    // Function to hide the BottomNavigationView (bottom_navigation_user)
    fun hideBottomNavigation() {
        val bottomNavigationUser: BottomNavigationView = findViewById(R.id.bottom_navigation_user)
        bottomNavigationUser.visibility = View.GONE
    }

    // Function to show the BottomNavigationView (bottom_navigation_user)
    fun showBottomNavigation() {
        val bottomNavigationUser: BottomNavigationView = findViewById(R.id.bottom_navigation_user)
        bottomNavigationUser.visibility = View.VISIBLE
    }

    override fun onBackPressed() {
        val navController = findNavController(R.id.nav_host_fragment)
        if (!navController.popBackStack()) {
            // If the back stack is empty, exit the activity
            super.onBackPressed()
        }
    }

}
