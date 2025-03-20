package com.example.sciencequest

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.widget.RemoteViews
import com.example.sciencequest.Login.LoginFragment
import com.google.firebase.database.*

class MyAppWidgetProvider : AppWidgetProvider() {

    private lateinit var database: DatabaseReference
    private lateinit var sharedPreferences: SharedPreferences
    private var streakEventListener: ValueEventListener? = null

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d("MyAppWidgetProvider", "onUpdate triggered")
        database = FirebaseDatabase.getInstance().reference
        sharedPreferences = context.getSharedPreferences("userPrefs", Context.MODE_PRIVATE)

        // Fetch userId from SharedPreferences
        val userId = sharedPreferences.getString("userId", null)

        // Iterate through all widget IDs
        for (appWidgetId in appWidgetIds) {
            if (userId == null) {
                // Update widget for a logged-out user
                updateWidgetForLoggedOutUser(context, appWidgetManager, appWidgetId)
            } else {
                // Fetch streak and update widget for a logged-in user
                fetchAndUpdateStreak(context, appWidgetManager, appWidgetId, userId)
            }
        }
    }

    companion object {
        private lateinit var database: DatabaseReference

        fun updateWidgetForLoggedOutUser(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.my_app_widget_provider)
            views.setTextViewText(R.id.tv_widget_title, "Log in to track streak!")
            views.setTextViewText(R.id.tv_streak_count, "")
            views.setImageViewResource(R.id.widgetIcon, R.drawable.einstein_model)
            views.setInt(R.id.widgetLayout, "setBackgroundResource", R.drawable.widget_background)

            // Add PendingIntent to LoginFragment
            val intent = Intent(context, LoginFragment::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widgetLayout, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun fetchAndUpdateStreak(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            userId: String
        ) {
            // Initialize the database property
            database = FirebaseDatabase.getInstance().reference

            val streakRef = database.child("Users").child(userId).child("streak")
            streakRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val streak = snapshot.getValue(Int::class.java) ?: 0
                    Log.d("MyAppWidgetProvider", "Fetched streak: $streak")
                    updateAppWidget(context, appWidgetManager, appWidgetId, streak)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("MyAppWidgetProvider", "Failed to fetch streak: ${error.message}")
                }
            })

            // Set up a listener for real-time updates
            streakRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val streak = snapshot.getValue(Int::class.java) ?: 0
                    Log.d("MyAppWidgetProvider", "Real-time update: Fetched streak: $streak")
                    updateAppWidget(context, appWidgetManager, appWidgetId, streak)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("MyAppWidgetProvider", "Failed to fetch streak: ${error.message}")
                }
            })
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        Log.d("MyAppWidgetProvider", "Widget enabled")
        scheduleWidgetUpdate(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        Log.d("MyAppWidgetProvider", "Widget disabled")
        cancelWidgetUpdate(context)

        // Remove the listener to avoid memory leaks
        streakEventListener?.let {
            database.removeEventListener(it)
        }
    }

    private fun scheduleWidgetUpdate(context: Context) {
        val intent = Intent(context, MyAppWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intervalMillis = 1800000L // 30 minutes

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + intervalMillis,
            intervalMillis,
            pendingIntent
        )
    }

    private fun cancelWidgetUpdate(context: Context) {
        val intent = Intent(context, MyAppWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
    streak: Int?
) {
    val views = RemoteViews(context.packageName, R.layout.my_app_widget_provider)


    // Update widget layout and text based on streak
    if (streak == null) {
        views.setTextViewText(R.id.tv_widget_title, "Please login to track your streaks")
        views.setTextViewText(R.id.tv_streak_count, "")
        views.setImageViewResource(R.id.widgetIcon, R.drawable.einstein_model)
        views.setInt(R.id.widgetLayout, "setBackgroundResource", R.drawable.widget_background)
    } else {
        views.setTextViewText(R.id.tv_streak_count, "$streak")
        when {
            streak == 0 -> {
                views.setTextViewText(R.id.tv_widget_title, "Ready to start your quest?")
                views.setImageViewResource(R.id.widgetIcon, R.drawable.einstein_model)
                views.setInt(R.id.widgetLayout, "setBackgroundResource", R.drawable.widget_background)
            }
            streak == 1 -> {
                views.setTextViewText(R.id.tv_streak_count, "$streak")
                views.setTextViewText(R.id.tv_widget_title, "Keep going!")
                views.setImageViewResource(R.id.widgetIcon, R.drawable.shuttle)
                views.setInt(R.id.widgetLayout, "setBackgroundResource", R.drawable.app_streak_background_5)
            }
            streak in 2..5 -> {
                views.setTextViewText(R.id.tv_widget_title, "Keep going!")
                views.setImageViewResource(R.id.widgetIcon, R.drawable.shuttle)
                views.setInt(R.id.widgetLayout, "setBackgroundResource", R.drawable.app_streak_background_5)
            }
            streak in 6..10 -> {
                views.setTextViewText(R.id.tv_widget_title, "Almost there!")
                views.setImageViewResource(R.id.widgetIcon, R.drawable.medal)
                views.setInt(R.id.widgetLayout, "setBackgroundResource", R.drawable.app_streak_background_10)
            }
            streak > 10 -> {
                views.setTextViewText(R.id.tv_widget_title, "Streak Master!")
                views.setImageViewResource(R.id.widgetIcon, R.drawable.trophy)
                views.setInt(R.id.widgetLayout, "setBackgroundResource", R.drawable.app_streak_background_11up)
            }
        }
    }

    // Add PendingIntent to MainActivity
    val intent = Intent(context, MainActivity::class.java)
    val pendingIntent = PendingIntent.getActivity(
        context, 0, intent, PendingIntent.FLAG_IMMUTABLE
    )
    views.setOnClickPendingIntent(R.id.widgetLayout, pendingIntent)

    // Update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}
