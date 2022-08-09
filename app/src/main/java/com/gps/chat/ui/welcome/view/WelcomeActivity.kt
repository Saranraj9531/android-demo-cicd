package com.gps.chat.ui.welcome.view

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.gps.chat.ui.welcome.model.response.CheckUserNameResponse
import com.gps.chat.ui.welcome.view.dialog.GenderFragmentDialog
import com.gps.chat.ui.welcome.viewmodel.CheckUserNameViewModel
import com.gps.chat.R
import com.gps.chat.databinding.ActivityWelcomeBinding
import com.gps.chat.network.Resource
import com.gps.chat.ui.login.view.LoginActivity
import com.gps.chat.ui.register.view.RegisterActivity
import com.gps.chat.utils.*
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class WelcomeActivity : AppCompatActivity(), View.OnClickListener {
    private var intYear = 0
    private var intMonth: Int = 0
    private var intDay: Int = 0
    private val checkUserNameViewModel: CheckUserNameViewModel by viewModels()
    private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back_arrow)
        checkUserNameObserver()

        binding.etUserName.doAfterTextChanged {
            if (it.toString().length > 3) {
                checkUserNameViewModel.mUserName = it.toString()
                checkUserNameViewModel.checkUserName(it.toString())
            }
        }
        checkUserNameViewModel.isUserValid().observe(this) {
            if (it) {
                binding.tvNext.alpha = 1f
                binding.tvNext.isClickable = true
            } else {
                binding.tvNext.alpha = .5f
                binding.tvNext.isClickable = false
            }
        }

        binding.etGender.isFocusable = false
        binding.etDob.isFocusable = false
        binding.etGender.setOnClickListener(this)
        binding.etDob.setOnClickListener(this)
        binding.tvLogin.setOnClickListener(this)
        binding.tvNext.setOnClickListener(this)
    }

    private fun checkUserNameObserver() {
        checkUserNameViewModel.checkUserNameResponse.observe(this) { response ->
            when (response.status) {
                Resource.Status.LOADING -> {
                }
                Resource.Status.SUCCESS -> {
                    updateUi(response)
                }
                Resource.Status.ERROR -> {
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

    private fun updateUi(response: Resource<CheckUserNameResponse>) {
        response.data?.let {
            if (it.status) {
                if (it.data?.available == 1) {
                    binding.tvStatus.hide()
                } else {
                    binding.tvStatus.show()
                }
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

    override fun onClick(view: View?) {
        view?.let {
            hideKeyboard(this, it)
            preventDoubleClick(it)
            when (it.id) {
                R.id.et_gender -> {
                    val genderFragmentDialog = GenderFragmentDialog(this)
                    genderFragmentDialog.show(supportFragmentManager, "gender")
                }
                R.id.et_dob -> {
                    datePicker()
                }
                R.id.tv_login -> {
                    startActivity(Intent(this, LoginActivity::class.java))
                }
                R.id.tv_next -> {
                    if (binding.tvStatus.visibility == View.VISIBLE) {
                        snackBar(
                            this,
                            binding.layoutParent,
                            resources.getString(R.string.error_username)
                        )
                    } else {
                        startActivity(
                            Intent(this, RegisterActivity::class.java)
                                .putExtra("username", binding.etUserName.text.toString().trim())
                                .putExtra("gender", binding.etGender.text.toString().trim())
                                .putExtra("dob", binding.etDob.text.toString().trim())
                        )
                    }

                }
            }
        }

    }

    fun setGender(stringGender: String) {
        binding.etGender.setText(stringGender)
        checkUserNameViewModel.mGender = stringGender
    }

    private fun datePicker() {
        // Process to get Current Date
        val c = Calendar.getInstance()
        intYear = c[Calendar.YEAR]
        intMonth = c[Calendar.MONTH]
        intDay = c[Calendar.DAY_OF_MONTH]
        // Launch Date Picker Dialog
        val dpd = DatePickerDialog(
            this, R.style.MyAppTheme,
            { _, year, monthOfYear, dayOfMonth -> // Display Selected date in EditText
                binding.etDob.setText(
                    dayOfMonth.toString().plus("-")
                        .plus((monthOfYear + 1)).plus("-")
                        .plus(year)
                )
                checkUserNameViewModel.mDob = binding.etDob.text.toString()
            }, intYear, intMonth, intDay
        )
        dpd.datePicker.maxDate = System.currentTimeMillis()
        dpd.setCancelable(false)
        dpd.show()
    }


}
