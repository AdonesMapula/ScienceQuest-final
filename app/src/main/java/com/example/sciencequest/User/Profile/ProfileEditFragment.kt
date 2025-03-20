package com.example.sciencequest.User.Profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.sciencequest.R
import com.example.sciencequest.databinding.FragmentProfileEditBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProfileEditFragment : Fragment() {

    private lateinit var binding: FragmentProfileEditBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var userId: String? = null
    private var selectedAvatar: String? = null
    private var selectedAvatarView: ImageView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileEditBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference.child("Users")

        userId = auth.currentUser?.uid
        if (userId != null) {
            loadUserData()
        }

        setupAvatarSelection()

        binding.backButton.setOnClickListener {
            findNavController().navigate(R.id.userProfileFragment)
        }

        binding.saveChangesButton.setOnClickListener {
            saveChanges()
        }

        return binding.root
    }

    private fun loadUserData() {
        userId?.let { uid ->
            database.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val username = snapshot.child("username").getValue(String::class.java)
                        val firstName = snapshot.child("firstName").getValue(String::class.java)
                        val lastName = snapshot.child("lastName").getValue(String::class.java)
                        val email = snapshot.child("email").getValue(String::class.java)
                        selectedAvatar = snapshot.child("avatar").getValue(String::class.java)

                        binding.usernameEditText.setText(username)
                        binding.firstNameEditText.setText(firstName)
                        binding.lastNameEditText.setText(lastName)
                        binding.emailEditText.setText(email)

                        // Make email field read-only
                        binding.emailEditText.isEnabled = false

                        selectedAvatar?.let { avatar ->
                            selectAvatarByTag(avatar)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Error loading data", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun setupAvatarSelection() {
        val avatarMap = mapOf(
            R.id.avatar1 to "avatar1",
            R.id.avatar2 to "avatar2",
            R.id.avatar3 to "avatar3",
            R.id.avatar4 to "avatar4",
            R.id.avatar5 to "avatar5",
            R.id.avatar6 to "avatar6",
            R.id.avatar7 to "avatar7",
            R.id.avatar8 to "avatar8",
        )

        avatarMap.forEach { (viewId, avatarName) ->
            binding.root.findViewById<ImageView>(viewId).setOnClickListener {
                selectedAvatar = avatarName
                highlightSelectedAvatar(it as ImageView)
            }
        }
    }

    private fun highlightSelectedAvatar(selectedView: ImageView) {
        selectedAvatarView?.setBackgroundResource(0) // Remove highlight from previous selection
        selectedView.setBackgroundResource(R.drawable.avatar_selected_border) // Highlight new selection
        selectedAvatarView = selectedView
    }

    private fun selectAvatarByTag(avatar: String) {
        val avatarId = when (avatar) {
            "avatar1" -> R.id.avatar1
            "avatar2" -> R.id.avatar2
            "avatar3" -> R.id.avatar3
            "avatar4" -> R.id.avatar4
            "avatar5" -> R.id.avatar5
            "avatar6" -> R.id.avatar6
            "avatar7" -> R.id.avatar7
            "avatar8" -> R.id.avatar8
            else -> null
        }

        avatarId?.let {
            val avatarView = binding.root.findViewById<ImageView>(it)
            highlightSelectedAvatar(avatarView)
        }
    }

    private fun saveChanges() {
        val updatedUsername = binding.usernameEditText.text.toString()
        val updatedFirstName = binding.firstNameEditText.text.toString()
        val updatedLastName = binding.lastNameEditText.text.toString()

        if (userId != null) {
            val updates = mapOf(
                "username" to updatedUsername,
                "firstName" to updatedFirstName,
                "lastName" to updatedLastName,
                "avatar" to (selectedAvatar ?: ""),
                "role" to "admin", // Ensure role remains unchanged
                "streak" to 1, // Keep streak as per existing data
                "lessonProgress" to emptyMap<String, Any>() // Placeholder for lesson progress
            )

            database.child(userId!!).updateChildren(updates).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Profile updated!", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.userProfileFragment)
                } else {
                    Toast.makeText(requireContext(), "Update failed!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
