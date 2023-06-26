package com.example.tfgandroid.helpers

import android.util.Log
import com.example.tfgandroid.database.models.Group
import java.text.SimpleDateFormat
import java.util.*

class DateHelper {
    companion object {
        fun getCurrentDateString(): String {
            val calendar = Calendar.getInstance().time
            val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
            return formatter.format(calendar)
        }

        fun getCurrentDateVerbose(): String
        {
            val calendar = Calendar.getInstance().time
            val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS", Locale.getDefault())
            return formatter.format(calendar)
        }

        fun hasTimePassed(dateToCompare: String, seconds: Int): Boolean
        {

            val calendar = Calendar.getInstance()
            val currentDate = calendar.time
            val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS", Locale.getDefault())
            calendar.time = formatter.parse(dateToCompare)!!
            calendar.add(Calendar.SECOND, 10)
            val lastDatePlusOffset = calendar.time
            Log.d("Testing", "${formatter.format(currentDate)} -- ${formatter.format(lastDatePlusOffset)}")
            return lastDatePlusOffset < currentDate
        }

    }
}