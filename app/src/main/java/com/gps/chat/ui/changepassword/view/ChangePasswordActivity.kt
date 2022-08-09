package com.gps.chat.ui.changepassword.view

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.gps.chat.R
import com.gps.chat.databinding.ActivityChangePasswordBinding
import com.gps.chat.network.Resource
import com.gps.chat.ui.changepassword.model.ChangePasswordRequest
import com.gps.chat.ui.changepassword.model.ChangePasswordResponse
import com.gps.chat.ui.changepassword.viewmodel.ChangePasswordViewModel
import com.gps.chat.utils.*
import com.gps.chat.utils.CONSTANTS.PASSWORD
import com.gps.chat.utils.CONSTANTS.USER_ID
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus

@AndroidEntryPoint
class ChangePasswordActivity : AppCompatActivity(), View.OnClickListener {
    private val changePasswordViewModel: ChangePasswordViewModel by viewModels()
    private lateinit var binding: ActivityChangePasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back_arrow)
        changePasswordObserver()

        changePasswordViewModel.isUserValid().observe(this) {
            if (it) {
                binding.textviewNext.alpha = 1f
                binding.textviewNext.isClickable = true
            } else {
                binding.textviewNext.alpha = .5f
                binding.textviewNext.isClickable = false
            }
        }

        binding.edittextPassword.setText(SharedPreferencesEditor.getData(PASSWORD))
        binding.textviewNext.setOnClickListener(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClick(view: View?) {
        view?.let {
            preventDoubleClick(it)
            hideKeyboard(this, it)
            when (it.id) {
                R.id.textview_next -> {
                    changePassword()
                }
            }

        }
    }

    private fun changePassword() {
        val changePasswordRequest = ChangePasswordRequest()
        changePasswordRequest.id = SharedPreferencesEditor.getData(USER_ID)
        changePasswordRequest.oldPassword = SharedPreferencesEditor.getData(PASSWORD)
        changePasswordRequest.newPassword = binding.edittextNewPassword.text.toString().trim()
        changePasswordViewModel.changePassword(changePasswordRequest)
    }

    private fun changePasswordObserver() {

        changePasswordViewModel.changePasswordResponse.observe(this) {
            when (it.status) {
                Resource.Status.LOADING -> {
                    showProgressDialog(this@ChangePasswordActivity)
                }
                Resource.Status.SUCCESS -> {
                    removeProgressDialog()
                    updateUi(it)
                }
                Resource.Status.ERROR -> {
                    removeProgressDialog()
                    it.message?.let {
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

    private fun updateUi(response: Resource<ChangePasswordResponse>) {
        response.data?.let {
            if (it.status) {
                showToast(this@ChangePasswordActivity, it.message)
                EventBus.getDefault().postSticky("password_changed")
                finish()
            } else {
                snackBar(
                    this@ChangePasswordActivity,
                    binding.layoutParent,
                    it.message
                )
            }

        }

    }
}

