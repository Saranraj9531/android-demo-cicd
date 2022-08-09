package com.sparkout.chat.ui.creategroup.view

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.sparkout.chat.R
import com.sparkout.chat.common.BaseUtils.Companion.CAMERA_REQUEST_CODE
import com.sparkout.chat.common.BaseUtils.Companion.STORAGE_REQUEST_CODE
import com.sparkout.chat.common.BaseUtils.Companion.getGroupId
import com.sparkout.chat.common.BaseUtils.Companion.getMessageId
import com.sparkout.chat.common.BaseUtils.Companion.getUTCTime
import com.sparkout.chat.common.BaseUtils.Companion.isOnline
import com.sparkout.chat.common.BaseUtils.Companion.preventDoubleClick
import com.sparkout.chat.common.BaseUtils.Companion.removeProgressDialog
import com.sparkout.chat.common.BaseUtils.Companion.showProgressDialog
import com.sparkout.chat.common.BaseUtils.Companion.showToast
import com.sparkout.chat.common.BaseUtils.Companion.snackBar
import com.sparkout.chat.common.BaseUtils.Companion.snackBarAction
import com.sparkout.chat.common.ChatApp
import com.sparkout.chat.common.Global.USER_ID
import com.sparkout.chat.common.ImageFilePath
import com.sparkout.chat.common.SharedPreferenceEditor
import com.sparkout.chat.common.chatenum.ChatMessageStatus
import com.sparkout.chat.common.chatenum.ChatMessageTypes
import com.sparkout.chat.common.chatenum.ChatTypes
import com.sparkout.chat.common.model.CommonResponse
import com.sparkout.chat.common.model.UserDetailsModel
import com.sparkout.chat.common.viewmodel.UploadMediaViewModel
import com.sparkout.chat.crop.CropImage
import com.sparkout.chat.crop.CropImageView
import com.sparkout.chat.databinding.ActivityCreateGroupBinding
import com.sparkout.chat.network.Resource
import com.sparkout.chat.ui.chat.model.ChatModel
import com.sparkout.chat.ui.creategroup.view.adapter.GroupParticipantsAdapter
import com.sparkout.chat.ui.groupchat.model.CreateGroupSocketModel
import com.sparkout.chat.ui.groupchat.model.GroupDetailsModel
import com.sparkout.chat.ui.groupchat.model.MembersModel
import com.sparkout.chat.ui.groupchat.view.GroupChatActivity
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.net.URLConnection

class CreateGroupActivity : AppCompatActivity(), View.OnClickListener {
    var mListUsersSelected = ArrayList<UserDetailsModel>()
    private var mResultUri: Uri? = null
    private var mImageFile: File? = null
    var mimeType: String = ""
    private val mUploadMediaViewModel: UploadMediaViewModel by viewModels()
    private var mImageUrl: String = ""

    private lateinit var binding: ActivityCreateGroupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateGroupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        listenObservers()

        mListUsersSelected =
            intent.extras?.getSerializable("selectedMembers") as ArrayList<UserDetailsModel>

        binding.imageviewBack.setOnClickListener(this)
        binding.imageviewGroupIcon.setOnClickListener(this)
        binding.textviewNext.setOnClickListener(this)

