package com.flaxstudio.drawon.utils

import android.os.Build
import org.ocpsoft.prettytime.PrettyTime
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
        val prettyTime = PrettyTime()
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val savedDateTime = LocalDateTime.parse(
                dateTimeString,
                DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
            )
            return prettyTime.format(savedDateTime) ?: ""

        }else{
            val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
            val savedDateTime = formatter.parse(dateTimeString)
            return prettyTime.format(savedDateTime) ?: ""
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