package com.sparkout.chat.ui.userinfo.view

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.sparkout.chat.R
import com.sparkout.chat.common.BaseUtils.Companion.hideKeyboard
import com.sparkout.chat.common.BaseUtils.Companion.preventDoubleClick
import com.sparkout.chat.common.BaseUtils.Companion.requestOptionsD
import com.sparkout.chat.common.ChatApp
import com.sparkout.chat.common.ChatApp.Companion.mAppDatabase
import com.sparkout.chat.common.Global
import com.sparkout.chat.common.SharedPreferenceEditor
import com.sparkout.chat.common.model.BlockedUsersModel
import com.sparkout.chat.common.model.UserDetailsModel
import com.sparkout.chat.databinding.ActivityUserInfoBinding
import com.sparkout.chat.ui.chat.model.BlockUserSocketModel
import com.sparkout.chat.ui.pinchzoom.view.ImageZoomActivity
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

class UserInfoActivity : AppCompatActivity(), View.OnClickListener {
    var mToId: String = ""
    lateinit var userDetailsModel: List<UserDetailsModel>
    private lateinit var binding: ActivityUserInfoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        EventBus.getDefault().register(this)
        setSupportActionBar(binding.toolBar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_arrow_back)

        binding.imageviewProfile.setOnClickListener(this)
        binding.textviewBlockContact.setOnClickListener(this)
        binding.textviewUnblockContact.setOnClickListener(this)

        if (intent.getStringExtra("id")!!.isNotEmpty()) {
            mToId = intent.getStringExtra("id")!!
        }

        userDetailsModel = mAppDatabase!!.getUserDetailsDao().getUserDetails(mToId)
        if (userDetailsModel.isNotEmpty()) {
            Glide.with(this)
                .setDefaultRequestOptions(requestOptionsD()!!)
                .load(userDetailsModel[0].profilePicture)
                .into(binding.imageviewProfile)
            binding.textviewName.text = userDetailsModel[0].username
            binding.textviewGender.text = userDetailsModel[0].gender
            binding.textviewDob.text = userDetailsModel[0].dateOfBirth
        }
        val mBlockedByYouStatusList =
            mAppDatabase!!.getBlockedUsersDao().checkAlreadyBlockedByYou(
                mToId, SharedPreferenceEditor.getData(
                    Global.USER_ID
                )
            )
        if (mBlockedByYouStatusList.isNotEmpty()) {
            binding.textviewBlockContact.visibility = View.GONE
            binding.textviewUnblockContact.visibility = View.VISIBLE
        } else {
            binding.textviewUnblockContact.visibility = View.GONE
            binding.textviewBlockContact.visibility = View.VISIBLE
        }
        val mBlockedByUserStatusList =
            mAppDatabase!!.getBlockedUsersDao().checkAlreadyBlockedByUser(
                mToId, SharedPreferenceEditor.getData(
                    Global.USER_ID
                )
            )
        if (mBlockedByUserStatusList.isNotEmpty()) {
            Glide.with(this)
                .setDefaultRequestOptions(requestOptionsD()!!)
                .load(R.drawable.default_user)
                .into(binding.imageviewProfile)
        } else {
            if (userDetailsModel.isNotEmpty()) {
                Glide.with(this)
                    .setDefaultRequestOptions(requestOptionsD()!!)
                    .load(userDetailsModel[0].profilePicture)
                    .into(binding.imageviewProfile)
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

    override fun onClick(v: View?) {
        preventDoubleClick(v!!)
        hideKeyboard(this, v)
        when (v.id) {
            R.id.imageview_profile -> {
                if (userDetailsModel.isNotEmpty()) {
                    startActivity(
                        Intent(this, ImageZoomActivity::class.java)
                            .putExtra("value", 2)
                            .putExtra("URL", userDetailsModel[0].profilePicture)
                    )
                }
            }
            R.id.textview_block_contact -> {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Block user")
                builder.setMessage(
                    "Are you sure you want to block"
                        .plus(" ")
                        .plus(binding.textviewName.text.toString())
                        .plus("?")
                )
                builder.setCancelable(true)
                builder.setPositiveButton("Yes", object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        dialog?.dismiss()
                        val mBlockUserSocketModel = BlockUserSocketModel()
                        mBlockUserSocketModel.sender =
                            SharedPreferenceEditor.getData(Global.USER_ID)
                        mBlockUserSocketModel.blockUser = mToId
                        val mBlockUserJson = Gson().toJson(mBlockUserSocketModel)
                        val mJsonObject = JSONObject(mBlockUserJson)
                        ChatApp.mSocketHelper?.blockUser(mJsonObject)
                    }
                })
                builder.setNegativeButton("No", object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        dialog?.dismiss()
                    }
                })
                val alertDialog = builder.create()
                alertDialog.show()
            }
            R.id.textview_unblock_contact -> {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Unblock user")
                builder.setMessage(
                    "Are you sure you want to unblock"
                        .plus(" ")
                        .plus(binding.textviewName.text.toString())
                        .plus("?")
                )
                builder.setCancelable(true)
                builder.setPositiveButton("Yes", object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        dialog?.dismiss()
                        val mBlockUserSocketModel = BlockUserSocketModel()
                        mBlockUserSocketModel.sender =
                            SharedPreferenceEditor.getData(Global.USER_ID)
                        mBlockUserSocketModel.unblockUser = mToId
                        val mBlockUserJson = Gson().toJson(mBlockUserSocketModel)
                        val mJsonObject = JSONObject(mBlockUserJson)
                        ChatApp.mSocketHelper?.unblockUser(mJsonObject)
                    }
                })
                builder.setNegativeButton("No", object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        dialog?.dismiss()
                    }
                })
                val alertDialog = builder.create()
                alertDialog.show()
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(mBlockedUsersModel: BlockedUsersModel) {
        if (mBlockedUsersModel.checkBlockedByYou) {
            binding.textviewBlockContact.visibility = View.GONE
            binding.textviewUnblockContact.visibility = View.VISIBLE
        } else {
            binding.textviewUnblockContact.visibility = View.GONE
            binding.textviewBlockContact.visibility = View.VISIBLE
        }

        if (mBlockedUsersModel.checkBlockedByUser) {
            Glide.with(this)
                .setDefaultRequestOptions(requestOptionsD()!!)
                .load(R.drawable.default_user)
                .into(binding.imageviewProfile)
        } else {
            if (userDetailsModel.isNotEmpty()) {
                Glide.with(this)
                    .setDefaultRequestOptions(requestOptionsD()!!)
                    .load(userDetailsModel[0].profilePicture)
                    .into(binding.imageviewProfile)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}
