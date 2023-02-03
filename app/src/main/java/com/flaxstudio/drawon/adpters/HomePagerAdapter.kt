package com.flaxstudio.drawon.adpters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.flaxstudio.drawon.fragments.*
import com.flaxstudio.drawon.fragments.FragmentHomeToday

class HomePagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0-> FragmentHomeAll()
            1-> FragmentHomeToday()
            2-> FragmentHomeWeek()
            else -> FragmentHomeMonth()
        }
    }
}