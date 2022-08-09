package com.sparkout.chat.ui.groupinfo.view

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.sparkout.chat.R
import com.sparkout.chat.common.*
import com.sparkout.chat.common.BaseUtils.Companion.CAMERA_REQUEST_CODE
import com.sparkout.chat.common.BaseUtils.Companion.STORAGE_REQUEST_CODE
import com.sparkout.chat.common.BaseUtils.Companion.getMessageId
import com.sparkout.chat.common.BaseUtils.Companion.getUTCTime
import com.sparkout.chat.common.BaseUtils.Companion.preventDoubleClick
import com.sparkout.chat.common.BaseUtils.Companion.removeProgressDialog
import com.sparkout.chat.common.BaseUtils.Companion.requestOptionsD
import com.sparkout.chat.common.BaseUtils.Companion.showProgressDialog
import com.sparkout.chat.common.BaseUtils.Companion.showToast
import com.sparkout.chat.common.BaseUtils.Companion.snackBarAction
import com.sparkout.chat.common.ChatApp.Companion.mAppDatabase
import com.sparkout.chat.common.Global.USER_ID
import com.sparkout.chat.common.chatenum.ChatMessageStatus
import com.sparkout.chat.common.chatenum.ChatMessageTypes
import com.sparkout.chat.common.chatenum.ChatTypes
import com.sparkout.chat.common.model.CommonResponse
import com.sparkout.chat.common.viewmodel.UploadMediaViewModel
import com.sparkout.chat.crop.CropImage
import com.sparkout.chat.crop.CropImageView
import com.sparkout.chat.databinding.ActivityGroupChatInfoBinding
import com.sparkout.chat.databinding.ActivityGroupInfoBinding
import com.sparkout.chat.network.Resource
import com.sparkout.chat.ui.addparticipants.view.AddParticipantsActivity
import com.sparkout.chat.ui.chat.model.ChatModel
import com.sparkout.chat.ui.groupchat.model.GroupDetailsModel
import com.sparkout.chat.ui.groupchat.model.GroupMemberModel
import com.sparkout.chat.ui.groupchat.view.GroupChatActivity
import com.sparkout.chat.ui.groupchat.view.adapter.GroupMemberAdapter
import com.sparkout.chat.ui.groupinfo.model.AddOrRemoveAdminModel
import com.sparkout.chat.ui.groupinfo.model.GroupChatExitModel
import com.sparkout.chat.ui.groupinfo.model.GroupInfoModel
import com.sparkout.chat.ui.groupinfo.model.GroupRemoveMember
import com.sparkout.chat.ui.groupinfo.viewmodel.GroupInfoViewModel
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.io.File
import java.net.URLConnection

