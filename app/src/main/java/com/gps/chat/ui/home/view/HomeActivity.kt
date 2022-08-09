package com.gps.chat.ui.home.view

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.bumptech.glide.Glide
import com.gps.chat.receivers.ConnectionBroadcastReceiver
import com.gps.chat.ui.home.model.UserProfileModel
import com.gps.chat.ui.home.viewmodel.HomeViewModel
import com.gps.chat.R
import com.gps.chat.databinding.ActivityHomeBinding
import com.gps.chat.databinding.DialogPermissionAlertBinding
import com.gps.chat.network.Resource
import com.gps.chat.ui.changepassword.view.ChangePasswordActivity
import com.gps.chat.ui.home.model.AdResponse
import com.gps.chat.ui.searchuser.view.SearchUserActivity
import com.gps.chat.ui.settings.view.SettingsActivity
import com.gps.chat.utils.*
import com.gps.chat.utils.CONSTANTS.AUTHORIZATION
import com.gps.chat.utils.CONSTANTS.IS_ADD
import com.gps.chat.utils.CONSTANTS.IS_PERMISSION_ALERT
import com.gps.chat.utils.CONSTANTS.PASSWORD
import com.gps.chat.utils.CONSTANTS.PROFILE_IMAGE
import com.gps.chat.utils.CONSTANTS.USER_ID
import com.sparkout.chat.common.ChatApp
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

@AndroidEntryPoint
class HomeActivity : AppCompatActivity(), View.OnClickListener,
    ConnectionBroadcastReceiver.ConnectivityReceiverListener {
    private var mDialog: Dialog? = null
    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var binding: ActivityHomeBinding
    private lateinit var dialogBinding: DialogPermissionAlertBinding


    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userDetailsObserver()
        adsObserver()
        try {
            if (ChatApp.mSocketHelper?.getSocketInstance()?.connected() == true) {
                ChatApp.mSocketHelper?.connectSocket(
                    SharedPreferencesEditor.getData(USER_ID), SharedPreferencesEditor.getData(
                        AUTHORIZATION
                    )
                )
            }
        } catch (e: Exception) {
            ChatApp.mSocketHelper?.connectSocket(
                SharedPreferencesEditor.getData(USER_ID), SharedPreferencesEditor.getData(
                    AUTHORIZATION
                )
            )
        }

        if (SharedPreferencesEditor.getDataBoolean(IS_PERMISSION_ALERT)) {
            SharedPreferencesEditor.storeValueBoolean(IS_PERMISSION_ALERT, false)
            if (Build.MANUFACTURER == "samsung") {
                permissionAlert()
            } else if (Build.MANUFACTURER == "Xiaomi") {
                permissionAlert()
            }
        }

        Glide.with(this)
            .setDefaultRequestOptions(glideRequestOptionProfile())
            .load(SharedPreferencesEditor.getData(PROFILE_IMAGE))
            .into(binding.ivProfile)

        binding.ivProfile.setOnClickListener(this)
        binding.ivSettings.setOnClickListener(this)
        binding.ivSearch.setOnClickListener(this)
        binding.ivNewChat.setOnClickListener(this)
        binding.ivConf.setOnClickListener(this)
        binding.textviewPassword.setOnClickListener(this)
        binding.tvChangePassword.setOnClickListener(this)
        binding.ivClose.setOnClickListener(this)
        getUserDetails()
    }

    override fun onClick(view: View?) {
        view?.let {
            preventDoubleClick(view)
            hideKeyboard(this, view)
            when (view.id) {
                R.id.iv_profile -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                }
                R.id.iv_conf -> {
                }
                R.id.iv_search -> {
                    startActivity(Intent(this, SearchUserActivity::class.java))
                }
                R.id.iv_new_chat -> {
                    startActivity(Intent(this, SearchUserActivity::class.java))
                }
                R.id.textview_password -> {
                    val clipboardManager =
                        getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clipData =
                        ClipData.newPlainText("password", binding.textviewPassword.text.toString())
                    clipboardManager.setPrimaryClip(clipData)
                    Toast.makeText(this, "Password copied to clipboard", Toast.LENGTH_SHORT).show()
                }
                R.id.tv_change_password -> {
                    startActivity(Intent(this, ChangePasswordActivity::class.java))
                }
                R.id.iv_close -> {
                    binding.layoutPassword.hide()
                    binding.layoutAd.hide()
                    binding.ivSearch.show()
                    binding.layoutMain.show()
                    binding.viewPagerMain.adapter =
                        ViewPagerAdapter(supportFragmentManager, this@HomeActivity)
                    binding.tabLayoutMain.setupWithViewPager(binding.viewPagerMain)
                }
                R.id.iv_settings -> {
                    val popup =
                        PopupMenu(this, view)
                    popup.menuInflater.inflate(R.menu.menu_home, popup.menu)
                    popup.setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.menu_create_group -> {
                                /* startActivity(
                                     Intent(this, AddParticipantsActivity::class.java)
                                         .putExtra("isFromMain", false)
                                 )*/
                            }
                            R.id.menu_settings -> {
                                startActivity(Intent(this, SettingsActivity::class.java))
                            }
                        }
                        true
                    }
                    popup.show()
                }
                R.id.text_view_allow -> {
                    mDialog!!.dismiss()
                    if (Build.MANUFACTURER == "samsung") {
                        val intent =
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri =
                            Uri.fromParts("package", packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    } else if (Build.MANUFACTURER == "Xiaomi") {
                        val intent = Intent("miui.intent.action.APP_PERM_EDITOR")
                        intent.setClassName(
                            "com.miui.securitycenter",
                            "com.miui.permcenter.permissions.PermissionsEditorActivity"
                        )
                        intent.putExtra("extra_pkgname", packageName)
                        startActivity(intent)
                    }
                }
            }
        }

    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
