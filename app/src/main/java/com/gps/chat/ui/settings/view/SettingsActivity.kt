package com.gps.chat.ui.settings.view

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.CompoundButton
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import com.bumptech.glide.Glide
import com.gps.chat.ui.settings.model.request.UpdateProfileModel
import com.gps.chat.ui.settings.model.response.UpdateProfileResponse
import com.gps.chat.ui.settings.viewmodel.UpdateProfileViewModel
import com.gps.chat.R
import com.gps.chat.crop.CropImage
import com.gps.chat.crop.CropImageView
import com.gps.chat.databinding.ActivitySettingsBinding
import com.gps.chat.network.Resource
import com.gps.chat.ui.register.viewmodel.UploadViewModel
import com.gps.chat.utils.*
import com.gps.chat.utils.CONSTANTS.CAMERA_REQUEST_CODE
import com.gps.chat.utils.CONSTANTS.DATE_OF_BIRTH
import com.gps.chat.utils.CONSTANTS.FULL_NAME
import com.gps.chat.utils.CONSTANTS.GENDER
import com.gps.chat.utils.CONSTANTS.PASSPORT_NUMBER
import com.gps.chat.utils.CONSTANTS.PROFILE_IMAGE
import com.gps.chat.utils.CONSTANTS.STORAGE_REQUEST_CODE
import com.gps.chat.utils.CONSTANTS.USERNAME
import com.gps.chat.utils.CONSTANTS.USER_TYPE
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.net.URLConnection

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity(), CompoundButton.OnCheckedChangeListener,
    View.OnClickListener {
    var isFullNameEdited = false
    var stringProfileUrl: String = ""
    private var resultUri: Uri? = null
    private var imageFile: File? = null
    var mimeType: String = ""
    private val uploadViewModel: UploadViewModel by viewModels()
    private val updateProfileViewModel: UpdateProfileViewModel by viewModels()
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back_arrow)
        uploadImageObserver()
        updateProfileObserver()

        binding.tvName.text = SharedPreferencesEditor.getData(FULL_NAME)
        binding.etFullName.setText(SharedPreferencesEditor.getData(FULL_NAME))

        if (SharedPreferencesEditor.getDataInt(USER_TYPE) == 0) {
            binding.tvUserType.text = resources.getString(R.string.string_temporary)
                .plus(" ")
                .plus(resources.getString(R.string.string_user))
        } else {
            binding.tvUserType.text = resources.getString(R.string.string_permanent)
                .plus(" ")
                .plus(resources.getString(R.string.string_user))
        }
        binding.tvUserName.text = SharedPreferencesEditor.getData(USERNAME)
        binding.tvGender.text = SharedPreferencesEditor.getData(GENDER)
        binding.tvDob.text = SharedPreferencesEditor.getData(DATE_OF_BIRTH)
        binding.tvPassportNumber.text = SharedPreferencesEditor.getData(PASSPORT_NUMBER)
        Glide.with(this)
            .setDefaultRequestOptions(glideRequestOptionProfile())
            .load(SharedPreferencesEditor.getData(PROFILE_IMAGE))
            .into(binding.ivUserProfile)

        binding.switchHideLastSeen.setOnCheckedChangeListener(this)
        binding.ivUserProfile.setOnClickListener(this)
        binding.ivAddProfile.setOnClickListener(this)
        binding.tvEdit.setOnClickListener(this)
        binding.tvLogout.setOnClickListener(this)

        binding.etFullName.doAfterTextChanged { string ->
            if (string.toString().isNotEmpty()) {
                isFullNameEdited = true
            }
        }
    }

    private fun updateProfileObserver() {
        updateProfileViewModel.updateProfileResponse.observe(this) { response ->
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

    private fun updateUi(response: Resource<UpdateProfileResponse>) {
        response.data?.let {
            if (it.status) {
                showToast(this@SettingsActivity, it.message)
                binding.tvEdit.text = resources.getString(R.string.string_edit)
                binding.ivAddProfile.hide()
                binding.layoutFullName.hide()
                binding.layoutName.show()
                stringProfileUrl = ""
                binding.etFullName.setText(it.data.name)
                SharedPreferencesEditor.saveUserData(it.data)
                EventBus.getDefault().postSticky("update_profile")
            } else {
                snackBar(
                    this,
                    binding.layoutParent,
                    it.message
                )
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

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
    }

    override fun onClick(view: View?) {
        view?.let {
            hideKeyboard(this, view)
            preventDoubleClick(view)
            when (view.id) {
                R.id.iv_user_profile -> {
                    /* startActivity(
                         Intent(this, ImageZoomActivity::class.java)
                             .putExtra("value", 2)
                             .putExtra(
                                 "URL",
                                 SharedPreferenceEditor.getData(PROFILE_IMAGE)
                             )
                     )*/
                }
                R.id.iv_add_profile -> {
                    selectImage()
                }
                R.id.tv_edit -> {
                    if (binding.tvEdit.text.toString() == resources.getString(R.string.string_edit)) {
                        binding.tvEdit.text = resources.getString(R.string.string_update)
                        binding.layoutName.hide()
                        binding.layoutFullName.show()
                        binding.ivAddProfile.show()
                    } else {
                        if (stringProfileUrl.isEmpty() && !isFullNameEdited) {
                            binding.tvEdit.text = resources.getString(R.string.string_edit)
                            binding.ivAddProfile.hide()
                            binding.layoutFullName.hide()
                            binding.layoutFullName.show()
                        } else {
                            updateProfile()

                        }
                    }
                }
                R.id.tv_logout -> {
                    updateProfileViewModel.logout()
                }
                else -> {
                }
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
                        resultUri = it
                    }
                    if (resultUri != null) {
                        Glide.with(this)
                            .setDefaultRequestOptions(glideRequestOptionProfile())
                            .load(resultUri)
                            .into(binding.ivUserProfile)
                        imageUpload()
                    }
                }
            }
        }
    }

    //TODO: api call upload image
    private fun imageUpload() {
        if (resultUri != null) {
            imageFile = FilePath.getPath(this, resultUri!!)
            mimeType = URLConnection.guessContentTypeFromName(imageFile!!.name)
        }
        if (imageFile != null) {
            val requestFile: RequestBody =
                imageFile!!.asRequestBody(mimeType.toMediaTypeOrNull())
            val body: MultipartBody.Part =
                MultipartBody.Part.createFormData(
                    "media", imageFile!!.name,
                    requestFile
                )
            uploadViewModel.uploadImage(body)
        }
    }

    //TODO: api call to update profile data
    private fun updateProfile() {
        val updateProfileModel = UpdateProfileModel()
        updateProfileModel.name = binding.etFullName.text.toString().trim()
        if (stringProfileUrl.isNotEmpty()) {
            updateProfileModel.profilePicture = stringProfileUrl
        }
        updateProfileViewModel.updateProfile(updateProfileModel)
    }


    private fun updateUiImage(response: Resource<CommonResponse>) {
        response.data?.let {
            if (it.status) {
                showToast(this, it.message)
                mimeType = ""
                imageFile = null
                resultUri = null
                it.data?.let {
                    stringProfileUrl = it.url

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
