package com.sparkout.chat.ui.media.view

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.sparkout.chat.R
import com.sparkout.chat.databinding.ActivityMediaBinding
import com.sparkout.chat.ui.media.view.fragment.PhotoFragment
import com.sparkout.chat.ui.media.view.fragment.VideoFragment

class MediaActivity : AppCompatActivity() {
    private var mToId: String = ""
    private var mChatType: String = ""
    private lateinit var binding: ActivityMediaBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMediaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolBar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_arrow_back)

        binding.textviewName.text = intent.getStringExtra("name")
        mChatType = intent.getStringExtra("chat_type")!!

        mToId = intent.getStringExtra("toId")!!

        binding.tabMedia.setupWithViewPager(binding.viewPager)
        setupViewPager(binding.viewPager)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return true
    }

    private fun setupViewPager(viewPager: ViewPager) {
        val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager, mToId, mChatType)
        viewPager.adapter = viewPagerAdapter
    }

    class ViewPagerAdapter(fm: FragmentManager?, var mToId: String, private val mChatType: String) :
        FragmentStatePagerAdapter(fm!!) {
        var titles =
            arrayOf("Photos", "Videos")

        override fun getPageTitle(position: Int): CharSequence? {
            return titles[position]
        }

        override fun getItem(position: Int): Fragment {
            val photosFragment = PhotoFragment(mToId, mChatType)
            val videoFragment = VideoFragment(mToId, mChatType)
            //            val documentFragment = DocumentFragment(mToId)
            when (position) {
                0 -> return photosFragment
                1 -> return videoFragment
                //                2 -> return documentFragment
            }
            return photosFragment
        }

        override fun getCount(): Int {
            return titles.size //This one is important too
        }
    }
}
