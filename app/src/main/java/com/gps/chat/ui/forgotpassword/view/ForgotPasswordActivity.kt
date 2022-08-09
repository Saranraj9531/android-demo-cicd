package com.gps.chat.ui.forgotpassword.view

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.gps.chat.ui.forgotpassword.model.request.ForgotPasswordRequest
import com.gps.chat.ui.forgotpassword.viewmodel.ForgotPasswordViewModel
import com.gps.chat.R
import com.gps.chat.crop.CropImage
import com.gps.chat.crop.CropImageView
import com.gps.chat.databinding.ActivityForgotPasswordBinding
import com.gps.chat.network.Resource
import com.gps.chat.ui.forgotpassword.model.response.ForgotPasswordResponse
import com.gps.chat.ui.register.viewmodel.UploadViewModel
import com.gps.chat.utils.*
import com.gps.chat.utils.CONSTANTS.CAMERA_REQUEST_CODE
import com.gps.chat.utils.CONSTANTS.IS_ADD
import com.gps.chat.utils.CONSTANTS.STORAGE_REQUEST_CODE
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.net.URLConnection

@AndroidEntryPoint
class ForgotPasswordActivity : AppCompatActivity(), View.OnClickListener {
    var passportUri: Uri? = null
    private var passportFile: File? = null
    var strPassportIdUrl: String = ""
    var mimeType: String = ""
    private val uploadViewModel: UploadViewModel by viewModels()
    private val forgotPasswordViewModel: ForgotPasswordViewModel by viewModels()
    private lateinit var binding: ActivityForgotPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back_arrow)
        forgotPasswordObserver()
        uploadImageObserver()

        binding.layoutPassportId.setOnClickListener(this)
        binding.cardPassportId.setOnClickListener(this)
        binding.tvNext.setOnClickListener(this)
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
            preventDoubleClick(view)
            hideKeyboard(this, view)
            when (view.id) {
                R.id.layout_passport_id -> {
                    selectImage()
                }
                R.id.card_passport_id -> {
                    selectImage()
                }
                R.id.tv_next -> {
                    forgotPassword()
                }
            }
        }

    }


    private fun uploadImageObserver() {
        uploadViewModel.uploadResponse.observe(this) { response ->
            when (response.status) {
                Resource.Status.LOADING -> {
                    showProgressDialog(this)
                }
                Resource.Status.SUCCESS -> {
                    removeProgressDialog()
                    updateUiImage(response)

                }
                Resource.Status.ERROR -> {
                    removeProgressDialog()
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

    private fun updateUiImage(response: Resource<CommonResponse>) {
        response.data?.let {
            if (it.status) {
                showToast(this, it.message)
                mimeType = ""
                passportFile = null
                passportUri = null
                it.data?.let {
                    strPassportIdUrl = it.url

                }

            } else {
                snackBar(
                    this,
                    binding.layoutParent,
                    it.message
                )
            }

        }
    }


    //TODO: fun to choose image
    private fun selectImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(this)
                } else {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                    ) {
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            STORAGE_REQUEST_CODE
                        )
                    } else {
                        snackBarAction(
                            this,
                            binding.layoutParent,
                            resources.getString(R.string.hint_permissions)
                        )
                    }
                }
            } else {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.CAMERA
                    )
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.CAMERA),
                        CAMERA_REQUEST_CODE
                    )
                } else {
                    snackBarAction(
                        this,
                        binding.layoutParent,
                        resources.getString(R.string.hint_permissions)
                    )
                }
            }
        } else {
            CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this)
        }
    }

    //TODO: onActivityResult to handle the result of cropped image
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                val result = CropImage.getActivityResult(data)
                if (resultCode == Activity.RESULT_OK) {
                    result?.uri?.let {
                        passportUri = it
                    }
                    if (passportUri != null) {
                        Glide.with(this)
                            .setDefaultRequestOptions(glideRequestOptionProfile())
                            .load(passportUri)
                            .into(binding.ivPassportId)
                        imageUpload()
                    }
                }
            }
        }
    }

    //TODO: api call upload image
    private fun imageUpload() {
        if (passportUri != null) {
            passportFile = FilePath.getPath(this, passportUri!!)
            mimeType = URLConnection.guessContentTypeFromName(passportFile!!.name)
        }
        if (passportFile != null) {
            val requestFile: RequestBody =
                passportFile!!.asRequestBody(mimeType.toMediaTypeOrNull())
            val body: MultipartBody.Part =
                MultipartBody.Part.createFormData(
                    "media", passportFile!!.name,
                    requestFile
                )
            uploadViewModel.uploadImage(body)
        }
    }

    //TODO: fun to call api to update the password
    private fun forgotPassword() {
        val forgotPasswordRequest = ForgotPasswordRequest()
        forgotPasswordRequest.username = binding.etUserName.text.toString().trim()
        forgotPasswordRequest.passportNumber = binding.etPassportNumber.text.toString().trim()
        forgotPasswordRequest.passportImage = strPassportIdUrl
        forgotPasswordViewModel.forgotPassword(forgotPasswordRequest)
    }

    //TODO: observer fun to listen the lifecycle of viewmodel
    private fun forgotPasswordObserver() {
        forgotPasswordViewModel.forgotPasswordResponse.observe(this) { response ->
            when (response.status) {
                Resource.Status.LOADING -> {
                    showProgressDialog(this)
                }
                Resource.Status.SUCCESS -> {
                    removeProgressDialog()
                    updateUi(response)
                }
                Resource.Status.ERROR -> {
                    removeProgressDialog()
                }


            }
        }

    }

    private fun updateUi(response: Resource<ForgotPasswordResponse>) {
        when (response.responseCode) {
            200 -> {
                response.data?.let {
                    if (it.status) {
                        showToast(
                            this@ForgotPasswordActivity,
                            it.message
                        )
                        SharedPreferencesEditor.storeValueBoolean(IS_ADD, true)
                        SharedPreferencesEditor.saveUserData(it.data)
                        /* startActivity(
                             Intent(
                                 this@ForgotPasswordActivity,
                                 HomeActivity::class.java
                             )
                         )*/
                        finishAffinity()
                    } else {
                        snackBar(
                            this@ForgotPasswordActivity,
                            binding.layoutParent,
                            it.message
                        )
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.isNotEmpty()) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            STORAGE_REQUEST_CODE
                        )
                    } else {
                        CropImage.activity()
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .start(this)
                    }
                } else {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.CAMERA
                        )
                    ) {
                        snackBarAction(
                            this,
                            binding.layoutParent,
                            resources.getString(R.string.hint_permissions)
                        )
                    } else {
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.CAMERA),
                            CAMERA_REQUEST_CODE
                        )
                    }
                }
            }
        } else if (requestCode == STORAGE_REQUEST_CODE) {
            if (grantResults.isNotEmpty()) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(this)
                } else {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                    ) {
                        snackBarAction(
                            this,
                            binding.layoutParent,
                            resources.getString(R.string.hint_permissions)
                        )
                    } else {
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            STORAGE_REQUEST_CODE
                        )
                    }
                }
            }
        }
    }
}
