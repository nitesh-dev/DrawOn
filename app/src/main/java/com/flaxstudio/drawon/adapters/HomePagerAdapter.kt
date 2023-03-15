package com.flaxstudio.drawon.adapters

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.flaxstudio.drawon.fragments.*

class HomePagerAdapter(fragment : Fragment) : FragmentStateAdapter(fragment) {

//    val fragmentAll = HomeFragmentAll()
//    val fragmentToday = HomeFragmentToday()
//    val fragmentWeek = HomeFragmentWeek()
//    val fragmentMonth = HomeFragmentMonth()

    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {

        Log.e("+++++++", "hello")
       return when(position){
           0 -> HomeFragmentAll()
           1 -> HomeFragmentAll()
           2 -> HomeFragmentAll()
           else -> HomeFragmentAll()
       }
    }
}