class GroupInfoActivity : AppCompatActivity(), View.OnClickListener {
    var mToId: String = ""
    lateinit var mGroupInfoViewModel: GroupInfoViewModel
    private lateinit var mChangeGroupSubDialog: Dialog
    var mGroupDetailsList: List<GroupDetailsModel> = ArrayList<GroupDetailsModel>()
    var mGroupMembersList: List<GroupMemberModel> = ArrayList<GroupMemberModel>()
    private var mResultUri: Uri? = null
    private var mImageFile: File? = null
    var mimeType: String = ""
    private val mUploadMediaViewModel: UploadMediaViewModel by viewModels()
    private var mImageUrl: String = ""
    private lateinit var binding: ActivityGroupInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent.getStringExtra("id")!!.isNotEmpty()) {
            mToId = intent.getStringExtra("id")!!
        }

        listenObserver()

        mGroupInfoViewModel.getGroupDetails(mToId)
        mGroupInfoViewModel.getGroupMembers(mToId)

        binding.imageviewGroupDetailsEdit.setOnClickListener(this)
        binding.textviewGroupDescription.setOnClickListener(this)
        binding.textviewGroupDescriptionInfo.setOnClickListener(this)
        binding.imageviewGroupIcon.setOnClickListener(this)
        binding.layoutAddParticipants.setOnClickListener(this)
        binding.textviewExitGroup.setOnClickListener(this)
        binding.textviewDeleteGroup.setOnClickListener(this)
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(mGroupId: String) {
        if (mGroupId.isNotEmpty()) {
            mGroupInfoViewModel.getGroupDetails(mGroupId)
            mGroupInfoViewModel.getGroupMembers(mGroupId)
        }
    }

    private fun listenObserver() {
        mGroupInfoViewModel.mListGroupDetails
            .observe(this, object : Observer<List<GroupDetailsModel>> {
                override fun onChanged(mGroupDetails: List<GroupDetailsModel>) {
                    if (mGroupDetails.isNotEmpty() && mGroupDetails[0].groupId == mToId) {
                        mGroupDetailsList = mGroupDetails
                        binding.collapsingGroupInfo.title = mGroupDetailsList[0].groupTitle
                        Glide.with(this@GroupInfoActivity)
                            .setDefaultRequestOptions(requestOptionsD()!!)
                            .load(mGroupDetailsList[0].groupImage)
                            .into(binding.imageviewGroupIcon)
                        if (mGroupDetailsList[0].checkExit) {

                            binding.imageviewGroupDetailsEdit.visibility = View.GONE
                            binding.textviewExitGroup.visibility = View.GONE
                            binding.layoutAddParticipants.visibility = View.GONE
                            binding.textviewDeleteGroup.visibility = View.VISIBLE
                        } else {
                            binding.textviewDeleteGroup.visibility = View.GONE
                            binding.textviewExitGroup.visibility = View.VISIBLE
                            val mGroupAdminList = mAppDatabase!!.getGroupMemberDao()
                                .getCheckAdmin(
                                    mToId,
                                    SharedPreferenceEditor.getData(Global.USER_ID)
                                )
                            if (mGroupAdminList.isNotEmpty()) {
                                binding.imageviewGroupDetailsEdit.visibility = View.VISIBLE
                                binding.layoutAddParticipants.visibility = View.VISIBLE
                                if (mGroupDetailsList[0].groupDescription.isNotEmpty()) {

                                    binding.textviewGroupDescription.visibility = View.GONE
                                    binding.layoutDescGroupInfo.visibility = View.VISIBLE

                                    binding.textviewGroupDescriptionInfo.text =
                                        mGroupDetailsList[0].groupDescription
                                } else {
                                    binding.layoutDescGroupInfo.visibility = View.GONE
                                    binding.textviewGroupDescription.visibility = View.VISIBLE
                                }
                            } else {
                                binding.textviewGroupDescription.visibility = View.GONE
                                binding.imageviewGroupDetailsEdit.visibility = View.GONE
                                binding.layoutAddParticipants.visibility = View.GONE
                            }
                        }
                    }
                }
            })
        mGroupInfoViewModel.mListGroupMembers
            .observe(this, object : Observer<List<GroupMemberModel>> {
                override fun onChanged(mGroupMembers: List<GroupMemberModel>) {
                    if (mGroupMembers.isNotEmpty() && mGroupMembers[0].groupId == mToId) {
                        mGroupMembersList = mGroupMembers
                        if (mGroupMembersList.size == 1) {
                            binding.tvParticipantCount.text =
                                mGroupMembersList.size.toString().plus(" ")
                                    .plus(resources.getString(R.string.str_participant))
                        } else {
                            binding.tvParticipantCount.text =
                                mGroupMembersList.size.toString().plus(" ")
                                    .plus(resources.getString(R.string.str_participants))
                        }
                        val mGroupMemberAdapter =
                            GroupMemberAdapter(this@GroupInfoActivity, mGroupMembersList)
                        binding.rvGroupMembers.adapter = mGroupMemberAdapter
                    }
                }
            })
        mUploadMediaViewModel.commonResponse.observe(this) { response ->
            when (response.status) {
                Resource.Status.LOADING -> {
                    showProgressDialog(this)
                }
                Resource.Status.SUCCESS -> {
                    response.data?.status?.let {
                        mImageUrl = response.data.data.url
                        updateGroupInfo("", mImageUrl, null)
                    }

                }
                Resource.Status.ERROR -> {
                    removeProgressDialog()
                }

            }
        }


    }

    override fun onClick(v: View?) {
        preventDoubleClick(v!!)
        when (v.id) {
            R.id.imageview_group_details_edit -> {
                changeGrpSubDialog(1)
            }
            R.id.textview_group_description -> {
                changeGrpSubDialog(2)
            }
            R.id.textview_group_description_info -> {
                changeGrpSubDialog(2)
            }
            R.id.layout_add_participants -> {
                startActivity(
                    Intent(this, AddParticipantsActivity::class.java)
                        .putExtra("isFromMain", true)
                        .putExtra("groupId", mToId)
                )
            }
            R.id.textview_exit_group -> {
                AlertDialog.Builder(this)
                    .setMessage(
                        getString(R.string.str_exit) + " \"" + binding.collapsingGroupInfo + "\" " + getString(
                            R.string.str_groups
                        )
                    )
                    .setPositiveButton(resources.getString(R.string.str_exit)) { dialogInterface, which ->
                        exitGroupFunctionalities()
                    }
                    .setNegativeButton(resources.getString(R.string.str_cancel), null)
                    .show()
            }
            R.id.imageview_group_icon -> {
                val mGroupAdminList =
                    mAppDatabase!!.getGroupMemberDao()
                        .getCheckAdmin(mToId, SharedPreferenceEditor.getData(USER_ID))
                if (mGroupDetailsList.isNotEmpty() && !mGroupDetailsList[0].checkExit && mGroupAdminList.isNotEmpty()) {
                    selectImage()
                }
            }
            R.id.textview_delete_group -> {
                AlertDialog.Builder(this)
                    .setMessage(getString(R.string.str_delete_group) + " \"" + binding.collapsingGroupInfo.title + "\" ?")
                    .setPositiveButton(resources.getString(R.string.str_delete)) { dialogInterface, which ->
                        val mGroupMembersList =
                            mAppDatabase!!.getGroupMemberDao()
                                .getGroupMembers(
                                    mToId,
                                    SharedPreferenceEditor.getData(Global.USER_ID)
                                )
                        if (mGroupMembersList.isNotEmpty()) {
                            mAppDatabase!!.getGroupMemberDao().deleteList(mGroupMembersList)
                        }
                        val mGroupDetailsList =
                            mAppDatabase!!.getGroupDetailsDao()
                                .getGroupDetails(
                                    mToId,
                                    SharedPreferenceEditor.getData(Global.USER_ID)
                                )
                        if (mGroupDetailsList.isNotEmpty()) {
                            mAppDatabase!!.getGroupDetailsDao().deleteList(mGroupDetailsList)
                        }
                        val mGroupChatList =
                            mAppDatabase!!.getChatDao().getGroupChat(
                                mToId, SharedPreferenceEditor.getData(
                                    Global.USER_ID
                                )
                            )
                        if (mGroupChatList.isNotEmpty()) {
                            mAppDatabase!!.getChatDao().delete(mGroupChatList)
                        }
                        val mGroupConversationList =
                            mAppDatabase!!.getChatListDao().getChatList(
                                mToId, SharedPreferenceEditor.getData(
                                    Global.USER_ID
                                )
                            )
                        if (mGroupConversationList.isNotEmpty()) {
                            mAppDatabase!!.getChatListDao().delete(mGroupConversationList)
                        }
                        EventBus.getDefault().post("message_update")
                        finish()
                        GroupChatActivity.activity.finish()
                    }
                    .setNegativeButton(resources.getString(R.string.str_cancel), null)
                    .show()
            }
        }
    }

    /**
     * fun - to change the group_subject or group_description
     *
     * @param value - 1 (group_name) or 2 (group_description)
     */
    private fun changeGrpSubDialog(value: Int) {
        val mGroupAdminList =
            mAppDatabase!!.getGroupMemberDao().getCheckAdmin(
                mToId, SharedPreferenceEditor.getData(
                    Global.USER_ID
                )
            )
        if (mGroupDetailsList.isNotEmpty() && !mGroupDetailsList[0].checkExit && mGroupAdminList.isNotEmpty()) {
            mChangeGroupSubDialog = Dialog(this, R.style.SlideTheme)
            mChangeGroupSubDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            mChangeGroupSubDialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            mChangeGroupSubDialog.setContentView(R.layout.dialog_change_group_details)
            mChangeGroupSubDialog.show()
            val edittextDialogGroupSubject: EditText =
                mChangeGroupSubDialog.findViewById<EditText>(R.id.edittext_dialog_group_subject)
            val textviewDialogGroupDescription: TextView =
                mChangeGroupSubDialog.findViewById<TextView>(R.id.textview_dialog_group_desc)
            val textviewGroupDialogTitle: TextView =
                mChangeGroupSubDialog.findViewById<TextView>(R.id.textview_group_dialog_title)
            val btnGroupDialogCancel: AppCompatButton =
                mChangeGroupSubDialog.findViewById(R.id.btn_group_dialog_cancel)
            val btnGroupDialogok: AppCompatButton =
                mChangeGroupSubDialog.findViewById(R.id.btn_group_dialog_ok)
            btnGroupDialogCancel.setOnClickListener { mChangeGroupSubDialog.dismiss() }
            if (value == 1) {
                edittextDialogGroupSubject.hint = resources.getString(R.string.str_add_subject)
                edittextDialogGroupSubject.setText(mGroupDetailsList[0].groupTitle)
                textviewGroupDialogTitle.text = resources.getString(R.string.str_enter_new_subject)
                textviewDialogGroupDescription.visibility = View.GONE
            } else {
                edittextDialogGroupSubject.hint =
                    resources.getString(R.string.str_add_group_description)
                edittextDialogGroupSubject.setText(mGroupDetailsList[0].groupDescription)
                textviewGroupDialogTitle.text = resources.getString(R.string.str_group_description)
                textviewDialogGroupDescription.visibility = View.VISIBLE
            }
            btnGroupDialogok.setOnClickListener {
                if (value == 1) {
                    if (TextUtils.isEmpty(edittextDialogGroupSubject.text.toString().trim())) {
                        showToast(
                            this@GroupInfoActivity,
                            this@GroupInfoActivity.resources.getString(R.string.str_subject_cant_be_empty)
                        )
                    } else {
                        mChangeGroupSubDialog.dismiss()
                        updateGroupInfo(edittextDialogGroupSubject.text.toString().trim(), "", null)
                    }
                } else {
                    mChangeGroupSubDialog.dismiss()
                    if (mGroupDetailsList[0].groupDescription != edittextDialogGroupSubject.text.toString()
                            .trim()
                    ) {
                        updateGroupInfo("", "", edittextDialogGroupSubject.text.toString().trim())
                    }
                }
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
                    mResultUri = result!!.uri
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
                        if (BaseUtils.isOnline(this)) {
                            imageUpload()
                        } else {
                            BaseUtils.snackBar(
                                this,
                                binding.layoutParent,
                                resources.getString(R.string.no_internet)
                            )
                        }
                    }
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    val error = result!!.error
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

    private fun updateGroupInfo(
        mGroupName: String,
        mGroupIcon: String,
        mGroupDescription: String?
    ) {
        var mUpdateMessage: String = ""
        var mUpdateMessageType: String = ""
        val mListGroupMembersId = ArrayList<String>()
        for (i in 0 until mGroupMembersList.size) {
            mListGroupMembersId.add(mGroupMembersList[i].memberId)
        }
        val mGroupInfoModel = GroupInfoModel()
        mGroupInfoModel.sender = SharedPreferenceEditor.getData(Global.USER_ID)
        mGroupInfoModel.groupId = mToId
        mGroupInfoModel.receivers = mListGroupMembersId
        if (mGroupName.isNotEmpty()) {
            mGroupInfoModel.groupTitle = mGroupName
            mUpdateMessageType = ChatMessageTypes.GROUPINFO.type
            mUpdateMessage =
                "changed the subject from ".plus("\"").plus(mGroupDetailsList[0].groupTitle)
                    .plus("\"")
                    .plus(" to ").plus("\"").plus(mGroupName).plus("\"")
        }
        if (mGroupIcon.isNotEmpty()) {
            mGroupInfoModel.groupImage = mGroupIcon
            mUpdateMessageType = ChatMessageTypes.GROUPPROFILE.type
            mUpdateMessage = mGroupDetailsList[0].groupImage.plus(" to ").plus(mGroupIcon)
        }
        if (null != mGroupDescription) {
            mGroupInfoModel.groupDescription = mGroupDescription
            if (mGroupDescription.isNotEmpty()) {
                mUpdateMessageType = ChatMessageTypes.GROUPINFO.type
                mUpdateMessage = "changed the group description"
            } else {
                mUpdateMessageType = ChatMessageTypes.GROUPINFO.type
                mUpdateMessage = "deleted the group description"
            }
        }
        val mGroupInfoJson = Gson().toJson(mGroupInfoModel)
        val jsonObject = JSONObject(mGroupInfoJson)
        ChatApp.mSocketHelper?.updateGroupInfo(jsonObject)
        groupInfoUpdateMessage(mUpdateMessage, mUpdateMessageType)
    }

    private fun groupInfoUpdateMessage(
        mUpdateMessage: String,
        mUpdateMessageType: String
    ) {
        val mListGroupMembersId = ArrayList<String>()
        for (i in 0 until mGroupMembersList.size) {
            mListGroupMembersId.add(mGroupMembersList[i].memberId)
        }
        val chatModel = ChatModel()
        chatModel.sender = SharedPreferenceEditor.getData(Global.USER_ID)
        chatModel.receiver = mToId
        chatModel.receivers = mListGroupMembersId
        chatModel.message = mUpdateMessage
        chatModel.chatTime = getUTCTime()
        chatModel.chatType = ChatTypes.GROUP.type
        chatModel.checkForwarded = false
        chatModel.checkReply = false
        chatModel.messageId =
            getMessageId(
                getUTCTime().plus("-").plus(SharedPreferenceEditor.getData(Global.USER_ID))
                    .plus("-")
                    .plus(mToId)
            )
        chatModel.messageStatus = ChatMessageStatus.SENT.ordinal
        chatModel.messageType = mUpdateMessageType
        val mChatJson = Gson().toJson(chatModel)
        val jsonObject = JSONObject(mChatJson)
        ChatApp.mSocketHelper?.sendGroupChat(jsonObject)
    }

    fun popupMenu(
        mLayoutGroupMembers: RelativeLayout,
        mGroupMemberModel: GroupMemberModel
    ) {
        val popup =
            PopupMenu(this, mLayoutGroupMembers, Gravity.CENTER)
        popup.menuInflater.inflate(R.menu.menu_group_member, popup.menu)
        for (i in mGroupMembersList.indices) {
            if (mGroupMembersList[i].memberId == SharedPreferenceEditor.getData(Global.USER_ID) && mGroupMembersList[i].checkAdmin) {
                popup.menu.findItem(R.id.nav_remove).isVisible = true
                if (mGroupMemberModel.checkAdmin) {
                    popup.menu.findItem(R.id.nav_dismiss_admin).isVisible = true
                } else {
                    popup.menu.findItem(R.id.nav_make_admin).isVisible = true
                }
            }
        }
        popup.setOnMenuItemClickListener { item ->
            groupMemberPopupAction(item, mGroupMemberModel)
            true
        }
        popup.show()
    }

    /**
     * fun - call from GroupMembersAdapter while select popup item
     * To view, message, make_admin, dismiss_admin or remove the selected member.
     *
     * @param item        - selected item
     * @param groupMember - selected member details
     */
    private fun groupMemberPopupAction(
        item: MenuItem,
        mGroupMember: GroupMemberModel
    ) {
        val i = item.itemId
        if (i == R.id.nav_message) {
            showToast(this, item.title.toString())
        } else if (i == R.id.nav_view) {
            showToast(this, item.title.toString())
        } else if (i == R.id.nav_make_admin) {
            makeGroupAdmin(mGroupMember)
        } else if (i == R.id.nav_dismiss_admin) {
            removeGroupAdmin(mGroupMember)
        } else if (i == R.id.nav_remove) {
            val mMembersList = ArrayList<String>()
            val mRemovedMembersList = ArrayList<String>()
            mRemovedMembersList.add(mGroupMember.memberId)
            for (k in mGroupMembersList.indices) {
                mMembersList.add(mGroupMembersList[k].memberId)
            }
            val mGroupRemoveMember = GroupRemoveMember()
            mGroupRemoveMember.groupId = mToId
            mGroupRemoveMember.sender = SharedPreferenceEditor.getData(Global.USER_ID)
            mGroupRemoveMember.members = mMembersList
            mGroupRemoveMember.removedMembers = mRemovedMembersList
            val mMessage = "removed ".plus(mGroupMember.memberId)
            val chatModel = ChatModel()
            chatModel.sender = SharedPreferenceEditor.getData(Global.USER_ID)
            chatModel.receiver = mToId
            chatModel.receivers = mMembersList
            chatModel.message = mMessage
            chatModel.chatTime = getUTCTime()
            chatModel.chatType = ChatTypes.GROUP.type
            chatModel.checkForwarded = false
            chatModel.checkReply = false
            chatModel.messageId =
                getMessageId(
                    getUTCTime().plus("-").plus(SharedPreferenceEditor.getData(Global.USER_ID))
                        .plus("-")
                        .plus(mToId)
                )
            chatModel.messageStatus = ChatMessageStatus.SENT.ordinal
            chatModel.messageType = ChatMessageTypes.REMOVEMEMBER.type

            mGroupRemoveMember.message = chatModel
            val mRemoveMemberJson = Gson().toJson(mGroupRemoveMember)
            val jsonObject = JSONObject(mRemoveMemberJson)
            Log.e("Bush", "remove: " + Gson().toJson(jsonObject))
            ChatApp.mSocketHelper?.removeGroupMember(jsonObject)
        }
    }

    private fun exitGroupFunctionalities() {
        val mGroupAdminList = mAppDatabase!!.getGroupMemberDao()
            .getCheckAdmin(mToId, SharedPreferenceEditor.getData(Global.USER_ID))
        if (mGroupAdminList.isNotEmpty()) {
            val mAdminList = mAppDatabase!!.getGroupMemberDao().fetchAdminList(mToId)
            if (mAdminList.size <= 1) {
                val mMembersExceptUserList =
                    mAppDatabase!!.getGroupMemberDao().fetchMembersExceptGroupList(
                        mToId,
                        SharedPreferenceEditor.getData(Global.USER_ID)
                    )
                if (mMembersExceptUserList.isNotEmpty()) {
                    val mMembersList = ArrayList<String>()
                    for (k in mGroupMembersList.indices) {
                        mMembersList.add(mGroupMembersList[k].memberId)
                    }
                    val mAddOrRemoveAdminModel = AddOrRemoveAdminModel()
                    mAddOrRemoveAdminModel.groupId = mToId
                    mAddOrRemoveAdminModel.receivers = mMembersList
                    mAddOrRemoveAdminModel.admin = mMembersExceptUserList[0].memberId
                    val mAddAdminJson = Gson().toJson(mAddOrRemoveAdminModel)
                    val mAddAdminObject = JSONObject(mAddAdminJson)
                    Log.e("Bush", "exitmakeGroupAdmin: " + Gson().toJson(mAddAdminJson))
                    ChatApp.mSocketHelper?.makeGroupAdmin(mAddAdminObject)
                }
            }
        }
        val mMembersList = ArrayList<String>()
        for (k in mGroupMembersList.indices) {
            mMembersList.add(mGroupMembersList[k].memberId)
        }
        val mGroupChatExitModel = GroupChatExitModel()
        mGroupChatExitModel.sender = SharedPreferenceEditor.getData(Global.USER_ID)
        mGroupChatExitModel.groupId = mToId
        mGroupChatExitModel.receivers = mMembersList
        val mGroupChatExitJson = Gson().toJson(mGroupChatExitModel)
        val jsonObject = JSONObject(mGroupChatExitJson)
        Log.e("Bush", "exitGroupFunctionalities: " + Gson().toJson(jsonObject))
        ChatApp.mSocketHelper?.exitGroup(jsonObject)
        val mUpdateMessage = "left"
        val chatModel = ChatModel()
        chatModel.sender = SharedPreferenceEditor.getData(Global.USER_ID)
        chatModel.receiver = mToId
        chatModel.receivers = mMembersList
        chatModel.message = mUpdateMessage
        chatModel.chatTime = getUTCTime()
        chatModel.chatType = ChatTypes.GROUP.type
        chatModel.checkForwarded = false
        chatModel.checkReply = false
        chatModel.messageId =
            getMessageId(
                getUTCTime().plus("-").plus(SharedPreferenceEditor.getData(Global.USER_ID))
                    .plus("-")
                    .plus(mToId)
            )
        chatModel.messageStatus = ChatMessageStatus.SENT.ordinal
        chatModel.messageType = ChatMessageTypes.EXITMEMBER.type
        val mChatJson = Gson().toJson(chatModel)
        val mChatJsonObject = JSONObject(mChatJson)
        ChatApp.mSocketHelper?.sendGroupChat(mChatJsonObject)
    }

    private fun makeGroupAdmin(mGroupMember: GroupMemberModel) {
        val mMembersList = ArrayList<String>()
        for (k in mGroupMembersList.indices) {
            mMembersList.add(mGroupMembersList[k].memberId)
        }
        val mAddOrRemoveAdminModel = AddOrRemoveAdminModel()
        mAddOrRemoveAdminModel.groupId = mToId
        mAddOrRemoveAdminModel.receivers = mMembersList
        mAddOrRemoveAdminModel.admin = mGroupMember.memberId
        val mAddAdminJson = Gson().toJson(mAddOrRemoveAdminModel)
        val mAddAdminObject = JSONObject(mAddAdminJson)
        Log.e("Bush", "makeGroupAdmin: " + Gson().toJson(mAddAdminJson))
        ChatApp.mSocketHelper?.makeGroupAdmin(mAddAdminObject)
    }

    private fun removeGroupAdmin(mGroupMember: GroupMemberModel) {
        val mMembersList = ArrayList<String>()
        for (k in mGroupMembersList.indices) {
            mMembersList.add(mGroupMembersList[k].memberId)
        }
        val mAddOrRemoveAdminModel = AddOrRemoveAdminModel()
        mAddOrRemoveAdminModel.groupId = mToId
        mAddOrRemoveAdminModel.receivers = mMembersList
        mAddOrRemoveAdminModel.admin = mGroupMember.memberId
        val mRemoveAdminJson = Gson().toJson(mAddOrRemoveAdminModel)
        val mRemoveAdminObject = JSONObject(mRemoveAdminJson)
        Log.e("Bush", "removeGroupAdmin: " + Gson().toJson(mRemoveAdminObject))
        ChatApp.mSocketHelper?.removeGroupAdmin(mRemoveAdminObject)
    }
}