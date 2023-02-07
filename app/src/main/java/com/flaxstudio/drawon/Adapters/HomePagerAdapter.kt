package com.flaxstudio.drawon.Adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.flaxstudio.drawon.fragments.*

class HomePagerAdapter(fragment : Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
       return when(position){
           0 -> HomeFragmentAll()
           1 -> HomeFragmentToday()
           2 -> HomeFragmentWeek()
           else -> HomeFragmentMonth()
       }
    }
}