package com.flaxstudio.drawon.adapters

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.flaxstudio.drawon.fragments.*
import com.flaxstudio.drawon.utils.FragmentType

class HomePagerAdapter(fragment : Fragment) : FragmentStateAdapter(fragment) {


    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {

       return when(position){
           0 -> HomeFragmentAll(FragmentType.Today)
           1 -> HomeFragmentAll(FragmentType.Week)
           2 -> HomeFragmentAll(FragmentType.Month)
           else -> HomeFragmentAll(FragmentType.All)
       }
    }

}