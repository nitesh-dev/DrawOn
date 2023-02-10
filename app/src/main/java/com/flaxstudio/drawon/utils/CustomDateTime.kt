package com.flaxstudio.drawon.utils

import android.os.Build
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDateTime
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.*

class CustomDateTime {


    fun getDateTimeString(): String {

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
            current.format(formatter)
        }else{
            val calendar = Calendar.getInstance()
            val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
            simpleDateFormat.format(calendar.time)
        }
    }

    fun parseTimeString(dateTime: String): String{
        return dateTime.substringAfter(' ')
    }


    fun getDateWithin(dateTimeString: String): DateWithin{

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val now = LocalDateTime.now()
            val dateTime = LocalDateTime.parse(dateTimeString,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            val period = Period.between(dateTime.toLocalDate(), now.toLocalDate())

            if(period.months <= 1){
              // Date and time is within the last month
                return DateWithin.Month
            }

            if(period.days <= 7){
                // Date and time is within the week
                return DateWithin.Week
            }

            if(period.days <= 1){
                // Date and time is within the last day
                return DateWithin.Today
            }

            // for years
            return DateWithin.All
        }else{
            val now = Calendar.getInstance()
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val dateTime = formatter.parse(dateTimeString)
            val calendar = Calendar.getInstance()
            calendar.time = dateTime!!

            calendar.add(Calendar.MONTH, 1)
            if (now.before(calendar)) {
                // Date and time is within the last month
                return DateWithin.Month
            }

            calendar.time = dateTime
            calendar.add(Calendar.WEEK_OF_YEAR, 1)
            if (now.before(calendar)) {
                // Date and time is within the last week
                return DateWithin.Week
            }


            calendar.time = dateTime
            val startOfToday = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val endOfToday = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }

            if (calendar.after(startOfToday) && calendar.before(endOfToday)) {
                // Date and time is within the today
                return DateWithin.Today
            }

            return DateWithin.All
        }
    }

    enum class DateWithin{
        Today,
        Week,
        Month,
        All
    }
}