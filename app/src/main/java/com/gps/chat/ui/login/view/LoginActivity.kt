package com.gps.chat.ui.login.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import com.gps.chat.ui.login.model.response.LoginResponse
import com.gps.chat.ui.login.viewmodel.LoginViewModel
import com.gps.chat.R
import com.gps.chat.custom_dialog.Animation
import com.gps.chat.custom_dialog.CustomAlertDialog
import com.gps.chat.databinding.ActivityLoginBinding
import com.gps.chat.network.Resource
import com.gps.chat.ui.home.view.HomeActivity
import com.gps.chat.utils.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity() : AppCompatActivity(), View.OnClickListener {
    private val loginViewModel: LoginViewModel by viewModels()
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back_arrow)
        listenObservers()
        binding.tvLogin.setOnClickListener(this)
        binding.tvForgotPassword.setOnClickListener(this)

        binding.etUserName.doAfterTextChanged {
            loginViewModel.mUserName = it.toString()
        }
        binding.etPassword.doAfterTextChanged {
            loginViewModel.mPassword = it.toString()
        }

        loginViewModel.isUserValid().observe(this, Observer {
            if (it) {
                binding.tvLogin.alpha = 1f
                binding.tvLogin.isClickable = true
            } else {
                binding.tvLogin.alpha = .5f
                binding.tvLogin.isClickable = false
            }
        })


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
            hideKeyboard(this, it)
            preventDoubleClick(it)
            when (it.id) {
                R.id.tv_login -> {
                    login()
                }
                R.id.tv_forgot_password -> {
//                    startActivity(Intent(this, ForgotPasswordActivity::class.java))
                }
            }
        }

    }


    private fun login() {
        loginViewModel.login()
    }

    private fun loginNewSession() {
        loginViewModel.loginNewSession()
    }

    private fun listenObservers() {
        loginViewModel.loginResponse.observe(this, Observer {
            when (it.status) {
                Resource.Status.LOADING -> {
                    showProgressDialog(this@LoginActivity)
                }
                Resource.Status.SUCCESS -> {
                    removeProgressDialog()
                    updateUi(it)
                }
                Resource.Status.ERROR -> {
                    removeProgressDialog()
                    if (it.responseCode == 423) {
                        /* it.message?.let {
                             newSessionAlert(it)
                         }*/
                    } else {
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
        })

    }

    private fun updateUi(data: Resource<LoginResponse>) {
        data.data?.let {
            if (it.status) {
                showToast(this@LoginActivity, it.message)
                SharedPreferencesEditor.storeValueBoolean(CONSTANTS.IS_ADD, true)
                it.data?.let { userData ->
                    SharedPreferencesEditor.saveUserData(userData)
                }
                startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                finishAffinity()
            } else {
                snackBar(this@LoginActivity, binding.layoutParent, it.message)
            }
        }
    }

    /**TODO: create new session alert*/
    private fun newSessionAlert(body: LoginResponse) {
        body.data?.session?.let {

            CustomAlertDialog.Builder(this)
                .setTitle(getString(R.string.msg_alert))
                .setMessage(getString(R.string.msg_alert_content).plus(it.deviceModel))
                .setAnimation(Animation.SIDE)
                .setCancelable(false)
                .setPositiveButton(
                    getString(R.string.msg_yes)
                ) { loginNewSession() }
                .setNegativeButton(getString(R.string.msg_no), null)
                .build()
        }
    }
}
