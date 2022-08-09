package com.gps.chat.ui.searchuser.view

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.gps.chat.R
import com.gps.chat.databinding.ActivitySearchUserBinding
import com.gps.chat.network.Resource
import com.gps.chat.ui.searchuser.model.SearchUserResponse
import com.gps.chat.ui.searchuser.adapter.SearchUserAdapter
import com.gps.chat.ui.searchuser.viewmodel.SearchUserViewModel
import com.gps.chat.utils.*
import com.sparkout.chat.common.ChatApp.Companion.mAppDatabase
import com.sparkout.chat.common.model.UserDetailsModel
import com.sparkout.chat.ui.chat.view.ChatActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchUserActivity : AppCompatActivity() {
    private val searchUserViewModel: SearchUserViewModel by viewModels()
    lateinit var searchUserAdapter: SearchUserAdapter
    var listSearchedUsers: ArrayList<UserDetailsModel> = ArrayList()
    private lateinit var binding: ActivitySearchUserBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back_arrow)
        binding.rvUsers.layoutManager = LinearLayoutManager(this)
        searchUserObserver()

        binding.etSearch.doAfterTextChanged { s ->
            if (s.toString().trim().isNotEmpty() && s.toString().length > 3) {
                binding.rvUsers.show()
                searchUser(s.toString().trim())
            } else {
                binding.rvUsers.hide()
            }
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }


    private fun searchUser(keyword: String) {
        searchUserViewModel.searchUser(keyword)
    }

    private fun searchUserObserver() {
        searchUserViewModel.searchUserResponse.observe(this) { response ->
            when (response.status) {
                Resource.Status.LOADING -> {
                }
                Resource.Status.SUCCESS -> {
                    updateUi(response)
                }
                Resource.Status.ERROR -> {
                    if (response.responseCode == 421) {
                        errorAuthenticationLogout(this)
                    } else {
                        response.message?.let {
                            snackBar(
                                this,
                                binding.layoutParent,
                                it
                            )

                        }
                    }

                }
            }
        }

    }

    private fun updateUi(response: Resource<SearchUserResponse>) {
        response.data?.let {
            if (it.status) {
                listSearchedUsers.clear()
                listSearchedUsers = it.data
                searchUserAdapter =
                    SearchUserAdapter(this@SearchUserActivity, listSearchedUsers) {
                        itemClickFunction(it)
                    }
                binding.rvUsers.adapter = searchUserAdapter
            } else {
                snackBar(
                    this@SearchUserActivity,
                    binding.layoutParent,
                    it.message
                )
            }
        }
    }


    private fun itemClickFunction(data: UserDetailsModel) {
        if (listSearchedUsers.size > 0) {
            mAppDatabase?.let {
                val userDetails = it.getUserDetailsDao()
                    .getUserDetails(data.id)
                if (userDetails.isEmpty()) {
                    it.getUserDetailsDao()
                        .insert(data)
                } else {
                    it.getUserDetailsDao()
                        .update(data)
                }
            }

            startActivity(
                Intent(this@SearchUserActivity, ChatActivity::class.java)
                    .putExtra("id", data.id)
            )
            finish()
        }
    }
}
