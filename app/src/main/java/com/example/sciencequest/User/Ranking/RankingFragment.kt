package com.example.sciencequest.User.Ranking

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.Toast
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sciencequest.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RankingFragment : Fragment(R.layout.fragment_ranking) {

    private lateinit var rankingRecyclerView: RecyclerView
    private lateinit var rankingAdapter: RankingAdapter
    private val userList = mutableListOf<User>()
    private val database = FirebaseDatabase.getInstance().getReference("Users")
    private lateinit var categorySpinner: Spinner
    private lateinit var scoreRangeSlider: SeekBar
    private var podiumUsers: List<User> = emptyList()
    private var regularUsers: List<User> = emptyList()
    private var selectedCategory: String = "Total Score"
    private lateinit var userId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ranking, container, false)

        val sharedPreferences = requireContext().getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        userId = sharedPreferences.getString("userId", null).toString()

        rankingRecyclerView = view.findViewById(R.id.rankingRecyclerView)
        categorySpinner = view.findViewById(R.id.categorySpinner)
        scoreRangeSlider = view.findViewById(R.id.scoreRangeSlider)

        // Setup ranking RecyclerView
        rankingAdapter = RankingAdapter(regularUsers, selectedCategory)
        rankingRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        rankingRecyclerView.adapter = rankingAdapter

        // Setup category spinner
        val categories = listOf("Total Score", "Biology", "Chemistry", "Physics", "Math & Statistics", "Earth & Space")
//        val customAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
//        customAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        categorySpinner.adapter = customAdapter

        // Set the custom adapter to the spinner
        val customAdapter = CustomSpinnerAdapter(requireContext(), categories)
        categorySpinner.adapter = customAdapter

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedCategory = categories[position]
                fetchUserDataByCategory(selectedCategory)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Score range filtering
        scoreRangeSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                filterUserDataByScore(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        fetchUserDataByCategory(selectedCategory)

        // Ensure updateUserRank() is called after the views are fully initialized
        view.post {
            getCurrentUserScore { userScore ->
                updateUserRank(userScore)
            }
        }

        return view
    }

    private fun updateUserRank(userScore: Int) {
        view?.let {
            getUserFromDatabase { currentUser ->
                val rank = getUserRank(userScore, currentUser)
                val message = getRankingMessage(rank)

                val currentRankingTextView: TextView = it.findViewById(R.id.currentRanking)
                val rankingMessageTextView: TextView = it.findViewById(R.id.rankingMessage)

                currentRankingTextView.text = "#$rank"
                Log.d("Ranking", "Current User Ranking: $rank")

                rankingMessageTextView.text = message
            }
        }
    }

    private fun getCurrentUserScore(callback: (Int) -> Unit) {
        if (selectedCategory == "Total Score") {
            getUserTotalScore(callback)
        } else {
            getUserCategoryScore(callback)
        }
    }

    private fun getUserTotalScore(callback: (Int) -> Unit) {
        getUserFromDatabase { user ->
            callback(user.totalScore ?: 0)
        }
    }

    private fun getUserCategoryScore(callback: (Int) -> Unit) {
        getUserFromDatabase { user ->
            callback(user.categoryTotalScore?.get(selectedCategory) ?: 0)
        }
    }

    private fun getUserFromDatabase(callback: (User) -> Unit) {
        database.child(userId).get().addOnSuccessListener {
            val user = it.getValue(User::class.java) ?: User()
            callback(user)
        }.addOnFailureListener {
            Log.e("RankingFragment", "Error fetching user data", it)
        }
    }

    private fun getUserRank(userScore: Int, currentUser: User): Int {
        val sortedUsers = userList.sortedByDescending {
            when (selectedCategory) {
                "Total Score" -> it.totalScore ?: 0
                else -> it.categoryTotalScore?.get(selectedCategory) ?: 0
            }
        }

        Log.d("Ranking", "Sorted users: ${sortedUsers.map { it.username to it.totalScore }}")
        Log.d("Ranking", "Current User: ${currentUser.username}")

        return sortedUsers.indexOfFirst { user ->
            when (selectedCategory) {
                "Total Score" -> user.totalScore == userScore
                else -> user.categoryTotalScore?.get(selectedCategory) == userScore
            }
        } + 1
    }

    private fun getRankingMessage(rank: Int): String {
        val totalUsers = userList.size
        if (totalUsers == 0) return "No rankings available."

        val percentageBetter = ((totalUsers - rank).toDouble() / totalUsers.toDouble()) * 100
        return "You are doing better than ${percentageBetter.toInt()}% of players in $selectedCategory"
    }


    private fun filterUserDataByScore(minScore: Int) {
        val filteredList = userList.filter {
            when (selectedCategory) {
                "Total Score" -> it.totalScore ?: 0 >= minScore
                else -> it.categoryTotalScore?.get(selectedCategory) ?: 0 >= minScore
            }
        }

        val sortedList = filteredList.sortedByDescending {
            when (selectedCategory) {
                "Total Score" -> it.totalScore ?: 0
                else -> it.categoryTotalScore?.get(selectedCategory) ?: 0
            }
        }

        podiumUsers = sortedList.take(3)
        regularUsers = sortedList.drop(3)

        updatePodium()
        rankingAdapter.updateData(regularUsers, selectedCategory)
    }

    private fun updatePodium() {
        for (i in 0 until 3) {
            val user = podiumUsers.getOrNull(i)

            when (i) {
                0 -> {
                    val usernameTextView: TextView? = view?.findViewById(R.id.username1)
                    val scoreTextView: TextView? = view?.findViewById(R.id.score1)

                    usernameTextView?.text = user?.username ?: "N/A"
                    scoreTextView?.text = "${user?.totalScore ?: 0} pts."
                }
                1 -> {
                    val usernameTextView: TextView? = view?.findViewById(R.id.username2)
                    val scoreTextView: TextView? = view?.findViewById(R.id.score2)

                    usernameTextView?.text = user?.username ?: "N/A"
                    scoreTextView?.text = "${user?.totalScore ?: 0} pts."
                }
                2 -> {
                    val usernameTextView: TextView? = view?.findViewById(R.id.username3)
                    val scoreTextView: TextView? = view?.findViewById(R.id.score3)

                    usernameTextView?.text = user?.username ?: "N/A"
                    scoreTextView?.text = "${user?.totalScore ?: 0} pts."
                }
            }
        }
    }

    private fun fetchUserDataByCategory(selectedCategory: String) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()

                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(User::class.java)
                    if (user != null) {
                        if (selectedCategory == "Total Score" && user.totalScore != null) {
                            userList.add(user)
                        } else if (user.categoryTotalScore?.containsKey(selectedCategory) == true) {
                            userList.add(user)
                        }
                    }
                }

                userList.sortByDescending {
                    when (selectedCategory) {
                        "Total Score" -> it.totalScore ?: 0
                        else -> it.categoryTotalScore?.get(selectedCategory) ?: 0
                    }
                }

                podiumUsers = userList.take(3)
                regularUsers = userList.drop(3)

                updatePodium()
                rankingAdapter.updateData(regularUsers, selectedCategory)

                getCurrentUserScore { userScore ->
                    updateUserRank(userScore)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load ranking.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