//        App.instance.setConnectivityListener(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(sticky = true)
    fun onMessageEvent(string: String) {
        if (string == "update_profile") {
            Glide.with(this)
                .setDefaultRequestOptions(glideRequestOptionProfile())
                .load(SharedPreferencesEditor.getData(PROFILE_IMAGE))
                .into(binding.ivProfile)
        } else if (string == "password_changed") {
            binding.layoutPassword.hide()
            if (SharedPreferencesEditor.getDataBoolean(IS_ADD)) {
                SharedPreferencesEditor.storeValueBoolean(IS_ADD, false)
                loadAd()
            } else {
                binding.layoutPassword.hide()
                binding.layoutAd.hide()
                binding.ivSearch.show()
                binding.layoutMain.show()
                binding.viewPagerMain.adapter =
                    ViewPagerAdapter(supportFragmentManager, this@HomeActivity)
                binding.tabLayoutMain.setupWithViewPager(binding.viewPagerMain)
            }
        }
    }

    //TODO: fun to make user enable to permission manually to unrestrict background optimization
    private fun permissionAlert() {
        dialogBinding = DialogPermissionAlertBinding.inflate(layoutInflater)
        mDialog = Dialog(this, R.style.SlideTheme)
        mDialog?.let {
            it.requestWindowFeature(Window.FEATURE_NO_TITLE)
            it.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            it.setCancelable(false)
            it.setContentView(dialogBinding.root)

            if (Build.MANUFACTURER == "samsung") {
                dialogBinding.textViewContent.text =
                    resources.getString(R.string.str_permission_samsung)
            } else if (Build.MANUFACTURER == "Xiaomi") {
                dialogBinding.textViewContent.text =
                    resources.getString(R.string.str_permission_xiaomi)
            }

            dialogBinding.textViewAllow.setOnClickListener(this)

            it.show()
        }

    }

    private fun getUserDetails() {
        homeViewModel.getUserDetails(SharedPreferencesEditor.getData(USER_ID))
    }

    private fun loadAd() {
        homeViewModel.getAds()
    }

    private fun userDetailsObserver() {
        homeViewModel.userDetailsResponse.observe(this) { response ->
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
                        errorAuthenticationLogout(this@HomeActivity)
                    } else {
                        response.message?.let {
                            snackBar(
                                this@HomeActivity,
                                binding.layoutParent,
                                it
                            )

                        }
                    }

                }
            }
        }

    }

    private fun adsObserver() {
        homeViewModel.adsResponse.observe(this) { response ->
            when (response.status) {
                Resource.Status.SUCCESS -> {
                    removeProgressDialog()
                    updateAdsUi(response)
                }
                Resource.Status.LOADING -> {
                    showProgressDialog(this)
                }
                Resource.Status.ERROR -> {
                    removeProgressDialog()
                    response.message?.let {
                        snackBar(
                            this@HomeActivity,
                            binding.layoutParent,
                            it
                        )

                    }
                }

            }
        }

    }

    private fun updateAdsUi(response: Resource<AdResponse>) {

        response.data?.let {
            if (it.status) {
                binding.layoutPassword.hide()
                binding.layoutMain.hide()
                binding.ivSearch.show()
                binding.layoutAd.show()
                it.data.let {
                    Glide.with(this@HomeActivity)
                        .setDefaultRequestOptions(glideRequestOptionProfile())
                        .load(it[0].adImage)
                        .into(binding.ivAd)
                }

            }
        }
    }


    private fun updateUi(response: Resource<UserProfileModel>) {
        response.data?.let {
            if (it.status) {
                if (it.data?.userStatus == 1) {
                    binding.ivSearch.hide()
                    binding.layoutAd.hide()
                    binding.layoutMain.hide()
                    binding.layoutPassword.show()
                    binding.textviewPassword.text =
                        SharedPreferencesEditor.getData(PASSWORD)
                } else {
                    binding.layoutPassword.hide()
                    if (SharedPreferencesEditor.getDataBoolean(IS_ADD)) {
                        SharedPreferencesEditor.storeValueBoolean(IS_ADD, false)
                        loadAd()
                    } else {
                        binding.layoutPassword.hide()
                        binding.layoutAd.hide()
                        binding.ivSearch.show()
                        binding.layoutMain.show()
                        binding.viewPagerMain.adapter =
                            ViewPagerAdapter(supportFragmentManager, this@HomeActivity)
                        binding.tabLayoutMain.setupWithViewPager(binding.viewPagerMain)
                    }
                }

            } else {
                snackBar(this@HomeActivity, binding.layoutParent, it.message)
            }
        }
    }


    class ViewPagerAdapter(
        fragmentManager: FragmentManager?,
        private val homeActivity: HomeActivity
    ) :
        FragmentPagerAdapter(fragmentManager!!) {
        var titles =
            arrayOf("Chat", "Call", "Video Call")

        override fun getPageTitle(position: Int): CharSequence? {
            return titles[position]
        }

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> {
                    RecentChatFragment(homeActivity)
                }
                else -> {
                    RecentChatFragment(homeActivity)
                }
            }
        }

        override fun getCount(): Int {
            return titles.size //This one is important too
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        if (isConnected) {
            getUserDetails()
        } else {
            snackBar(this, binding.layoutParent, resources.getString(R.string.no_internet))
        }
    }
}
