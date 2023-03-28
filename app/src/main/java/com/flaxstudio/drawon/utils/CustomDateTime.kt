package com.flaxstudio.drawon.utils

import android.os.Build
import org.ocpsoft.prettytime.PrettyTime
import org.ocpsoft.prettytime.units.Millisecond
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

class CustomDateTime {


    fun getDateTimeString(): String {

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
            current.format(formatter)
        }else{
            val calendar = Calendar.getInstance()
            val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
            simpleDateFormat.format(calendar.time)
        }
    }

    fun getDateTimeInWord(dateTimeString: String): String {

        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val now = LocalDateTime.now()
            val savedDateTime = LocalDateTime.parse(
                dateTimeString,
                DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
            )
            val deltaSeconds = now.toEpochSecond(ZoneOffset.UTC) - savedDateTime.toEpochSecond(ZoneOffset.UTC)
            getTimeAgo(deltaSeconds)

        }else{
            val now = Calendar.getInstance()
            val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
            val savedDateTime = formatter.parse(dateTimeString)
            val deltaSeconds = (now.time.time - savedDateTime!!.time) / 1000
           getTimeAgo(deltaSeconds)
        }
    }

    private fun getTimeAgo(diffSecond: Long): String {

        val minutes = diffSecond / 60
        val hours = minutes / 60
        val days = hours / 24
        val weeks = days / 7
        val months = days / 30
        val years = days / 365

        return when {
            years > 0 -> "$years years ago"
            months > 0 -> "$months months ago"
            weeks > 0 -> "$weeks weeks ago"
            days > 0 -> "$days days ago"
            hours > 0 -> "$hours hours ago"
            minutes > 0 -> "$minutes minutes ago"
            else -> "just now"
        }
    }

    fun parseTimeString(dateTime: String): String{
        return dateTime.substringAfter(' ')
    }

    private val secondsInDay = 24 * 60 * 60             // seconds in one day
    private val secondsInWeek = secondsInDay * 7        // seconds in one week

    fun getDateWithin(dateTimeString: String): DateWithin{

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val now = LocalDateTime.now()
            val savedDateTime = LocalDateTime.parse(dateTimeString,
                DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"))
            val deltaSeconds = now.toEpochSecond(ZoneOffset.UTC) - savedDateTime.toEpochSecond(ZoneOffset.UTC)

            return if(deltaSeconds < secondsInDay){
                DateWithin.Today
            }else if(deltaSeconds < secondsInWeek){
                DateWithin.Week
            }else{
                DateWithin.Month
            }

        }else{
            val now = Calendar.getInstance()
            val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
            val savedDateTime = formatter.parse(dateTimeString)
            val deltaSeconds = (now.time.time - savedDateTime!!.time) / 1000

            return if(deltaSeconds < secondsInDay){
                DateWithin.Today
            }else if(deltaSeconds < secondsInWeek){
                DateWithin.Week
            }else{
                DateWithin.Month
            }

        }
    }

    enum class DateWithin{
        Today,
        Week,
        Month
    }
}