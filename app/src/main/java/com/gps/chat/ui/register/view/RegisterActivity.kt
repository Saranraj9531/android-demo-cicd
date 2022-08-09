package com.gps.chat.ui.register.view

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.Window
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.gps.chat.R
import com.gps.chat.crop.CropImage
import com.gps.chat.crop.CropImageView
import com.gps.chat.databinding.ActivityRegisterBinding
import com.gps.chat.network.Resource
import com.gps.chat.ui.home.view.HomeActivity
import com.gps.chat.ui.register.model.response.RegistrationResponse
import com.gps.chat.ui.register.viewmodel.RegistrationViewModel
import com.gps.chat.ui.register.viewmodel.UploadViewModel
import com.gps.chat.utils.*
import com.gps.chat.utils.CONSTANTS.CAMERA_REQUEST_CODE
import com.gps.chat.utils.CONSTANTS.STORAGE_REQUEST_CODE
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.net.URLConnection

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity(), View.OnClickListener {
    private var mUserName: String = ""
    var mGender: String = ""
    var mDOB: String = ""
    var mimeType: String = ""
    private var isPassportID: Boolean = false
    private var mResultUri: Uri? = null
    private var mPassportResultUri: Uri? = null
    private var mImageFile: File? = null
    private var mPassportImageFile: File? = null
    val mUploadViewModel: UploadViewModel by viewModels()
    val mRegistrationViewModel: RegistrationViewModel by viewModels()
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back_arrow)

        binding.etName.doAfterTextChanged {
            mRegistrationViewModel.mName = it.toString()
        }
        binding.etPassportNumber.doAfterTextChanged {
            mRegistrationViewModel.mPassportNumber = it.toString()
        }

        intent.getStringExtra("username")?.let {
            mUserName = it
            mRegistrationViewModel.mUserName = it
        }

        intent.getStringExtra("gender")?.let {
            mGender = it
            mRegistrationViewModel.mGender = it

        }
        intent.getStringExtra("dob")?.let {
            mDOB = it
            mRegistrationViewModel.mDateOfBirth = it
        }

        mRegistrationViewModel.isUserValid().observe(this) {
            if (it) {
                binding.tvFinish.alpha = 1f
                binding.tvFinish.isClickable = true
            } else {
                binding.tvFinish.alpha = .5f
                binding.tvFinish.isClickable = false
            }
        }


        registerObserver()
        uploadImageObservers()

        binding.ivAddProfile.setOnClickListener(this)
        binding.layoutPassportId.setOnClickListener(this)
        binding.cardPassportId.setOnClickListener(this)
        binding.tvFinish.setOnClickListener(this)
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
            hideKeyboard(this, view)
            preventDoubleClick(view)
            when (view.id) {
                R.id.iv_add_profile -> {
                    isPassportID = false
                    selectImage()
                }
                R.id.layout_passport_id -> {
                    isPassportID = true
                    selectImage()
                }
                R.id.card_passport_id -> {
                    isPassportID = true
                    selectImage()
                }
                R.id.tv_finish -> {
                    register()

                }
            }
        }

    }

    private fun selectImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this@RegisterActivity,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                if (ContextCompat.checkSelfPermission(
                        this@RegisterActivity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    CropImage.activity(null)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(this)
                } else {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(
                            this@RegisterActivity,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                    ) {
                        ActivityCompat.requestPermissions(
                            this@RegisterActivity,
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            CONSTANTS.STORAGE_REQUEST_CODE
                        )
                    } else {
                        snackBarAction(
                            this@RegisterActivity,
                            binding.layoutParent,
                            resources.getString(R.string.hint_permissions)
                        )
                    }
                }
            } else {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(
                        this@RegisterActivity,
                        Manifest.permission.CAMERA
                    )
                ) {
                    ActivityCompat.requestPermissions(
                        this@RegisterActivity,
                        arrayOf(Manifest.permission.CAMERA),
                        CAMERA_REQUEST_CODE
                    )
                } else {
                    snackBarAction(
                        this@RegisterActivity,
                        binding.layoutParent,
                        resources.getString(R.string.hint_permissions)
                    )
                }
            }
        } else {
            CropImage.activity(null)
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this)
        }
    }


    private fun register() {
        mRegistrationViewModel.registerUser()
    }

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
                    if (isPassportID) {
                        result?.let {
                            mPassportResultUri = it.uri
                        }
                        if (mPassportResultUri != null) {
                            binding.cardPassportId.show()
                            Glide.with(this@RegisterActivity)
                                .load(mPassportResultUri)
                                .apply(
                                    RequestOptions()
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        .error(R.drawable.ic_default_user)
                                        .placeholder(R.drawable.ic_default_user)
                                        .dontAnimate()
                                )
                                .into(binding.ivPassportId)
                            imageUpload()

                        }
                    } else {
                        result?.let {
                            mResultUri = it.uri
                        }
                        if (mResultUri != null) {
                            Glide.with(this@RegisterActivity)
                                .load(mResultUri)
                                .apply(
                                    RequestOptions()
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        .error(R.drawable.ic_default_user)
                                        .placeholder(R.drawable.ic_default_user)
                                        .dontAnimate()
                                )
                                .into(binding.ivProfile)
                            imageUpload()

                        }
                    }
                }
            }
        }
    }

    private fun imageUpload() {
        if (mResultUri != null) {
            mImageFile = FilePath.getPath(this, mResultUri!!)
            mimeType = URLConnection.guessContentTypeFromName(mImageFile!!.name)
        }
        if (mPassportResultUri != null) {
            mPassportImageFile = FilePath.getPath(this, mPassportResultUri!!)
            mimeType = URLConnection.guessContentTypeFromName(mPassportImageFile!!.name)
        }
        if (mImageFile != null) {
            val requestFile: RequestBody =
                mImageFile!!.asRequestBody(mimeType.toMediaTypeOrNull())
            val body: MultipartBody.Part =
                MultipartBody.Part.createFormData(
                    "media", mImageFile!!.name,
                    requestFile
                )
            mUploadViewModel.uploadImage(body)
        }
        if (mPassportImageFile != null) {
            val requestFile: RequestBody =
                mPassportImageFile!!.asRequestBody(mimeType.toMediaTypeOrNull())
            val body: MultipartBody.Part =
                MultipartBody.Part.createFormData(
                    "media", mPassportImageFile!!.name,
                    requestFile
                )
            mUploadViewModel.uploadImage(body)
        }
    }

    private fun uploadImageObservers() {
        mUploadViewModel.uploadResponse.observe(this) { response ->
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
                            this@RegisterActivity,
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
                showToast(this@RegisterActivity, it.message)
                mimeType = ""
                mPassportImageFile = null
                mImageFile = null
                mPassportResultUri = null
                mResultUri = null

                if (isPassportID) {
                    isPassportID = false
                    binding.layoutPassportId.hide()
                    binding.cardPassportId.show()
                    it.data?.let {
                        mRegistrationViewModel.mPassportImage = it.url
                    }
                } else {
                    it.data?.let {
                        mRegistrationViewModel.mProfilePicture = it.url

                    }
                }

            } else {
                snackBar(
                    this@RegisterActivity,
                    binding.layoutParent,
                    it.message
                )
            }

        }


    }


    private fun registerObserver() {
        mRegistrationViewModel.registerResponse.observe(this, Observer {
            when (it.status) {
                Resource.Status.LOADING -> {
                    showProgressDialog(this)
                }

                Resource.Status.SUCCESS -> {
                    removeProgressDialog()
                    updateUi(it)
                }
                Resource.Status.ERROR -> {
                    removeProgressDialog()
                    it.message?.let {
                        snackBar(
                            this@RegisterActivity,
                            binding.layoutParent,
                            it
                        )
                    }

                }

            }
        })
    }

    private fun updateUi(data: Resource<RegistrationResponse>) {

        data.data?.let { response ->
            if (response.status) {
                registerSuccessDialog(response)
            } else {
                snackBar(
                    this@RegisterActivity,
                    binding.layoutParent,
                    response.message
                )
            }
        }

    }


    /**TODO: user register success info
     * @param body - registered user details*/
    private fun registerSuccessDialog(body: RegistrationResponse) {
        val dialog = Dialog(this, R.style.SlideTheme)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_success_register)
        val textViewGetStarted = dialog.findViewById<AppCompatTextView>(R.id.text_view_get_started)
        textViewGetStarted.setOnClickListener {
            dialog.dismiss()
            showToast(this@RegisterActivity, body.message)
            SharedPreferencesEditor.storeValueBoolean(CONSTANTS.IS_ADD, true)
            body.data?.let {
                SharedPreferencesEditor.saveUserData(it)
            }
            startActivity(Intent(this@RegisterActivity, HomeActivity::class.java))
            finishAffinity()
        }
        dialog.show()
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
                            this@RegisterActivity,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            this@RegisterActivity,
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            STORAGE_REQUEST_CODE
                        )
                    } else {
                        CropImage.activity(null)
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
                            this@RegisterActivity,
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
                    CropImage.activity(null)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(this)
                } else {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                    ) {
                        snackBarAction(
                            this@RegisterActivity,
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
