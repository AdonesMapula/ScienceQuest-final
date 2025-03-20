package com.example.sciencequest.Login

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.sciencequest.R
import com.example.sciencequest.databinding.FragmentSignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class SignupFragment : Fragment() {

    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)

        binding.tologin.setOnClickListener {
            val action = SignupFragmentDirections.actionSignupFragmentToLoginFragment()
            findNavController().navigate(action)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth
        database = FirebaseDatabase.getInstance().reference

        // Add a TextWatcher to the password EditText
        binding.passwordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                validatePassword(charSequence.toString())  // Validate password as user types
            }

            override fun afterTextChanged(editable: Editable?) {}
        })


        binding.signupButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val username = binding.usernamesignup.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()
            val confirmPassword = binding.confirmPasswordEditText.text.toString().trim()

            if (!validateForm(email, password, confirmPassword, username)) return@setOnClickListener

            // Check for duplicate email
            database.child("Users").orderByChild("email").equalTo(email).get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        if (task.result.exists()) {
                            // Email already exists
                            Toast.makeText(
                                context,
                                "This email is already registered. Please log in or use a different email.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            // Proceed with user creation
                            auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener { authTask ->
                                    if (authTask.isSuccessful) {
                                        val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                                        val user = User(userId, username, email, "user")

                                        // Save user details in the database
                                        database.child("Users").child(userId).setValue(user)
                                            .addOnCompleteListener { dbTask ->
                                                if (dbTask.isSuccessful) {
                                                    // Send email verification
                                                    auth.currentUser?.sendEmailVerification()
                                                        ?.addOnCompleteListener { emailTask ->
                                                            if (emailTask.isSuccessful) {
                                                                Toast.makeText(
                                                                    context,
                                                                    "Account created! Please verify your email before logging in.",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()

                                                                // After successful signup, navigate to login screen
                                                                findNavController().navigate(
                                                                    SignupFragmentDirections.actionSignupFragmentToLoginFragment()
                                                                )
                                                            } else {
                                                                Toast.makeText(
                                                                    context,
                                                                    "Failed to send verification email: ${emailTask.exception?.message}",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                            }
                                                        }
                                                } else {
                                                    Toast.makeText(
                                                        context,
                                                        "Database error: ${dbTask.exception?.message}",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Authentication error: ${authTask.exception?.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                        }
                    } else {
                        Toast.makeText(
                            context,
                            "Failed to check existing emails: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()

                        Log.e("SignupFragment", "${task.exception?.message}")
                    }
                }
        }
    }

    private fun validatePassword(password: String) {
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasLowerCase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasMinLength = password.length >= 8

        // Update visibility and color of the requirements based on password input
        updateRequirement(binding.requirementUppercase, hasUpperCase)
        updateRequirement(binding.requirementLowercase, hasLowerCase)
        updateRequirement(binding.requirementDigit, hasDigit)
        updateRequirement(binding.requirementLength, hasMinLength)
    }

    private fun updateRequirement(requirement: TextView, isValid: Boolean) {
        if (isValid) {
            requirement.setTextColor(resources.getColor(R.color.colorPrimary)) // Green for valid
        } else {
            requirement.setTextColor(resources.getColor(R.color.red)) // Red for invalid
        }
    }

    private fun validateForm(email: String, password: String, confirmPassword: String, username: String): Boolean {
        var isValid = true

        if (username.isEmpty()) {
            binding.nameLayout.error = "Username is required"
            isValid = false
        } else {
            binding.nameLayout.error = null
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailLayout.error = "Valid email is required"
            isValid = false
        } else {
            binding.emailLayout.error = null
        }

        if (password.isEmpty()) {
            binding.passwordError.error = "Password must not be empty"
            isValid = false
        } else {
            binding.passwordError.error = null
        }

        if (password != confirmPassword) {
            binding.confirmPasswordError.error = "Passwords do not match"
            isValid = false
        } else {
            binding.confirmPasswordError.error = null
        }

        if (!binding.checkbox.isChecked) {
            binding.errorMessage.visibility = View.VISIBLE
            binding.errorMessage.text = "You have to agree with the terms and conditions"
            isValid = false
        } else {
            binding.errorMessage.visibility = View.GONE
            binding.errorMessage.text = null
        }

        return isValid
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
