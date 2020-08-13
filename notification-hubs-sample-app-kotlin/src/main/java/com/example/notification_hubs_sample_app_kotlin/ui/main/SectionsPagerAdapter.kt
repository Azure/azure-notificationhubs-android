package com.example.notification_hubs_sample_app_kotlin.ui.main

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.notification_hubs_sample_app_kotlin.R

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class SectionsPagerAdapter(context: Context, fm: FragmentManager?) : FragmentPagerAdapter(fm!!) {
    private val mContext: Context = context
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> SetupFragment()
            1 -> NotificationListFragment()
            else -> PlaceholderFragment.newInstance(position + 1)
        }
    }

    override fun getCount(): Int {
        return 2;
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return mContext.resources.getString(TAB_TITLES[position])
    }

    companion object {
        @androidx.annotation.StringRes
        private val TAB_TITLES = intArrayOf(R.string.tab_text_1, R.string.tab_text_2)
    }

}