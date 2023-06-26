package com.example.tfgandroid.helpers

import android.content.Context
import androidx.core.content.edit
import com.example.tfgandroid.database.models.Group
import java.text.SimpleDateFormat
import java.util.*

class SharedPreferencesHelper {
    companion object {

        const val spName = "SP"

        fun setUsername(context: Context, name: String) {
            context.getSharedPreferences(spName, Context.MODE_PRIVATE).edit {
                putString("publicUsername", name)
                apply()
            }
        }

        fun getUsername(context: Context): String {
            return context.getSharedPreferences(spName, Context.MODE_PRIVATE).getString("publicUsername", "") ?: ""
        }
    }
}