        binding.rvCreateGroup.layoutManager = GridLayoutManager(this, 4)
        if (mListUsersSelected.isNotEmpty()) {
            if (mListUsersSelected.size == 1) {
                binding.textviewParticipant.text = resources.getString(R.string.str_participants)
                    .plus(": ")
                    .plus(mListUsersSelected.size)
                    .plus(" ")
                    .plus(resources.getString(R.string.str_person))
            } else {
                binding.textviewParticipant.text = resources.getString(R.string.str_participants)
                    .plus(": ")
                    .plus(mListUsersSelected.size)
                    .plus(" ")
                    .plus(resources.getString(R.string.str_persons))
            }
            binding.rvCreateGroup.adapter = GroupParticipantsAdapter(this, mListUsersSelected)
        }
    }

    override fun onClick(v: View?) {
        preventDoubleClick(v!!)
        when (v.id) {
            R.id.imageview_back -> {
                onBackPressed()
            }
            R.id.textview_next -> {
                if (isOnline(this)) {

                    if (binding.edittextGroupSubject.text.toString().trim().isEmpty()) {
                        snackBar(
                            this,
                            binding.layoutParent,
                            resources.getString(R.string.str_create_group_error)
                        )
                    } else {
                        val mGroupId = getGroupId(
                            getUTCTime()
                                .plus("-")
                                .plus(SharedPreferenceEditor.getData(USER_ID))
                                .plus("-")
                                .plus("groupid")
                        )
                        val mListMembersId = ArrayList<String>()
                        val mListMembers = ArrayList<MembersModel>()
                        mListMembers.clear()
                        mListMembersId.clear()
                        val mMembersModel = MembersModel()
                        mMembersModel.id = SharedPreferenceEditor.getData(USER_ID)
                        mMembersModel.checkAdmin = true
                        mListMembers.add(mMembersModel)
                        mListMembersId.add(SharedPreferenceEditor.getData(USER_ID))
                        for (i in 0 until mListUsersSelected.size) {
                            val membersModel = MembersModel()
                            membersModel.id = mListUsersSelected[i].id
                            membersModel.checkAdmin = false
                            mListMembers.add(membersModel)
                            mListMembersId.add(mListUsersSelected[i].id)
                        }
                        val createGroupSocketModel = CreateGroupSocketModel()
                        createGroupSocketModel.groupId = mGroupId
                        createGroupSocketModel.groupTitle =
                            binding.edittextGroupSubject.text.toString().trim()
                        createGroupSocketModel.groupDescription = ""
                        createGroupSocketModel.groupImage = mImageUrl
                        createGroupSocketModel.createdBy = SharedPreferenceEditor.getData(USER_ID)!!
                        createGroupSocketModel.members = mListMembers
                        val mGroupDetailsModel = GroupDetailsModel()
                        mGroupDetailsModel.groupId = mGroupId
                        mGroupDetailsModel.groupTitle =
                            binding.edittextGroupSubject.text.toString().trim()
                        mGroupDetailsModel.groupDescription = ""
                        mGroupDetailsModel.groupImage = mImageUrl
                        mGroupDetailsModel.createdBy = SharedPreferenceEditor.getData(USER_ID)!!
                        mGroupDetailsModel.checkExit = false
                        mGroupDetailsModel.createdDate = ""
                        ChatApp.mAppDatabase!!.getGroupDetailsDao().insert(mGroupDetailsModel)
                        val chatModel = ChatModel()
                        chatModel.sender = SharedPreferenceEditor.getData(USER_ID)!!
                        chatModel.receiver = mGroupId
                        chatModel.receivers = mListMembersId
                        chatModel.message = "created group"
                            .plus(" \"")
                            .plus(binding.edittextGroupSubject.text.toString().trim())
                            .plus("\"")
                        chatModel.chatTime = getUTCTime()
                        chatModel.chatType = ChatTypes.GROUP.type
                        chatModel.checkForwarded = false
                        chatModel.checkReply = false
                        chatModel.messageId =
                            getMessageId(
                                getUTCTime().plus("-").plus(SharedPreferenceEditor.getData(USER_ID))
                                    .plus("-")
                                    .plus(mGroupId)
                            )
                        chatModel.messageStatus = ChatMessageStatus.SENT.ordinal
                        chatModel.messageType = ChatMessageTypes.CREATEGROUP.type

                        createGroupSocketModel.message = chatModel
                        val mCreateGroupJson = Gson().toJson(createGroupSocketModel)
                        val jsonObject = JSONObject(mCreateGroupJson)
                        ChatApp.mSocketHelper?.createGroupChat(jsonObject)
                        startActivity(
                            Intent(this, GroupChatActivity::class.java)
                                .putExtra("id", jsonObject.optString("group_id"))
                        )
                        finish()
                    }
                } else {
                    showToast(this, resources.getString(R.string.no_internet))
                }
            }
            R.id.imageview_group_icon -> {
                selectImage()
            }
        }
    }

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
                    CropImage
                        .activity(null)
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
                    mResultUri = result?.uri
                    if (mResultUri != null) {
                        Glide.with(this)
                            .load(mResultUri)
                            .apply(
                                RequestOptions()
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .error(R.drawable.default_user)
                                    .placeholder(R.drawable.default_user)
                                    .dontAnimate()
                            )
                            .into(binding.imageviewGroupIcon)
                        if (isOnline(this)) {
                            imageUpload()
                        } else {
                            snackBar(
                                this,
                                binding.layoutParent,
                                resources.getString(R.string.no_internet)
                            )
                        }
                    }
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    val error = result?.error
                }
            }
        }
    }

    private fun listenObservers() {
        mUploadMediaViewModel.commonResponse.observe(this) { response ->
            when (response.status) {
                Resource.Status.ERROR -> {
                    removeProgressDialog()
                }
                Resource.Status.SUCCESS -> {
                    response.data?.let {
                        mImageUrl = it.data.url
                    }
                }
                Resource.Status.LOADING -> {
                    showProgressDialog(this)
                }
            }
        }

    }

    private fun imageUpload() {
        if (mResultUri != null) {
            mImageFile = ImageFilePath.getFile(this, mResultUri!!)
            mimeType = URLConnection.guessContentTypeFromName(mImageFile!!.name)
        }
        if (mImageFile != null) {
            val requestFile: RequestBody =
                mImageFile!!.asRequestBody(mimeType.toMediaTypeOrNull())
            val body: MultipartBody.Part =
                MultipartBody.Part.createFormData(
                    "media", mImageFile!!.name,
                    requestFile
                )
            mUploadMediaViewModel.uploadImage(body)
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