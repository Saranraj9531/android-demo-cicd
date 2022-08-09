package com.sparkout.chat.ui.chat.view

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Dialog
import android.app.NotificationManager
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.ExifInterface
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.text.ClipboardManager
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.ProgressBar
import android.widget.SeekBar
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.TransformationUtils
import com.bumptech.glide.load.resource.gif.GifOptions
import com.bumptech.glide.request.RequestOptions
import com.google.android.libraries.places.api.model.Place
import com.google.gson.Gson
import com.sparkout.chat.R
import com.sparkout.chat.clickinterface.ChatClickOptions
import com.sparkout.chat.clickinterface.GifClickOptions
import com.sparkout.chat.common.*
import com.sparkout.chat.common.BaseUtils.Companion.DOCUMENT_PICKER_SELECT
import com.sparkout.chat.common.BaseUtils.Companion.MULTIPLE_IMAGE_SELECT
import com.sparkout.chat.common.BaseUtils.Companion.MY_REQUEST_CODE_DOCUMENT
import com.sparkout.chat.common.BaseUtils.Companion.MY_REQUEST_CODE_VIDEO
import com.sparkout.chat.common.BaseUtils.Companion.PHOTO_EDIT_CODE
import com.sparkout.chat.common.BaseUtils.Companion.PLACE_PICKER_REQUEST
import com.sparkout.chat.common.BaseUtils.Companion.fix
import com.sparkout.chat.common.BaseUtils.Companion.getBitmapFromUri
import com.sparkout.chat.common.BaseUtils.Companion.getLastMessage
import com.sparkout.chat.common.BaseUtils.Companion.getLastSeenFromMilli
import com.sparkout.chat.common.BaseUtils.Companion.getMessageId
import com.sparkout.chat.common.BaseUtils.Companion.getMimeType
import com.sparkout.chat.common.BaseUtils.Companion.getProgressPercentage
import com.sparkout.chat.common.BaseUtils.Companion.getStaticMap
import com.sparkout.chat.common.BaseUtils.Companion.getUTCTime
import com.sparkout.chat.common.BaseUtils.Companion.glideRequestOptionProfile
import com.sparkout.chat.common.BaseUtils.Companion.hideKeyboard
import com.sparkout.chat.common.BaseUtils.Companion.isOnline
import com.sparkout.chat.common.BaseUtils.Companion.loadVideoPath
import com.sparkout.chat.common.BaseUtils.Companion.pickImageFromStorage
import com.sparkout.chat.common.BaseUtils.Companion.preventDoubleClick
import com.sparkout.chat.common.BaseUtils.Companion.removeProgressDialog
import com.sparkout.chat.common.BaseUtils.Companion.requestOptionsT
import com.sparkout.chat.common.BaseUtils.Companion.requestOptionsTv
import com.sparkout.chat.common.BaseUtils.Companion.sDocumentDirectoryPath
import com.sparkout.chat.common.BaseUtils.Companion.showHideKeyboard
import com.sparkout.chat.common.BaseUtils.Companion.showProgressDialog
import com.sparkout.chat.common.BaseUtils.Companion.showToast
import com.sparkout.chat.common.BaseUtils.Companion.snackBar
import com.sparkout.chat.common.ChatApp.Companion.mAppDatabase
import com.sparkout.chat.common.ChatApp.Companion.mSocketHelper
import com.sparkout.chat.common.Global.USER_ID
import com.sparkout.chat.common.chatenum.ChatMessageStatus
import com.sparkout.chat.common.chatenum.ChatMessageTypes
import com.sparkout.chat.common.chatenum.ChatTypes
import com.sparkout.chat.common.model.*
import com.sparkout.chat.common.viewmodel.UploadMediaViewModel
import com.sparkout.chat.common.workmanager.VideoCompresserWorker
import com.sparkout.chat.crop.CropImage
import com.sparkout.chat.crop.CropImageView
import com.sparkout.chat.databinding.ActivityChatBinding
import com.sparkout.chat.musicplayer.RxMusicPlayer
import com.sparkout.chat.musicplayer.common.PlaybackState
import com.sparkout.chat.musicplayer.common.isPlaying
import com.sparkout.chat.musicplayer.common.playStop
import com.sparkout.chat.musicplayer.common.seek
import com.sparkout.chat.musicplayer.media.Media
import com.sparkout.chat.network.Resource
import com.sparkout.chat.recordaudio.OnBasketAnimationEnd
import com.sparkout.chat.recordaudio.OnRecordListener
import com.sparkout.chat.ui.attachment.GridMenuAdapter
import com.sparkout.chat.ui.attachment.view.MenuEditText
import com.sparkout.chat.ui.attachment.view.MenuRecyclerView
import com.sparkout.chat.ui.attachment.view.SoftKeyBoardPopup
import com.sparkout.chat.ui.chat.model.*
import com.sparkout.chat.ui.chat.view.adapter.ChatAdapter
import com.sparkout.chat.ui.chat.view.adapter.GifAdapter
import com.sparkout.chat.ui.chat.viewmodel.ChatViewModel
import com.sparkout.chat.ui.exoplayer.view.ExoPlayerActivity
import com.sparkout.chat.ui.forward.view.ForwardActivity
import com.sparkout.chat.ui.media.view.MediaActivity
import com.sparkout.chat.ui.photoedit.view.PhotoEditActivity
import com.sparkout.chat.ui.photoedit.view.PhotoEditNewActivity
import com.sparkout.chat.ui.pinchzoom.view.ImageZoomActivity
import com.sparkout.chat.ui.userinfo.view.UserInfoActivity
import com.sparkout.chat.waverecorder.WaveConfig
import com.sparkout.chat.waverecorder.WaveRecorder
import com.vincent.filepicker.Constant
import com.vincent.filepicker.activity.NormalFilePickActivity
import com.vincent.filepicker.activity.VideoPickActivity
import com.vincent.filepicker.activity.VideoPickActivity.IS_NEED_CAMERA
import com.vincent.filepicker.activity.VideoPickActivity.IS_NEED_FOLDER_LIST
import com.vincent.filepicker.filter.entity.NormalFile
import com.vincent.filepicker.filter.entity.VideoFile
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.apache.commons.lang.StringUtils
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import retrofit2.Response
import timber.log.Timber
import java.io.*
import java.lang.Runnable
import java.net.URLConnection
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

@AndroidEntryPoint
class ChatActivity : AppCompatActivity(), View.OnClickListener, MenuEditText.PopupListener,
    GridMenuAdapter.GridMenuListener, TextWatcher,
    ChatClickOptions, GifClickOptions {
    companion object {
        var mToId: String = ""
    }

    private lateinit var recorder: WaveRecorder
    private val recordingConfig = WaveConfig()
    private lateinit var compositeDisposable: CompositeDisposable
    private lateinit var holder: ChatAdapter.AudioViewHolder
    private var mChatModelDefault: ChatModel? = null
    private lateinit var chatModel: ChatModel
    private lateinit var mSeekBar: SeekBar
    private var mMedia: Media? = null
    private var sender: String = ""
    private var mMessageId: String = ""
    private lateinit var textViewRuntimeAudioSent: AppCompatTextView
    private lateinit var textViewRuntimeAudioReceived: AppCompatTextView
    private lateinit var textViewTotalTimeAudioSent: AppCompatTextView
    private lateinit var textViewTotalTimeAudioReceived: AppCompatTextView
    private lateinit var imageViewPlaySendAudioNew: AppCompatImageView
    private lateinit var imageViewPlayReceivedAudioNew: AppCompatImageView
    private val uploadMediaViewModel: UploadMediaViewModel by viewModels()
    private val chatViewModel: ChatViewModel by viewModels()
    lateinit var menuKeyboard: SoftKeyBoardPopup
    private lateinit var mAudioRecorder: MediaRecorder
    private var outputFile = ""
    private var duration: Int = 0
    lateinit var mProgressBar: ProgressBar
    private lateinit var mRecyclerViewGif: RecyclerView
    lateinit var imageViewGif: AppCompatImageView
    var listChat = ArrayList<ChatModel>()
    lateinit var mChatAdapter: ChatAdapter
    lateinit var mGifAdapter: GifAdapter
    var listGif = ArrayList<GifResponse.GifData>()
    private var mPrefVideo = ""
    private var mUri: Uri? = null
    var isReply = false
    var itemPosition = 0
    var isEditedMsg = false
    private var progressValue = 0
    private var mListCountOnLongClick: Int? = null
    lateinit var mLayoutManager: LinearLayoutManager
    var mMenuBlock: MenuItem? = null
    private lateinit var binding: ActivityChatBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        clearNotifications()

        EventBus.getDefault().register(this)
        // TODO avoid cursor allocation error samesung
        val manufacturer = Build.MANUFACTURER
        if (manufacturer == "samsung") {
            fix()
        }

        enablePermission()

        setSupportActionBar(binding.layoutAppBar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        supportActionBar!!.setDisplayHomeAsUpEnabled(false)

        if (intent.getStringExtra("id")!!.isNotEmpty()) {
            mToId = intent.getStringExtra("id")!!
        }

        initViews()

        listenObservers()

        initGifAdapter()

        initChatLayoutManager()

        userProfileData()

        initChatAdapter()

        initChatScrollListener()

        gif()

        initClickListeners()



        binding.edittextMessage.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if (binding.layoutGif.root.visibility == View.VISIBLE
                ) {
                    showHideKeyboard(this@ChatActivity, false, binding.edittextMessage)
                    binding.layoutGif.root.visibility = View.GONE
                    menuKeyboard.dismissPopup()
                }
                return false
            }
        })

        binding.recordView.setOnRecordListener(object : OnRecordListener {
            override fun onFinish(recordTime: Long) {
                binding.recordView.visibility = View.GONE
                binding.edittextMessage.visibility = View.VISIBLE
                binding.menuChatContainer.visibility = View.VISIBLE
                stopRecording(1)
            }

            override fun onLessThanSecond() {
                binding.recordView.visibility = View.GONE
                binding.edittextMessage.visibility = View.VISIBLE
                binding.menuChatContainer.visibility = View.VISIBLE
                stopRecording(0)
            }

            override fun onCancel() {
                //On Swipe To Cancel
                stopRecording(0)
            }

            override fun onStart() {
                if (mMedia != null && mMedia!!.isPlaying()) {
                    mMedia!!.playStop()
                }
                binding.edittextMessage.visibility = View.INVISIBLE
                binding.menuChatContainer.visibility = View.INVISIBLE
                binding.recordView.visibility = View.VISIBLE
                startRecording()
            }
        })
        binding.recordView.setOnBasketAnimationEndListener(object : OnBasketAnimationEnd {
            override fun onAnimationEnd() {
                binding.recordView.visibility = View.GONE
                binding.edittextMessage.visibility = View.VISIBLE
                binding.menuChatContainer.visibility = View.VISIBLE
            }
        })

        binding.rvChat.addOnItemTouchListener(
            RecyclerViewItemClickListener(
                this,
                binding.rvChat,
                object :
                    RecyclerViewItemClickListener.Companion.OnItemClickListener {
                    override fun onItemClick(
                        view: View?,
                        position: Int
                    ) {
                    }

                    override fun onItemLongClick(
                        view: View?,
                        position: Int
                    ) {
                        if (listChat.isNotEmpty()) {
                            mListCountOnLongClick = listChat.size
                            if (listChat[position].messageStatus != ChatMessageStatus.NOT_SENT.ordinal &&
                                listChat[position].messageType != ChatMessageTypes.DATE.type &&
                                listChat[position].messageType != ChatMessageTypes.UNREAD.type
                            ) {
                                itemPosition = position
                                chatOptions(position)
                            }
                        }
                    }
                })
        )
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_chat, menu)
        mMenuBlock = menu.findItem(R.id.menu_block)
        val mBlockedUsersList =
            mAppDatabase!!.getBlockedUsersDao()
                .getBlockedUsers(mToId, SharedPreferenceEditor.getData(USER_ID))
        if (mBlockedUsersList.isNotEmpty()) {
            if (mBlockedUsersList[0].checkBlockedByYou) {
                mMenuBlock?.title = resources.getText(R.string.string_unblock)
            } else {
                mMenuBlock?.title = resources.getText(R.string.string_block)
            }
        } else {
            mMenuBlock?.title = resources.getText(R.string.string_block)
        }

        return true
    }

    override fun onResume() {
        super.onResume()
        ChatApp.mSocketHelper?.updateReadStatus(this, mToId)
    }

    override fun onStart() {
        super.onStart()
        RxMusicPlayer.start(this)
    }

    override fun onStop() {
        super.onStop()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(mBlockedUsersModel: BlockedUsersModel) {
        if (mBlockedUsersModel.checkBlockedByYou) {

            hideKeyboard(this, binding.textviewUnblock)
            mMenuBlock?.title = resources.getText(R.string.string_unblock)
            binding.edittextMessage.visibility = View.INVISIBLE
            binding.menuChatContainer.visibility = View.INVISIBLE
            binding.textviewUnblock.visibility = View.VISIBLE
        } else {
            binding.textviewUnblock.visibility = View.GONE
            mMenuBlock?.title = resources.getText(R.string.string_block)
            binding.edittextMessage.visibility = View.VISIBLE
            binding.menuChatContainer.visibility = View.VISIBLE
        }

        if (mBlockedUsersModel.checkBlockedByUser) {
            binding.textviewStatus.text = resources.getString(R.string.string_last_seen_blocked)
            Glide.with(this)
                .setDefaultRequestOptions(BaseUtils.requestOptionsD()!!)
                .load(R.drawable.ic_default_user)
                .into(binding.imageviewProfile)
        } else {
            val userDetailsModel = mAppDatabase!!.getUserDetailsDao().getUserDetails(mToId)
            if (userDetailsModel.isNotEmpty()) {
                Glide.with(this@ChatActivity)
                    .setDefaultRequestOptions(glideRequestOptionProfile())
                    .load(userDetailsModel[0].profilePicture)
                    .into(binding.imageviewProfile)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(uploadFileModel: UploadFileModel) {
        Log.e("Nive ", "onMessageEvent:uploadFileModel ")
        updateImageDocument(uploadFileModel.mFilePath, uploadFileModel.mType)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(chatModel: ChatModel) {
        var listChatNew = ArrayList<ChatModel>()
        listChatNew =
            mAppDatabase!!.getChatDao()
                .getChat(mToId, SharedPreferenceEditor.getData(USER_ID)) as ArrayList<ChatModel>
        listChatNew = sentUpdatedData(listChatNew)
        listChatNew.reverse()

        if (chatModel.sender == SharedPreferenceEditor.getData(USER_ID)) {
            mChatAdapter.updateMessageListItems(
                listChatNew,
                binding.rvChat,
                mChatModelDefault
            )
            if (ChatApp.wasInBackground!!) {
                mSocketHelper?.updateReadStatus(this, mToId)
            }
        } else {
            mChatAdapter.updateMessageListItems(
                listChatNew,
                binding.rvChat,
                mChatModelDefault
            )
            if (ChatApp.wasInBackground!!) {
                mSocketHelper?.updateReadStatus(this, mToId)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(string: String) {
        if (string == "messenger") {
            var listChatNew = ArrayList<ChatModel>()
            listChatNew =
                mAppDatabase!!.getChatDao()
                    .getChat(mToId, SharedPreferenceEditor.getData(USER_ID)) as ArrayList<ChatModel>
            listChatNew = sentUpdatedData(listChatNew)
            listChatNew.reverse()
            mChatAdapter.updateMessageListItems(
                listChatNew,
                binding.rvChat,
                mChatModelDefault
            )

            mSocketHelper?.updateReadStatus(this, mToId)
            val userDetailsModel = mAppDatabase!!.getUserDetailsDao().getUserDetails(mToId)
            if (userDetailsModel.isNotEmpty()) {
                Glide.with(this@ChatActivity)
                    .setDefaultRequestOptions(glideRequestOptionProfile())
                    .load(userDetailsModel[0].profilePicture)
                    .into(binding.imageviewProfile)
                binding.textviewName.text = userDetailsModel[0].username
            }
        } else if (string == "user_status") {
            friendOnlineStatus()
        } else if (string == "upload_video_failed") {
            var newListChat = ArrayList<ChatModel>()
            newListChat =
                mAppDatabase!!.getChatDao()
                    .getChat(mToId, SharedPreferenceEditor.getData(USER_ID)) as ArrayList<ChatModel>
            newListChat = sentUpdatedData(newListChat)
            newListChat.reverse()
            mChatAdapter.updateMessageListItems(
                newListChat,
                binding.rvChat,
                mChatModelDefault
            )
        } else if (string == "Image Error") {
            var listChatNew = ArrayList<ChatModel>()
            listChatNew = mAppDatabase!!.getChatDao()
                .getChat(mToId, SharedPreferenceEditor.getData(USER_ID)) as ArrayList<ChatModel>
            listChatNew.reverse()
            mChatAdapter.updateMessageListItems(
                listChatNew,
                null,
                mChatModelDefault
            )
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(chatTypingModel: ChatTypingModel) {
        if (chatTypingModel.receiver == SharedPreferenceEditor.getData(USER_ID) && chatTypingModel.sender == mToId) {
            binding.textviewStatus.text = resources.getString(R.string.str_typing)
            Handler().postDelayed(object : Runnable {
                override fun run() {
                    friendOnlineStatus()
                }
            }, 3000)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(audioMessage: AudioMessage) {
        for (i in 0 until listChat.size) {
            if (listChat[i].messageId == audioMessage.messageId) {
                val viewHolder =
                    binding.rvChat.findViewHolderForAdapterPosition(i) as ChatAdapter.AudioViewHolder
                viewHolder.imageViewMicSentAudio.setImageResource(R.drawable.ic_mic_played)
                viewHolder.imageViewMicReceivedAudio.setImageResource(R.drawable.ic_mic_played)
                if (mMedia != null && mMedia!!.isPlaying()) {
                    Log.e("Nive ", "onMessageEvent:playing ")
                    viewHolder.imageViewPlaySentAudio.setImageResource(R.drawable.ic_audio_pause)
                    viewHolder.imageViewPlayReceivedAudio.setImageResource(R.drawable.ic_audio_pause)
                } else {
                    Log.e("Nive ", "onMessageEvent:notplaying else ")
                    viewHolder.imageViewPlaySentAudio.setImageResource(R.drawable.ic_audio_played)
                    viewHolder.imageViewPlayReceivedAudio.setImageResource(R.drawable.ic_audio_played)
                }
            }
        }
    }

    private fun clearNotifications() {
        val notificationManager: NotificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

    private fun initViews() {
        mProgressBar = findViewById<ProgressBar>(R.id.progress_bar)
        mRecyclerViewGif = findViewById<RecyclerView>(R.id.recyclerview_gif)
        imageViewGif = findViewById<AppCompatImageView>(R.id.imageview_gif)
    }


    private fun initGifAdapter() {
        mRecyclerViewGif.layoutManager = GridLayoutManager(this, 2)

        mGifAdapter =
            GifAdapter(this@ChatActivity, listGif, this)
        mRecyclerViewGif.adapter = mGifAdapter
    }

    private fun initChatLayoutManager() {
        mLayoutManager = LinearLayoutManager(this)
        mLayoutManager.stackFromEnd = false
        mLayoutManager.reverseLayout = true
        binding.rvChat.layoutManager = mLayoutManager


    }

    private fun initChatAdapter() {
        /*   val listUnreadMessageCount =
               mAppDatabase!!.getChatDao()
                   .getMessageCount(SharedPreferenceEditor.getData(USER_ID), mToId)
           listChat.addAll(
               mAppDatabase!!.getChatDao().getChat(mToId, SharedPreferenceEditor.getData(USER_ID))
           )
           for (i in listChat.indices) {
               if (listChat[i].messageType.contains("101")) {
                   mAppDatabase!!.getChatDao().delete(listChat[i])
               }
           }
           listChat.clear()
           listChat.addAll(
               mAppDatabase!!.getChatDao().getChat(mToId, SharedPreferenceEditor.getData(USER_ID))
           )
           listChat = sentUpdatedData(listChat)
           listChat.reverse()
           mChatAdapter =
               ChatAdapter(this, listChat, this, listUnreadMessageCount.size, this)
           binding.rvChat.adapter = mChatAdapter
           binding.rvChat.setItemViewCacheSize(listChat.size)
           binding.rvChat.itemAnimator = null*/


        CoroutineScope(Dispatchers.IO).async {
            listChat.clear()

            val listUnreadMessageCount =
                ChatApp.mAppDatabase!!.getChatDao()
                    .getMessageCount(
                        SharedPreferenceEditor.getData(Global.USER_ID),
                        ChatActivity.mToId
                    )
            listChat.addAll(
                ChatApp.mAppDatabase!!.getChatDao().getChat(
                    ChatActivity.mToId, SharedPreferenceEditor.getData(
                        Global.USER_ID
                    )
                )
            )

            for (i in listChat.indices) {
                if (listChat[i].messageType.contains("101")) {
                    ChatApp.mAppDatabase!!.getChatDao().delete(listChat[i])
                }
            }
            listChat = sentUpdatedData(listChat)
            listChat.reverse()


            withContext(Dispatchers.Main) {
                mChatAdapter =
                    ChatAdapter(
                        this@ChatActivity,
                        listChat,
                        this@ChatActivity,
                        listUnreadMessageCount.size,
                        this@ChatActivity
                    )
                binding.rvChat.adapter = mChatAdapter
                binding.rvChat.setItemViewCacheSize(listChat.size)
                binding.rvChat.itemAnimator = null
            }
        }

    }


    private fun initChatScrollListener() {
        binding.rvChat.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!recyclerView.canScrollVertically(1) && dy > 0) {
                    binding.fabDown.visibility = View.GONE
                } else if (dy < 0) {
                    binding.fabDown.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun initClickListeners() {
        binding.edittextMessage.popupListener = this
        binding.edittextMessage.addTextChangedListener(this)
        binding.recordButton.setRecordView(binding.recordView)
        binding.imageviewPin.setOnClickListener(this)
        imageViewGif.setOnClickListener(this)
        binding.imageviewSend.setOnClickListener(this)
        binding.imageviewBack.setOnClickListener(this)
        binding.layoutGroupInfo.setOnClickListener(this)
        binding.textviewCancel.setOnClickListener(this)
        binding.imageviewCancel.setOnClickListener(this)
        binding.fabDown.setOnClickListener(this)
        binding.textviewUnblock.setOnClickListener(this)
    }

    private fun userProfileData() {
        binding.imageviewProfile
        val userDetailsModel = mAppDatabase!!.getUserDetailsDao().getUserDetails(mToId)
        if (userDetailsModel.isNotEmpty()) {
            Glide.with(this@ChatActivity)
                .setDefaultRequestOptions(glideRequestOptionProfile())
                .load(userDetailsModel[0].profilePicture)
                .into(binding.imageviewProfile)
            binding.textviewName.text = userDetailsModel[0].username
        }
    }

    private fun friendBlockStatus() {
        val mBlockedByYouStatusList =
            mAppDatabase!!.getBlockedUsersDao()
                .checkAlreadyBlockedByYou(mToId, SharedPreferenceEditor.getData(USER_ID))
        if (mBlockedByYouStatusList.isNotEmpty()) {
            binding.edittextMessage.visibility = View.INVISIBLE
            binding.menuChatContainer.visibility = View.INVISIBLE
            binding.textviewUnblock.visibility = View.VISIBLE
        } else {
            binding.textviewUnblock.visibility = View.GONE
            binding.edittextMessage.visibility = View.VISIBLE
            binding.menuChatContainer.visibility = View.VISIBLE
        }
    }

    private fun friendOnlineStatus() {
        val listuserDetails = mAppDatabase!!.getUserDetailsDao().getUserDetails(mToId)
        if (listuserDetails.isNotEmpty()) {
            if (null != listuserDetails[0].lastSeen) {
                when (val mLastSeen = listuserDetails[0].lastSeen) {
                    "ONLINE" -> {

                        binding.textviewStatus.text = resources.getString(R.string.str_online)
                    }
                    "" -> {
                        binding.textviewStatus.text =
                            resources.getString(R.string.tap_to_seen_the_person_info)
                    }
                    else -> {
                        binding.textviewStatus.text =
                            resources.getString(R.string.str_last_seen).plus(" ")
                                .plus(getLastSeenFromMilli(mLastSeen!!.toLong()))
                    }
                }
            } else {
                binding.textviewStatus.text =
                    resources.getString(R.string.tap_to_seen_the_person_info)
            }
        }
    }

    private fun enablePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.RECORD_AUDIO
                    )
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.RECORD_AUDIO),
                        BaseUtils.AUDIO_REQUEST_CODE
                    )
                } else {
                    BaseUtils.snackBarAction(
                        this,
                        binding.layoutParent,
                        resources.getString(R.string.hint_permissions)
                    )
                }
            } else if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        BaseUtils.STORAGE_REQUEST_CODE
                    )
                } else {
                    BaseUtils.snackBarAction(
                        this,
                        binding.layoutParent,
                        resources.getString(R.string.hint_permissions)
                    )
                }
            } else if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.CAMERA
                    )
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.CAMERA),
                        BaseUtils.CAMERA_REQUEST_CODE
                    )
                } else {
                    BaseUtils.snackBarAction(
                        this,
                        binding.layoutParent,
                        resources.getString(R.string.hint_permissions)
                    )
                }
            }
        }
    }

    override fun afterTextChanged(s: Editable?) {
        if (isEditedMsg) {
            if (listChat[itemPosition].message == binding.edittextMessage.text.toString()) {
                binding.imageviewSend.visibility = View.INVISIBLE
                binding.recordButton.visibility = View.INVISIBLE
                binding.imageviewCancel.visibility = View.VISIBLE
            } else {
                if (binding.edittextMessage.text.toString().trim().isEmpty()) {
                    isEditedMsg = false
                    binding.imageviewCancel.visibility = View.GONE
                    binding.imageviewSend.visibility = View.GONE
                    binding.recordButton.visibility = View.VISIBLE
                } else {
                    binding.imageviewCancel.visibility = View.GONE
                    binding.imageviewSend.visibility = View.VISIBLE
                }
            }
        } else {
            if (s.toString().isNotEmpty()) {
                binding.recordButton.visibility = View.INVISIBLE
                binding.imageviewSend.visibility = View.VISIBLE
            } else {
                binding.imageviewSend.visibility = View.INVISIBLE
                binding.recordButton.visibility = View.VISIBLE
            }
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        try {
            Handler().postDelayed(object : Runnable {
                override fun run() {
                    if (s.toString().isNotEmpty()) {
                        val chatTypingModel = ChatTypingModel()
                        chatTypingModel.sender = SharedPreferenceEditor.getData(USER_ID)
                        chatTypingModel.receiver = mToId
                        val chatTypingJson = Gson().toJson(chatTypingModel)
                        val jsonObject = JSONObject(chatTypingJson)
                        ChatApp.mSocketHelper?.sendSingleChatTyping(jsonObject)
                    }
                }
            }, 500)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_media) {
            startActivity(
                Intent(this, MediaActivity::class.java)
                    .putExtra("chat_type", ChatTypes.SINGLE.type)
                    .putExtra("name", binding.textviewName.text.toString())
                    .putExtra("toId", mToId)
            )
        } else if (item.itemId == R.id.menu_clear_chat) {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Are you sure you want to clear messages in this messenger?")
            builder.setCancelable(true)
            builder.setPositiveButton("CLEAR", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    dialog?.cancel()
                    // recycled views remove
                    binding.rvChat.recycledViewPool.clear()
                    val listChatClear =
                        mAppDatabase!!.getChatDao()
                            .getChat(mToId, SharedPreferenceEditor.getData(USER_ID))
                    if (listChatClear.isNotEmpty()) {
                        mAppDatabase!!.getChatDao().deleteList(listChatClear)
                    }
                    var listChatNew = ArrayList<ChatModel>()
                    listChatNew =
                        mAppDatabase!!.getChatDao()
                            .getChat(
                                mToId,
                                SharedPreferenceEditor.getData(USER_ID)
                            ) as ArrayList<ChatModel>
                    listChatNew = sentUpdatedData(listChatNew)
                    listChatNew.reverse()
                    mChatAdapter.updateMessageListItems(
                        listChatNew,
                        null,
                        mChatModelDefault
                    )
                    val listChatRecent =
                        mAppDatabase!!.getChatListDao()
                            .getChatList(mToId, SharedPreferenceEditor.getData(USER_ID))
                    if (listChatRecent.isNotEmpty()) {
                        listChatRecent[0].lastMessage = ""
                        mAppDatabase!!.getChatListDao().update(listChatRecent[0])
                        EventBus.getDefault().post("message_update")
                    }
                }
            })
            builder.setNegativeButton("CANCEL", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    dialog?.cancel()
                }
            })
            val alertDialog = builder.create()
            alertDialog.show()
        } else if (item.itemId == R.id.menu_block) {
            if (mMenuBlock?.title == resources.getString(R.string.string_block)) {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("block user")
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
                        mBlockUserSocketModel.sender = SharedPreferenceEditor.getData(USER_ID)
                        mBlockUserSocketModel.blockUser = mToId
                        val mBlockUserJson = Gson().toJson(mBlockUserSocketModel)
                        val mJsonObject = JSONObject(mBlockUserJson)
                        mSocketHelper?.blockUser(mJsonObject)
                    }
                })
                builder.setNegativeButton("No", object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        dialog?.dismiss()
                    }
                })
                val alertDialog = builder.create()
                alertDialog.show()
            } else if (mMenuBlock?.title == resources.getString(R.string.string_unblock)) {
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
                        mBlockUserSocketModel.sender = SharedPreferenceEditor.getData(USER_ID)
                        mBlockUserSocketModel.unblockUser = mToId
                        val mBlockUserJson = Gson().toJson(mBlockUserSocketModel)
                        val mJsonObject = JSONObject(mBlockUserJson)
                        mSocketHelper?.unblockUser(mJsonObject)
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
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        RxMusicPlayer.stop(this)
        mAppDatabase!!.getChatDao().deleteUnreadMessage(
            ChatMessageTypes.UNREAD.type, mToId,
            SharedPreferenceEditor.getData(USER_ID)
        )
    }

    //TODO: fun to initialize and load hide/show GIF
    private fun gif() {
        val menus = arrayListOf(
            GridMenuAdapter.Menu(
                "Gallery",
                R.drawable.ic_attachment_gallery
            ),
            GridMenuAdapter.Menu(
                "Camera",
                R.drawable.ic_attachment_camera
            ),
            /*GridMenuAdapter.Menu("Document",
                                 R.drawable.ic_attachment_document),
            GridMenuAdapter.Menu("Live Location",
                                 R.drawable.ic_attachment_location),*/
            GridMenuAdapter.Menu(
                "Share Gif",
                R.drawable.ic_attachment_gif
            ),
            GridMenuAdapter.Menu(
                "Location",
                R.drawable.ic_attachment_location
            )
        )
        menuKeyboard =
            SoftKeyBoardPopup(
                this,
                binding.layoutParent,
                binding.edittextMessage,
                binding.edittextMessage,
                binding.menuChatContainer
            )
        SoftKeyBoardPopup.view.findViewById<MenuRecyclerView>(R.id.rvMenu).adapter =
            GridMenuAdapter(menus)
        ItemClickSupport.addTo(SoftKeyBoardPopup.view.findViewById<MenuRecyclerView>(R.id.rvMenu))
            .setOnItemClickListener(object : ItemClickSupport.OnItemClickListener {
                override fun onItemClicked(recyclerView: RecyclerView, position: Int, v: View) {
                    dismissPopup()
                    if (menus[position].name == "Gallery") {
                        showMediaPickerDialog()
                    } else if (menus[position].name == "Camera") {
                        showMediaCaptureDialog()
                    } else if (menus[position].name == "Document") {
                        documentPicker()
                    } else if (menus[position].name == "Live Location") {
                        showToast(this@ChatActivity, "Coming Soon!")
                    } else if (menus[position].name == "Share Gif") {
                        if (isOnline(this@ChatActivity)) {
                            if (binding.layoutGif.root.visibility == View.VISIBLE) {
                                showHideKeyboard(this@ChatActivity, false, binding.edittextMessage)
                                binding.layoutGif.root.visibility = View.GONE
                            } else {
                                showHideKeyboard(this@ChatActivity, true, binding.edittextMessage)
                                mProgressBar.visibility = View.VISIBLE
                                binding.layoutGif.root.visibility = View.VISIBLE
                                mRecyclerViewGif.visibility = View.GONE
                                chatViewModel.getGifData()
                            }
                        } else {
                            snackBar(
                                this@ChatActivity,
                                binding.layoutParent,
                                resources.getString(R.string.no_internet)
                            )
                        }
                    } else if (menus[position].name == "Location") {
                        shareLocation()
                    }
                }
            })
    }

    private fun listenObservers() {
        gifObserver()
        uploadImageObserver()

    }

    private fun gifObserver() {
        chatViewModel.gifResponse.observe(this) { response ->
            when (response.status) {
                Resource.Status.SUCCESS -> {
                    updateGifData(response)
                }
            }
        }

    }

    private fun updateGifData(response: Resource<GifResponse>) {
        response.data?.data?.let {
            if (it.size > 0) {
                mRecyclerViewGif.visibility = View.VISIBLE
                mProgressBar.visibility = View.GONE
                listGif.clear()
                listGif.addAll(it)
                mGifAdapter.notifyDataSetChanged()
            }
        }

    }

    private fun uploadImageObserver() {
        uploadMediaViewModel.commonResponse.observe(this, Observer { response ->
            when (response.status) {
                Resource.Status.LOADING -> {

                }
                Resource.Status.SUCCESS -> {
                    updateImageUi(response)
                }
                Resource.Status.ERROR -> {

                }

            }
        })
    }

    private fun updateImageUi(response: Resource<CommonResponse>) {
        response.data?.let {
            if (it.status) {
                val mFileUrl = it.data.url
                var mMessageId: String = ""
                var file: File? = null
                if (it.data.imageName.contains(".x-wav")) {
                    mMessageId = it.data.imageName.replace(".x-wav", "")
                } else if (it.data.imageName.contains(".mp4")) {
                    mMessageId = it.data.imageName.replace(".mp4", "")
                } else if (it.data.imageName.contains(".jpeg")) {
                    mMessageId = it.data.imageName.replace(".jpeg", "")
                }
                val listChat = mAppDatabase!!.getChatDao()
                    .getChatData(mMessageId, SharedPreferenceEditor.getData(USER_ID))
                if (listChat.isNotEmpty()) {
                    if (listChat[0].messageType == ChatMessageTypes.AUDIO.type) {
                        if (null != pickImageFromStorage(
                                this@ChatActivity,
                                listChat[0].messageId,
                                3
                            )
                        ) {
                            file =
                                File(
                                    pickImageFromStorage(
                                        this@ChatActivity,
                                        listChat[0].messageId,
                                        3
                                    )!!
                                )
                        }
                    } else if (listChat[0].messageType == ChatMessageTypes.VIDEO.type) {
                        if (null != pickImageFromStorage(
                                this@ChatActivity,
                                listChat[0].messageId,
                                2
                            )
                        ) {
                            file =
                                File(
                                    pickImageFromStorage(
                                        this@ChatActivity,
                                        listChat[0].messageId,
                                        2
                                    )!!
                                )
                        }
                    } else if (listChat[0].messageType == ChatMessageTypes.IMAGE.type) {
                        if (null != pickImageFromStorage(
                                this@ChatActivity,
                                listChat[0].messageId,
                                1
                            )
                        ) {
                            file =
                                File(
                                    pickImageFromStorage(
                                        this@ChatActivity,
                                        listChat[0].messageId,
                                        1
                                    )!!
                                )
                        }
                    } else if (listChat[0].messageType == ChatMessageTypes.DOCUMENT.type) {
                        if (null != pickImageFromStorage(
                                this@ChatActivity,
                                listChat[0].messageId,
                                4
                            )
                        ) {
                            file =
                                File(
                                    pickImageFromStorage(
                                        this@ChatActivity,
                                        listChat[0].messageId,
                                        4
                                    )!!
                                )
                        }
                    }
                    val mName =
                        if (listChat[0].messageType == ChatMessageTypes.DOCUMENT.type) {
                            file!!.nameWithoutExtension
                        } else {
                            file!!.name
                        }
                    val chatModel = createMessage().apply {
                        messageType = listChat[0].messageType
                        receiver = listChat[0].receiver
                        uri = mFileUrl
                        message = mName
                    }

                    /* if (listChat[0].messageType == ChatMessageTypes.AUDIO.type) {
                         chatModel.duration = listChat[0].duration
                     }*/
                    val chatJson = Gson().toJson(chatModel)
                    val jsonObject = JSONObject(chatJson)
                    ChatApp.mSocketHelper?.sendSingleChat(jsonObject)
                }
            }
        }


    }

    private fun createMessage(): ChatModel {
        val chatModel = ChatModel().apply {
            checkForwarded = false
            checkReply = false
            messageStatus = ChatMessageStatus.NOT_SENT.ordinal
            sender = SharedPreferenceEditor.getData(USER_ID)
            chatTime = getUTCTime()
            chatType = ChatTypes.SINGLE.type
            checkBlockedByUser = mAppDatabase!!.getBlockedUsersDao()
                .checkAlreadyBlockedByUser(mToId, SharedPreferenceEditor.getData(USER_ID))
                .isNotEmpty()
        }

        return chatModel

    }


    fun updateSeekBar(
        audioHolder: ChatAdapter.AudioViewHolder,
        curPosition: Int,
        chatModel: ChatModel,
        sender: String,
        mMessageId: String,
        isSameAudio: Boolean
    ) {
        this.sender = sender
        this.mMessageId = mMessageId
        this.chatModel = chatModel
        if (mMedia != null) {
            if (!isSameAudio) {
                compositeDisposable.clear()
                mMedia!!.playStop()
                mMedia = null
            } else {
                mMedia!!.playStop()
            }
        }

        if (!isSameAudio) {
            holder = audioHolder
            mChatModelDefault = chatModel
            //playing Media Player
            var duration: Int = 0
            if (null != listChat[curPosition].duration) {
                duration = listChat[curPosition].duration!!
            }
            var uri: String? = null
            when {
                listChat[curPosition].sender == SharedPreferenceEditor.getData(USER_ID) -> {
                    if (null != BaseUtils.loadAudioPath(this, listChat[curPosition].messageId, 1)) {
                        uri =
                            BaseUtils.loadAudioPath(this, listChat[curPosition].messageId, 1)
                    } else {
                        uri = listChat[curPosition].uri!!
                    }
                }
                else -> {
                    uri = if (null != BaseUtils.loadAudioPath(
                            this,
                            listChat[curPosition].messageId,
                            2
                        )
                    ) {
                        BaseUtils.loadAudioPath(this, listChat[curPosition].messageId, 2)
                    } else {
                        listChat[curPosition].uri
                    }
                }
            }
            val media = Media(
                listChat[curPosition].messageId,
                "",
                "",
                "",
                uri,
                duration
            )
            Log.e("Nive ", "updateSeekBar:After click load $uri")

            mMedia = media
            mMedia!!.playStop()
            //------------------------playing Media Player
            if (sender != SharedPreferenceEditor.getData(USER_ID)) {
                val listCheckAudioPlayed =
                    mAppDatabase!!.getAudioStatusDao()
                        .checkAlreadyExists(mMessageId, SharedPreferenceEditor.getData(USER_ID))
                if (listCheckAudioPlayed.isEmpty()) {
                    val mAudioMesage = AudioMessage()
                    mAudioMesage.messageId = mMessageId
                    mAudioMesage.receiver = mToId
                    mAudioMesage.sender = SharedPreferenceEditor.getData(USER_ID)
                    mAudioMesage.status = "3"
                    val mAudioPlayedModel = AudioPlayedModel()
                    mAudioPlayedModel.sender = SharedPreferenceEditor.getData(USER_ID)
                    mAudioPlayedModel.receiver = mToId
                    mAudioPlayedModel.message = mAudioMesage
                    val audioPlayedJson = Gson().toJson(mAudioPlayedModel)
                    val jsonObject = JSONObject(audioPlayedJson)
                    ChatApp.mSocketHelper?.audioPlayed(jsonObject)
                }
            }
            if (sender == SharedPreferenceEditor.getData(USER_ID)) {
                mSeekBar = audioHolder.seekarBarSentAudio
                textViewRuntimeAudioSent = audioHolder.textViewSentAudioRunTime
                textViewTotalTimeAudioSent = audioHolder.textViewSentAudioTotalTime
                imageViewPlaySendAudioNew = audioHolder.imageViewPlaySentAudio
            } else {
                mSeekBar = audioHolder.seekarBarReceivedAudio
                textViewRuntimeAudioReceived = audioHolder.textViewReceivedAudioRunTime
                textViewTotalTimeAudioReceived = audioHolder.textViewReceivedAudioTotalTime
                imageViewPlayReceivedAudioNew = audioHolder.imageViewPlayReceivedAudio
            }
            compositeDisposable = CompositeDisposable()
            if (listChat.isNotEmpty()) {
                compositeDisposable.add(
                    RxMusicPlayer.state
                        .distinctUntilChanged()
                        .doOnError { e ->
                            Log.e(
                                "Nive ",
                                "updatePlayBackStates ErrorHandler: ${e.message}"
                            )
                        }
                        .subscribe { state ->
                            when (state) {
                                is PlaybackState.Buffering -> {
                                    if (state.media!!.id == mChatModelDefault!!.messageId) {
                                        if (sender == SharedPreferenceEditor.getData(USER_ID)) {
                                            if (chatModel.isPlaying) {
                                                imageViewPlaySendAudioNew.setImageResource(R.drawable.ic_audio_pause)
                                            } else {
                                                val listAudioStatus =
                                                    mAppDatabase!!.getAudioStatusDao()
                                                        .checkAlreadyExists(
                                                            mMessageId,
                                                            SharedPreferenceEditor.getData(USER_ID)
                                                        )
                                                if (listAudioStatus.isNotEmpty()) {
                                                    if (listAudioStatus[0].checkPlayed) {
                                                        imageViewPlaySendAudioNew.setImageResource(
                                                            R.drawable.ic_audio_played
                                                        )
                                                    } else {
                                                        imageViewPlaySendAudioNew.setImageResource(
                                                            R.drawable.ic_audio_play
                                                        )
                                                    }
                                                } else {
                                                    imageViewPlaySendAudioNew.setImageResource(
                                                        R.drawable.ic_audio_play
                                                    )
                                                }
                                            }
                                        } else {
                                            if (chatModel.isPlaying) {
                                                imageViewPlayReceivedAudioNew.setImageResource(R.drawable.ic_audio_pause)
                                            } else {
                                                val listAudioStatus =
                                                    mAppDatabase!!.getAudioStatusDao()
                                                        .checkAlreadyExists(
                                                            mMessageId,
                                                            SharedPreferenceEditor.getData(USER_ID)
                                                        )
                                                if (listAudioStatus.isNotEmpty()) {
                                                    if (listAudioStatus[0].checkPlayed) {
                                                        imageViewPlayReceivedAudioNew.setImageResource(
                                                            R.drawable.ic_audio_played
                                                        )
                                                    } else {
                                                        imageViewPlayReceivedAudioNew.setImageResource(
                                                            R.drawable.ic_audio_play
                                                        )
                                                    }
                                                } else {
                                                    imageViewPlayReceivedAudioNew.setImageResource(
                                                        R.drawable.ic_audio_play
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                is PlaybackState.Playing -> {
                                    if (sender == SharedPreferenceEditor.getData(USER_ID)) {
                                        if (chatModel.isPlaying) {
                                            audioHolder.imageViewPlaySentAudio.setImageResource(
                                                R.drawable.ic_audio_pause
                                            )
                                        }
                                    } else {
                                        if (chatModel.isPlaying) {
                                            audioHolder.imageViewPlayReceivedAudio.setImageResource(
                                                R.drawable.ic_audio_pause
                                            )
                                        }
                                    }
                                }
                                is PlaybackState.Paused -> {
                                    if (sender == SharedPreferenceEditor.getData(USER_ID)) {
                                        val listAudioStatus = mAppDatabase!!.getAudioStatusDao()
                                            .checkAlreadyExists(
                                                mMessageId,
                                                SharedPreferenceEditor.getData(USER_ID)
                                            )
                                        if (listAudioStatus.isNotEmpty()) {
                                            if (listAudioStatus[0].checkPlayed) {
                                                imageViewPlaySendAudioNew.setImageResource(R.drawable.ic_audio_played)
                                            } else {
                                                imageViewPlaySendAudioNew.setImageResource(R.drawable.ic_audio_play)
                                            }
                                        } else {
                                            imageViewPlaySendAudioNew.setImageResource(R.drawable.ic_audio_play)
                                        }
                                    } else {
                                        val listAudioStatus = mAppDatabase!!.getAudioStatusDao()
                                            .checkAlreadyExists(
                                                mMessageId,
                                                SharedPreferenceEditor.getData(USER_ID)
                                            )
                                        if (listAudioStatus.isNotEmpty()) {
                                            if (listAudioStatus[0].checkPlayed) {
                                                imageViewPlayReceivedAudioNew.setImageResource(
                                                    R.drawable.ic_audio_played
                                                )
                                            } else {
                                                imageViewPlayReceivedAudioNew.setImageResource(
                                                    R.drawable.ic_audio_play
                                                )
                                            }
                                        } else {
                                            imageViewPlayReceivedAudioNew.setImageResource(
                                                R.drawable.ic_audio_play
                                            )
                                        }
                                    }
                                }
                                is PlaybackState.Completed -> {
                                    setAudioPauseFun()
                                }
                                is PlaybackState.Stopped -> {
                                    // At this state MediaService gets destroyed, so RxMusicPlayer.start needs to be called again
                                    RxMusicPlayer.start(this)
                                    Log.e("Nive ", "updateSeekBar:Stopped ")
                                }
                            }
                        })
            }

            mSeekBar.setOnSeekBarChangeListener(object :
                SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar,
                    progress: Int,
                    fromUser: Boolean
                ) {
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                    Log.e("Nive ", "Changed:Event ")
                    media.seek(seekBar.progress.toLong())
                    if (sender == SharedPreferenceEditor.getData(USER_ID)) {
                        preventDoubleClick(imageViewPlaySendAudioNew)
                    } else {
                        preventDoubleClick(imageViewPlayReceivedAudioNew)
                    }
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                }
            })

            compositeDisposable.add(
                RxMusicPlayer.position
                    .observeOn(AndroidSchedulers.mainThread())
                    .distinctUntilChanged()
                    .doOnError { e ->
                        Log.e(
                            "Nive ",
                            "updateSeekBar ErrorHandler : ${e.message}"
                        )
                    }
                    .subscribe { position ->
                        //set seekbar progress using time played
                        progressValue =
                            getProgressPercentage(position, media.duration!!.toLong())
                        val animation = ObjectAnimator.ofInt(
                            mSeekBar,
                            "progress",
                            progressValue
                        )
                        animation.interpolator = DecelerateInterpolator()
                        animation.start()
                        if (sender == SharedPreferenceEditor.getData(USER_ID)) {
                            textViewRuntimeAudioSent.visibility = View.VISIBLE
                            textViewTotalTimeAudioSent.visibility = View.VISIBLE
                            textViewRuntimeAudioSent.text =
                                getHumanTimeText(position)
                            textViewTotalTimeAudioSent.text =
                                "/ " + getHumanTimeText(media.duration.toLong())
                        } else {
                            textViewRuntimeAudioReceived.visibility = View.VISIBLE
                            textViewTotalTimeAudioReceived.visibility = View.VISIBLE
                            textViewRuntimeAudioReceived.text =
                                getHumanTimeText(position)
                            textViewTotalTimeAudioReceived.text =
                                "/ " + getHumanTimeText(media.duration.toLong())
                        }
                    })
        }
    }

    private fun setAudioPauseFun() {
        if (sender == SharedPreferenceEditor.getData(USER_ID)) {
            val listAudioStatus = mAppDatabase!!.getAudioStatusDao()
                .checkAlreadyExists(mMessageId, SharedPreferenceEditor.getData(USER_ID))
            if (listAudioStatus.isNotEmpty()) {
                if (listAudioStatus[0].checkPlayed) {
                    imageViewPlaySendAudioNew.setImageResource(R.drawable.ic_audio_played)
                } else {
                    imageViewPlaySendAudioNew.setImageResource(R.drawable.ic_audio_play)
                }
            } else {
                imageViewPlaySendAudioNew.setImageResource(R.drawable.ic_audio_play)
            }
            textViewRuntimeAudioSent.visibility = View.GONE
            textViewTotalTimeAudioSent.visibility = View.GONE
            chatModel.isPlaying = false
            chatModel.isPaused = false
            mChatAdapter.selectedPosition = -1
            mChatAdapter.notifyItemChanged(holder.adapterPosition)
            mSeekBar.progress = 0
            compositeDisposable.clear()
            mMedia = null
            mChatModelDefault = null
            preventDoubleClick(imageViewPlaySendAudioNew)
        } else {
            val listAudioStatus = mAppDatabase!!.getAudioStatusDao()
                .checkAlreadyExists(mMessageId, SharedPreferenceEditor.getData(USER_ID))
            if (listAudioStatus.isNotEmpty()) {
                if (listAudioStatus[0].checkPlayed) {
                    imageViewPlayReceivedAudioNew.setImageResource(
                        R.drawable.ic_audio_played
                    )
                } else {
                    imageViewPlayReceivedAudioNew.setImageResource(
                        R.drawable.ic_audio_play
                    )
                }
            } else {
                imageViewPlayReceivedAudioNew.setImageResource(
                    R.drawable.ic_audio_play
                )
            }
            textViewRuntimeAudioReceived.visibility = View.GONE
            textViewTotalTimeAudioReceived.visibility = View.GONE
            chatModel.isPlaying = false
            chatModel.isPaused = false
            mChatAdapter.selectedPosition = -1
            mChatAdapter.notifyItemChanged(holder.adapterPosition)
            mSeekBar.progress = 0
            compositeDisposable.clear()
            mMedia = null
            mChatModelDefault = null
            preventDoubleClick(imageViewPlayReceivedAudioNew)
        }
    }

    override fun onClick(v: View?) {
        preventDoubleClick(v!!)
        when (v.id) {
            R.id.imageview_back -> {
                onBackPressed()
            }
            R.id.fab_down -> {
                binding.rvChat.post(object : Runnable {
                    override fun run() {
                        binding.rvChat.scrollToPosition(0)
                        binding.fabDown.visibility = View.GONE
                    }
                })
            }
            R.id.imageview_pin -> {
                toggle()
            }
            R.id.imageview_gif -> {
                mProgressBar.visibility = View.VISIBLE
                mRecyclerViewGif.visibility = View.GONE
                chatViewModel.getGifData()
            }
            R.id.imageview_send -> {
                when {
                    binding.edittextMessage.text.toString().trim().isEmpty() -> {
                        snackBar(this, binding.layoutParent, "Enter your message")
                    }
                    isEditedMsg -> {
                        sendEdittedMessage()
                    }
                    else -> {
                        sendMessage()
                    }
                }
            }
            R.id.layout_group_info -> {
                startActivity(
                    Intent(this, UserInfoActivity::class.java)
                        .putExtra("id", mToId)
                )
            }
            R.id.textview_cancel -> {
                var pastVisiblesItems =
                    mLayoutManager.findFirstVisibleItemPosition()
                if (pastVisiblesItems > 0) {
                    binding.fabDown.visibility = View.VISIBLE
                }
                isReply = false
                binding.textviewUsername.text = null
                binding.textviewReplyContent.text = null
                binding.imageviewReplyContent.visibility = View.GONE
                binding.layoutReply.visibility = View.GONE
            }
            R.id.imageview_cancel -> {
                isEditedMsg = false
                binding.edittextMessage.text = null
                binding.imageviewCancel.visibility = View.GONE
                binding.imageviewSend.visibility = View.GONE
                binding.recordButton.visibility = View.VISIBLE
            }
            R.id.textview_unblock -> {
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
                        mBlockUserSocketModel.sender = SharedPreferenceEditor.getData(USER_ID)
                        mBlockUserSocketModel.unblockUser = mToId
                        val mBlockUserJson = Gson().toJson(mBlockUserSocketModel)
                        val mJsonObject = JSONObject(mBlockUserJson)
                        mSocketHelper?.unblockUser(mJsonObject)
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

    private fun toggle() {
        if (menuKeyboard.isShowing) {
            menuKeyboard.dismiss()
        } else {
            menuKeyboard.show()
        }
    }

    override fun getPopup(): PopupWindow {
        return menuKeyboard
    }

    /*TODO: fun to choose media type of capture*/
    private fun showMediaPickerDialog() {
        val dialog =
            AlertDialog.Builder(this)
        dialog.setTitle(resources.getString(R.string.str_select_option))
        val items =
            arrayOf(
                resources.getString(R.string.str_pictures),
                resources.getString(R.string.str_video)
            )
        // dialog.setMessage("*for your security reason we blocked!");
        dialog.setItems(items) { dialog, which -> // TODO Auto-generated method stub
            when (which) {
                0 -> {
                    val intent = Intent(
                        this@ChatActivity,
                        PhotoEditActivity::class.java
                    )
                    intent.putExtra("disableMultiple", 1)
                    intent.putExtra("isFrom", 0)
                    intent.putExtra("toId", mToId)
                    startActivityForResult(intent, MULTIPLE_IMAGE_SELECT)
                }
                1 -> {
                    val intent = Intent(
                        this@ChatActivity,
                        VideoPickActivity::class.java
                    )
                    intent.putExtra(IS_NEED_CAMERA, false)
                    intent.putExtra(IS_NEED_FOLDER_LIST, true)
                    intent.putExtra(Constant.MAX_NUMBER, 1)
                    startActivityForResult(intent, Constant.REQUEST_CODE_PICK_VIDEO)
                }
            }
        }
        dialog.show()
    }

    /*TODO: fun to choose media type of capture*/
    private fun showMediaCaptureDialog() {
        val dialog =
            AlertDialog.Builder(this)
        dialog.setTitle(resources.getString(R.string.str_select_option))
        val items =
            arrayOf(
                resources.getString(R.string.str_camera),
                resources.getString(R.string.str_video)
            )
        // dialog.setMessage("*for your security reason we blocked!");
        dialog.setItems(items) { dialog, which -> // TODO Auto-generated method stub
            when (which) {
                0 -> openImage()
                1 -> openVideo()
            }
        }
        dialog.show()
    }

    /*TODO: fun to open default camera to capture*/
    private fun openImage() {
        CropImage
            .activity(null)
            .setGuidelines(CropImageView.Guidelines.ON)
            .start(this)
    }

    /*TODO: fun to open default camera video to record*/
    private fun openVideo() {
        val takeVideoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        startActivityForResult(takeVideoIntent, MY_REQUEST_CODE_VIDEO)
    }

    /*TODO: fun to check runtime permission for storage*/
    private fun documentPicker() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    MY_REQUEST_CODE_DOCUMENT
                )
            } else {
                getDocument()
            }
        } else {
            getDocument()
        }
    }

    /*TODO: fun to open folder containing documents*/
    private fun getDocument() {
        val intent = Intent(this, NormalFilePickActivity::class.java)
        intent.putExtra(IS_NEED_FOLDER_LIST, true)
        intent.putExtra(Constant.MAX_NUMBER, 1)
        intent.putExtra(
            NormalFilePickActivity.SUFFIX,
            arrayOf("pdf")
        )
        startActivityForResult(intent, DOCUMENT_PICKER_SELECT)
    }

    /**
     * Method call for PlacePicker to shareLocation
     */
    private fun shareLocation() {
        /*val builder = PingPlacePicker.IntentBuilder()
        builder.setAndroidApiKey(resources.getString(R.string.str_map_key))
            .setMapsApiKey(resources.getString(R.string.str_map_key))
        // If you want to set a initial location rather then the current device location.
        // NOTE: enable_nearby_search MUST be true.
        // builder.setLatLng(LatLng(37.4219999, -122.0862462))
        try {
            val placeIntent = builder.build(this)
            startActivityForResult(placeIntent, PLACE_PICKER_REQUEST)
        } catch (ex: Exception) {
            ex.printStackTrace()
            Timber.tag("Krish").e("location: ${ex.message}")
        }*/
    }

    /**
     * initialize media audio recorder & media source and starts the audio recording
     */
    private fun startRecording() {
        try {
            /*val folder = File(Environment.getExternalStorageDirectory()
                                  .toString() + "/Bee Bush Messenger/Voice")
            if (!folder.isDirectory) {
                folder.mkdirs()
            }*/
            val mMessageId =
                getUTCTime().plus("-").plus(SharedPreferenceEditor.getData(USER_ID)).plus("-")
                    .plus(mToId)
            val cw = ContextWrapper(applicationContext)
            val folder = cw.getDir("Voice", Context.MODE_PRIVATE)
            val file = File(folder, getMessageId(mMessageId) + ".wav")
            Log.e("Nive ", "startRecording: $file")
            if (!file.exists()) {
                Log.e("Nive ", "startRecording:write")
                var fos: FileOutputStream? = null
                try {
                    fos = FileOutputStream(file)
                    fos.flush()
                    fos.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            Log.e("Nive ", "startRecording:exists ${file.exists()}")

            outputFile = file.absolutePath
            recorder = WaveRecorder(outputFile)
                .apply { waveConfig = recordingConfig }
            recorder.startRecording()
            /*mAudioRecorder = MediaRecorder()
            mAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            mAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mAudioRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            mAudioRecorder.setOutputFile(outputFile)
            mAudioRecorder.prepare()
            mAudioRecorder.start()*/
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * method to stop audio recorder and post to DB
     */
    private fun stopRecording(value: Int) {
        /*try {
            mAudioRecorder.stop()
            mAudioRecorder.reset()
            mAudioRecorder.release()
        } catch (stopException: RuntimeException) {
            //handle cleanup here
        }*/

        recorder.stopRecording()

        if (!TextUtils.isEmpty(outputFile)) {
            val mp =
                MediaPlayer.create(this@ChatActivity, Uri.parse(outputFile))
            if (mp != null) {
                if (value == 1) {
                    duration = mp.duration
                    if (duration <= 1000) {
                        Log.e("Nive ", "stopRecording:Not Sent $duration")
                    } else {
                        Log.e("Nive ", "stopRecording Sent: $duration")
                        updateImageDocument(outputFile, ChatMessageTypes.AUDIO.type)
                    }
                }
            } else {
                Timber.tag("Krish").e("stopRecording: Empty Value")
            }
        }
    }

    /**
     * method to get the timeformat 00:00 from milliseconds
     */
    @SuppressLint("DefaultLocale")
    private fun getHumanTimeText(milliseconds: Long): String? {
        return java.lang.String.format(
            "%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(milliseconds),
            TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                    TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(
                            milliseconds
                        )
                    )
        )
    }

    private fun sendEdittedMessage() {
        isEditedMsg = false
        if (listChat.isNotEmpty() && mListCountOnLongClick != null) {
            if (listChat.size > mListCountOnLongClick!!) {
                itemPosition += (listChat.size - mListCountOnLongClick!!)
            } else if (listChat.size < mListCountOnLongClick!!) {
                itemPosition -= (mListCountOnLongClick!! - listChat.size)
            }
        }
        val chatModel = listChat[itemPosition]
        chatModel.message = binding.edittextMessage.text.toString()
        val mChatModel = ChatModel()
        mChatModel.checkForwarded = chatModel.checkForwarded
        mChatModel.checkReply = chatModel.checkReply
        mChatModel.message = chatModel.message
        mChatModel.messageId = chatModel.messageId
        mChatModel.messageStatus = chatModel.messageStatus
        mChatModel.messageType = chatModel.messageType
        mChatModel.receiver = mToId
        mChatModel.sender = SharedPreferenceEditor.getData(USER_ID)
        mChatModel.chatTime = chatModel.chatTime
        mChatModel.chatType = chatModel.chatType
        mChatModel.checkEdit = true
        mChatModel.replyMessageId = chatModel.replyMessageId
        val mListCheckBlockedbyUser =
            mAppDatabase!!.getBlockedUsersDao()
                .checkAlreadyBlockedByUser(mToId, SharedPreferenceEditor.getData(USER_ID))
        chatModel.checkBlockedByUser = mListCheckBlockedbyUser.isNotEmpty()

        mAppDatabase!!.getChatDao().update(mChatModel)
        var newListChat = ArrayList<ChatModel>()
        newListChat =
            mAppDatabase!!.getChatDao()
                .getChat(mToId, SharedPreferenceEditor.getData(USER_ID)) as ArrayList<ChatModel>
        newListChat.reverse()
        mChatAdapter.updateMessageListItems(
            newListChat,
            binding.rvChat,
            mChatModelDefault
        )
        val chatJson = Gson().toJson(mChatModel)
        val jsonObject = JSONObject(chatJson)
        ChatApp.mSocketHelper?.sendSingleChat(jsonObject)
        binding.edittextMessage.setText("")
    }

    @SuppressLint("SimpleDateFormat")
    private fun sendMessage() {
        CoroutineScope(Dispatchers.IO).launch {
            /*  if (listChat.isNotEmpty() && mListCountOnLongClick != null) {
                  if (listChat.size > mListCountOnLongClick!!) {
                      itemPosition += (listChat.size - mListCountOnLongClick!!)
                  } else if (listChat.size < mListCountOnLongClick!!) {
                      itemPosition -= (mListCountOnLongClick!! - listChat.size)
                  }
              }*/
            val mMessageId =
                getUTCTime().plus("-").plus(SharedPreferenceEditor.getData(USER_ID)).plus("-")
                    .plus(mToId)
            val chatModel = createMessage().apply {
                message = binding.edittextMessage.text.toString().trim()
                messageId = getMessageId(mMessageId)
                messageType = ChatMessageTypes.TEXT.type
                receiver = mToId
            }

            /*  if (isReply) {
                  chatModel.checkReply = isReply
                  chatModel.replyMessageId = listChat[itemPosition].messageId
                  if (null != listChat[itemPosition].latitude) {
                      chatModel.latitude = listChat[itemPosition].latitude
                  }
                  if (null != listChat[itemPosition].longitude) {
                      chatModel.longitude = listChat[itemPosition].longitude
                  }
                  if (null != listChat[itemPosition].uri) {
                      chatModel.uri = listChat[itemPosition].uri
                  }
                  if (null != listChat[itemPosition].duration) {
                      chatModel.duration = listChat[itemPosition].duration
                  }
                  isReply = false
                  binding.textviewUsername.text = null
                  binding.textviewReplyContent.text = null
                  binding.imageviewReplyContent.visibility = View.GONE
                  binding.layoutReply.visibility = View.GONE
              }*/

            mAppDatabase!!.getChatDao().insert(chatModel)

            var newListChat = ArrayList<ChatModel>()
            newListChat =
                mAppDatabase!!.getChatDao()
                    .getChat(mToId, SharedPreferenceEditor.getData(USER_ID)) as ArrayList<ChatModel>
            newListChat = sentUpdatedData(newListChat)
            newListChat.reverse()
            mChatAdapter.updateMessageListItems(
                newListChat,
                binding.rvChat,
                mChatModelDefault
            )
            binding.rvChat.setItemViewCacheSize(newListChat.size)

            val chatJson = Gson().toJson(chatModel)
            val jsonObject = JSONObject(chatJson)
            ChatApp.mSocketHelper?.sendSingleChat(jsonObject)
            binding.edittextMessage.setText("")
        }


    }

    private fun chatOptions(position: Int) {
        val dialog = Dialog(this@ChatActivity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.layout_custom_chat_options)
        val layoutCopy = dialog.findViewById<LinearLayout>(R.id.layout_copy)
        val textViewCopy = dialog.findViewById<AppCompatTextView>(R.id.textview_copy)
        val layoutEdit = dialog.findViewById<LinearLayout>(R.id.layout_edit)
        val textViewEdit = dialog.findViewById<AppCompatTextView>(R.id.textview_edit)
        val layoutReply = dialog.findViewById<LinearLayout>(R.id.layout_reply)
        val textViewReply = dialog.findViewById<AppCompatTextView>(R.id.textview_reply)
        val textViewForward = dialog.findViewById<AppCompatTextView>(R.id.textview_forward)
        val textViewDelete = dialog.findViewById<AppCompatTextView>(R.id.textview_delete)
        val layoutDeleteForEveryone =
            dialog.findViewById<LinearLayout>(R.id.layout_delete_for_everyone)
        val textViewDeleteForEveryone =
            dialog.findViewById<AppCompatTextView>(R.id.tv_delete_everyone)

        if (listChat[position].messageType == ChatMessageTypes.TEXT.type) {
            layoutCopy.visibility = View.VISIBLE
        } else {
            layoutCopy.visibility = View.GONE
        }
        val mListCheckBlockedbyYou =
            mAppDatabase!!.getBlockedUsersDao()
                .checkAlreadyBlockedByYou(mToId, SharedPreferenceEditor.getData(USER_ID))
        if (mListCheckBlockedbyYou.isNotEmpty()) {
            layoutReply.visibility = View.GONE
        } else {
            layoutReply.visibility = View.VISIBLE
        }

        if (listChat[position].sender == SharedPreferenceEditor.getData(USER_ID)) {
            if (listChat[position].messageType == ChatMessageTypes.TEXT.type) {
                layoutEdit.visibility = View.VISIBLE
            } else {
                layoutEdit.visibility = View.GONE
            }
            layoutDeleteForEveryone.visibility = View.VISIBLE
        } else {
            layoutEdit.visibility = View.GONE
            layoutDeleteForEveryone.visibility = View.GONE
        }

        textViewCopy.setOnClickListener(View.OnClickListener {
            dialog.dismiss()
            if (listChat.isNotEmpty() && mListCountOnLongClick != null) {
                if (listChat.size > mListCountOnLongClick!!) {
                    itemPosition += (listChat.size - mListCountOnLongClick!!)
                } else if (listChat.size < mListCountOnLongClick!!) {
                    itemPosition -= (mListCountOnLongClick!! - listChat.size)
                }
            }
            val sdk = Build.VERSION.SDK_INT
            if (sdk < Build.VERSION_CODES.HONEYCOMB) {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.text = listChat[itemPosition].message
            } else {
                val clipboard =
                    getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = ClipData.newPlainText("text label", listChat[itemPosition].message)
                clipboard.setPrimaryClip(clip)
            }
        })

        textViewEdit.setOnClickListener(View.OnClickListener {
            dialog.dismiss()
            if (listChat.isNotEmpty() && mListCountOnLongClick != null) {
                if (listChat.size > mListCountOnLongClick!!) {
                    itemPosition += (listChat.size - mListCountOnLongClick!!)
                } else if (listChat.size < mListCountOnLongClick!!) {
                    itemPosition -= (mListCountOnLongClick!! - listChat.size)
                }
            }
            binding.recordButton.visibility = View.GONE
            isEditedMsg = true
            binding.edittextMessage.setText(listChat[itemPosition].message)
            showHideKeyboard(this@ChatActivity, false, binding.edittextMessage)
        })

        textViewReply.setOnClickListener(View.OnClickListener {
            binding.fabDown.visibility = View.GONE
            dialog.dismiss()
            if (listChat.isNotEmpty() && mListCountOnLongClick != null) {
                if (listChat.size > mListCountOnLongClick!!) {
                    itemPosition += (listChat.size - mListCountOnLongClick!!)
                } else if (listChat.size < mListCountOnLongClick!!) {
                    itemPosition -= (mListCountOnLongClick!! - listChat.size)
                }
            }
            reply(itemPosition)
        })

        textViewForward.setOnClickListener(View.OnClickListener {
            dialog.dismiss()
            if (listChat.isNotEmpty() && mListCountOnLongClick != null) {
                if (listChat.size > mListCountOnLongClick!!) {
                    itemPosition += (listChat.size - mListCountOnLongClick!!)
                } else if (listChat.size < mListCountOnLongClick!!) {
                    itemPosition -= (mListCountOnLongClick!! - listChat.size)
                }
            }
            finish()
            startActivity(
                Intent(this, ForwardActivity::class.java)
                    .putExtra("messageForward", listChat[itemPosition])
            )
        })

        textViewDelete.setOnClickListener(View.OnClickListener {
            dialog.dismiss()
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Delete message?")
            builder.setCancelable(true)
            builder.setPositiveButton("DELETE", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    dialog?.cancel()
                    if (listChat.isNotEmpty() && mListCountOnLongClick != null) {
                        if (listChat.size > mListCountOnLongClick!!) {
                            itemPosition += (listChat.size - mListCountOnLongClick!!)
                        } else if (listChat.size < mListCountOnLongClick!!) {
                            itemPosition -= (mListCountOnLongClick!! - listChat.size)
                        }
                    }
                    // while playing if audio is deleted in that case handled
                    mChatAdapter.selectedPosition = -1
                    if (mMedia != null && mMedia!!.isPlaying()) {
                        mMedia!!.playStop()
                        mMedia = null
                        compositeDisposable.clear()
                    }

                    Log.e("Nive ", "onClick:MessageId " + listChat[itemPosition].messageId)
                    val listSingleChat =
                        mAppDatabase!!.getChatDao().getChatData(
                            listChat[itemPosition].messageId,
                            SharedPreferenceEditor.getData(USER_ID)
                        )
                    Log.e("Nive ", "onClick:Total " + Gson().toJson(listSingleChat))
                    if (listSingleChat.isNotEmpty()) {
                        mAppDatabase!!.getChatDao().delete(listSingleChat[0])
                        Log.e("Nive ", "onClick:delete " + Gson().toJson(listSingleChat[0]))
                    }
                    var listChatNew = ArrayList<ChatModel>()
                    listChatNew =
                        mAppDatabase!!.getChatDao()
                            .getChat(
                                mToId,
                                SharedPreferenceEditor.getData(USER_ID)
                            ) as ArrayList<ChatModel>
                    listChatNew = sentUpdatedData(listChatNew)
                    listChatNew.reverse()
                    mChatAdapter.updateMessageListItems(
                        listChatNew,
                        null,
                        mChatModelDefault
                    )
                    val listChatModel =
                        mAppDatabase!!.getChatDao()
                            .getChat(mToId, SharedPreferenceEditor.getData(USER_ID))
                    val listChatRecent =
                        mAppDatabase!!.getChatListDao()
                            .getChatList(mToId, SharedPreferenceEditor.getData(USER_ID))

                    if (listChatRecent.isNotEmpty()) {
                        if (listChatModel.isNotEmpty()) {
                            val chatModel = listChatModel[listChatModel.size - 1]
                            listChatRecent[0].time = chatModel.chatTime
                            if (chatModel.messageType == ChatMessageTypes.TEXT.type) {
                                listChatRecent[0].lastMessage = chatModel.message
                            } else {
                                listChatRecent[0].lastMessage =
                                    getLastMessage(chatModel.messageType)
                            }
                        } else {
                            listChatRecent[0].lastMessage = ""
                        }
                        mAppDatabase!!.getChatListDao().update(listChatRecent[0])
                        EventBus.getDefault().post("message_update")
                    }
                    /*  if (listChat.size == 2) {
                          val listSingleChat =
                              mAppDatabase!!.getChatDao().getChat(listChat[itemPosition].messageId)
                          if (listSingleChat.isNotEmpty()) {
                              mAppDatabase!!.getChatDao().delete(listSingleChat[0])
                          }
                          val listChatDate =
                              mAppDatabase!!.getChatDao()
                                  .getChat(listChat[itemPosition + 1].messageId)
                          val listDateChatId =
                              mAppDatabase!!.getDateDao()
                                  .getDate(listChat[itemPosition + 1].messageId)
                          if (listChatDate.isNotEmpty()) {
                              if (listChatDate[0].messageType == ChatMessageTypes.DATE.type) {
                                  mAppDatabase!!.getChatDao().delete(listChatDate[0])
                              }
                          }
                          if (listDateChatId.isNotEmpty()) {
                              mAppDatabase!!.getDateDao().delete(listDateChatId[0])
                          }
                          var listChatNew = ArrayList<ChatModel>()
                          listChatNew =
                              mAppDatabase!!.getChatDao()
                                  .getChat(mToId,
                                           SharedPreferenceEditor.getData(USER_ID)) as ArrayList<ChatModel>
                          listChatNew.reverse()
                          mChatAdapter.updateMessageListItems(listChatNew,
                                                              null,
                                                              mChatModelDefault)
                          val listChatModel =
                              mAppDatabase!!.getChatDao()
                                  .getChat(mToId, SharedPreferenceEditor.getData(USER_ID))
                          val listChatRecent =
                              mAppDatabase!!.getChatListDao()
                                  .getChatList(mToId, SharedPreferenceEditor.getData(USER_ID))

                          if (listChatRecent.isNotEmpty()) {
                              if (listChatModel.isNotEmpty()) {
                                  val chatModel = listChatModel[listChatModel.size - 1]
                                  listChatRecent[0].time = chatModel.chatTime
                                  if (chatModel.messageType == ChatMessageTypes.TEXT.type) {
                                      listChatRecent[0].lastMessage = chatModel.message
                                  } else {
                                      listChatRecent[0].lastMessage =
                                          getLastMessage(chatModel.messageType)
                                  }
                              } else {
                                  listChatRecent[0].lastMessage = ""
                              }
                              mAppDatabase!!.getChatListDao().update(listChatRecent[0])
                              EventBus.getDefault().post("message_update")
                          }
                      } else {
                          val listSingleChat =
                              mAppDatabase!!.getChatDao().getChat(listChat[itemPosition].messageId)
                          if (listSingleChat.isNotEmpty()) {
                              mAppDatabase!!.getChatDao().delete(listSingleChat[0])
                          }
                          var listChatNew = ArrayList<ChatModel>()
                          listChatNew =
                              mAppDatabase!!.getChatDao()
                                  .getChat(mToId,
                                           SharedPreferenceEditor.getData(USER_ID)) as ArrayList<ChatModel>
                          listChatNew.reverse()
                          mChatAdapter.updateMessageListItems(listChatNew,
                                                              null,
                                                              mChatModelDefault)
                          val listChatModel =
                              mAppDatabase!!.getChatDao()
                                  .getChat(mToId, SharedPreferenceEditor.getData(USER_ID))
                          val listChatRecent =
                              mAppDatabase!!.getChatListDao()
                                  .getChatList(mToId, SharedPreferenceEditor.getData(USER_ID))

                          if (listChatRecent.isNotEmpty()) {
                              if (listChatModel.isNotEmpty()) {
                                  val chatModel = listChatModel[listChatModel.size - 1]
                                  listChatRecent[0].time = chatModel.chatTime
                                  if (chatModel.messageType == ChatMessageTypes.TEXT.type) {
                                      listChatRecent[0].lastMessage = chatModel.message
                                  } else {
                                      listChatRecent[0].lastMessage =
                                          getLastMessage(chatModel.messageType)
                                  }
                              } else {
                                  listChatRecent[0].lastMessage = ""
                              }
                              mAppDatabase!!.getChatListDao().update(listChatRecent[0])
                              EventBus.getDefault().post("message_update")
                          }
                      }*/
                }
            })
            builder.setNegativeButton("CANCEL", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    dialog?.cancel()
                }
            })
            val alertDialog = builder.create()
            alertDialog.show()
        })

        textViewDeleteForEveryone.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                preventDoubleClick(v!!)
                dialog.dismiss()
                val builder = AlertDialog.Builder(this@ChatActivity)
                builder.setMessage("Delete message?")
                builder.setCancelable(true)
                builder.setPositiveButton("DELETE", object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        dialog?.cancel()
                        if (listChat.isNotEmpty() && mListCountOnLongClick != null) {
                            if (listChat.size > mListCountOnLongClick!!) {
                                itemPosition += (listChat.size - mListCountOnLongClick!!)
                            } else if (listChat.size < mListCountOnLongClick!!) {
                                itemPosition -= (mListCountOnLongClick!! - listChat.size)
                            }
                        }
                        // while playing if audio is deleted in that case handled
                        mChatAdapter.selectedPosition = -1
                        if (mMedia != null && mMedia!!.isPlaying()) {
                            mMedia!!.playStop()
                            mMedia = null
                            compositeDisposable.clear()
                        }
                        val mChatDeleteMessageEveryoneSocketModel =
                            ChatDeleteMessageEveryoneSocketModel()
                        mChatDeleteMessageEveryoneSocketModel.receiver = mToId
                        val mMessageModel = ChatDeleteMessageEveryoneSocketModel().MessageModel()
                        mMessageModel.sender = SharedPreferenceEditor.getData(USER_ID)
                        mMessageModel.messageId = listChat[itemPosition].messageId
                        mMessageModel.status = "12"
                        mChatDeleteMessageEveryoneSocketModel.message = mMessageModel
                        val mDeleteEveryoneModel =
                            Gson().toJson(mChatDeleteMessageEveryoneSocketModel)
                        Log.e("TAG", "onClick: " + Gson().toJson(mDeleteEveryoneModel))
                        val jsonObject = JSONObject(mDeleteEveryoneModel)
                        mSocketHelper?.deleteMessageForEveryone(jsonObject)
                    }
                })
                builder.setNegativeButton("CANCEL", object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        dialog?.cancel()
                    }
                })
                val alertDialog = builder.create()
                alertDialog.show()
            }
        })

        dialog.show()
    }

    private fun reply(position: Int) {
        isReply = true
        binding.layoutReply.visibility = View.VISIBLE
        val userDetailsModel = mAppDatabase!!.getUserDetailsDao().getUserDetails(mToId)
        if (listChat[position].sender == SharedPreferenceEditor.getData(USER_ID)) {
            binding.textviewUsername.text = resources.getString(R.string.str_you)
        } else {
            if (userDetailsModel.isNotEmpty()) {
                binding.textviewUsername.text = userDetailsModel[0].username
            }
        }
        if (listChat[position].messageType == ChatMessageTypes.TEXT.type) {
            binding.textviewReplyContent.text = listChat[position].message
            binding.imageviewReplyContent.visibility = View.GONE
        } else if (listChat[position].messageType == ChatMessageTypes.AUDIO.type) {
            binding.textviewReplyContent.text = getLastMessage(listChat[position].messageType)
            binding.imageviewReplyContent.visibility = View.GONE
        } else if (listChat[position].messageType == ChatMessageTypes.IMAGE.type ||
            listChat[position].messageType == ChatMessageTypes.VIDEO.type ||
            listChat[position].messageType == ChatMessageTypes.GIF.type
        ) {
            binding.textviewReplyContent.text = getLastMessage(listChat[position].messageType)
            binding.imageviewReplyContent.visibility = View.VISIBLE
            when (listChat[position].messageType) {
                ChatMessageTypes.VIDEO.type -> {
                    Glide.with(this)
                        .setDefaultRequestOptions(requestOptionsTv()!!)
                        .load(listChat[position].uri)
                        .into(binding.imageviewReplyContent)
                }
                ChatMessageTypes.GIF.type -> {
                    Glide.with(this)
                        .setDefaultRequestOptions(requestOptionsT()!!)
                        .asGif()
                        .apply(
                            RequestOptions().set(
                                GifOptions.DISABLE_ANIMATION,
                                false
                            )
                        )
                        .load(listChat[position].uri)
                        .into(binding.imageviewReplyContent)
                }
                else -> {
                    Glide.with(this)
                        .setDefaultRequestOptions(requestOptionsT()!!)
                        .load(listChat[position].uri)
                        .into(binding.imageviewReplyContent)
                }
            }
        } else if (listChat[position].messageType == ChatMessageTypes.LOCATION.type ||
            listChat[position].messageType == ChatMessageTypes.LIVELOCATION.type
        ) {
            binding.textviewReplyContent.text = getLastMessage(listChat[position].messageType)
            binding.imageviewReplyContent.visibility = View.VISIBLE
            val mMapUrl: String = getStaticMap(
                this, listChat[position].latitude.toString()
                    .plus(",")
                    .plus(listChat[position].longitude.toString())
            )!!
            Glide.with(this)
                .setDefaultRequestOptions(requestOptionsT()!!)
                .load(mMapUrl)
                .into(binding.imageviewReplyContent)
        } else if (listChat[position].messageType == ChatMessageTypes.DOCUMENT.type) {
            binding.textviewReplyContent.text = getLastMessage(listChat[position].messageType)
            binding.imageviewReplyContent.visibility = View.VISIBLE
            Glide.with(this)
                .load(R.drawable.ic_document)
                .into(binding.imageviewReplyContent)
        } else {
            binding.imageviewReplyContent.visibility = View.GONE
            binding.textviewReplyContent.text = resources.getString(R.string.str_reply)
        }
    }

    override fun onDestroy() {
        menuKeyboard.clear()
        super.onDestroy()
        RxMusicPlayer.stop(this)
        EventBus.getDefault().unregister(this)
    }

    override fun dismissPopup() {
        menuKeyboard.dismissPopup()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == BaseUtils.AUDIO_REQUEST_CODE) {
            if (grantResults.isNotEmpty()) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.RECORD_AUDIO
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
                            BaseUtils.STORAGE_REQUEST_CODE
                        )
                    }
                } else {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.RECORD_AUDIO
                        )
                    ) {
                        BaseUtils.snackBarAction(
                            this,
                            binding.layoutParent,
                            resources.getString(R.string.hint_permissions)
                        )
                    } else {
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.RECORD_AUDIO),
                            BaseUtils.AUDIO_REQUEST_CODE
                        )
                    }
                }
            }
        } else if (requestCode == BaseUtils.STORAGE_REQUEST_CODE) {
            if (grantResults.isNotEmpty()) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.CAMERA
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.CAMERA),
                            BaseUtils.CAMERA_REQUEST_CODE
                        )
                    }
                } else {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                    ) {
                        BaseUtils.snackBarAction(
                            this,
                            binding.layoutParent,
                            resources.getString(R.string.hint_permissions)
                        )
                    } else {
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            BaseUtils.STORAGE_REQUEST_CODE
                        )
                    }
                }
            }
        } else if (requestCode == BaseUtils.CAMERA_REQUEST_CODE) {
            if (grantResults.isNotEmpty()) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.CAMERA
                        )
                    ) {
                        BaseUtils.snackBarAction(
                            this,
                            binding.layoutParent,
                            resources.getString(R.string.hint_permissions)
                        )
                    } else {
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.CAMERA),
                            BaseUtils.CAMERA_REQUEST_CODE
                        )
                    }
                }
            }
        } else if (requestCode == MY_REQUEST_CODE_DOCUMENT) {
            getDocument()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onVideoCompressDone(videoCompressModel: VideoCompressModel) {
        Log.e("Nive ", "onVideoCompressDone: ")
        val file = File(videoCompressModel.path)
        val mimeType = URLConnection.guessContentTypeFromName(file.name)
        val requestFile: RequestBody =
            file.asRequestBody(mimeType.toMediaTypeOrNull())
        val body: MultipartBody.Part =
            MultipartBody.Part.createFormData(
                "media", file.name,
                requestFile
            )
        uploadMediaViewModel.uploadImage(body)
    }

    @SuppressLint("SimpleDateFormat")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                MULTIPLE_IMAGE_SELECT -> {
                    /*if (data!!.getStringExtra(Constants.PREF_VIDEO_SELECTED) != null) {
                        mPrefVideo =
                            data.getStringExtra(Constants.PREF_VIDEO_SELECTED)!!
                    }*/
                    val listOfAllImages =
                        data!!.getSerializableExtra("result") as java.util.ArrayList<String>
                    if (listOfAllImages.isNotEmpty()) {
                        getMultipleImage(listOfAllImages)
                    }
                }
                Constant.REQUEST_CODE_PICK_VIDEO -> {
                    val listVideo: ArrayList<VideoFile> =
                        data!!.getParcelableArrayListExtra(Constant.RESULT_PICK_VIDEO)!!
                    val mMessageId =
                        getUTCTime()
                            .plus("-").plus(SharedPreferenceEditor.getData(USER_ID))
                            .plus("-")
                            .plus(mToId)
                    val listChatUpdate =
                        mAppDatabase!!.getChatDao()
                            .getChatData(
                                getMessageId(mMessageId), SharedPreferenceEditor.getData(USER_ID)
                            )

                    if (listChatUpdate.isEmpty()) {
                        val chatModel = ChatModel()
                        chatModel.checkForwarded = false
                        chatModel.checkReply = false
                        chatModel.message = ""
                        chatModel.messageId = getMessageId(mMessageId)
                        chatModel.messageStatus = ChatMessageStatus.NOT_SENT.ordinal
                        chatModel.messageType = ChatMessageTypes.VIDEO.type
                        chatModel.receiver = mToId
                        chatModel.sender = SharedPreferenceEditor.getData(USER_ID)
                        chatModel.uri = "null"
                        chatModel.chatTime = getUTCTime()
                        chatModel.chatType = ChatTypes.SINGLE.type
                        val mListCheckBlockedbyUser =
                            mAppDatabase!!.getBlockedUsersDao()
                                .checkAlreadyBlockedByUser(
                                    mToId,
                                    SharedPreferenceEditor.getData(USER_ID)
                                )
                        chatModel.checkBlockedByUser = mListCheckBlockedbyUser.isNotEmpty()
                        Log.e("Nive ", "onActivityResult:galleryinsert ${getMessageId(mMessageId)}")
                        mAppDatabase!!.getChatDao().insert(chatModel)
                    } else {
                        Log.e("Nive ", "onActivityResult:galleryupdate ${getMessageId(mMessageId)}")

                        mAppDatabase!!.getChatDao().update(listChatUpdate[0])
                    }
                    var newListChat = ArrayList<ChatModel>()
                    newListChat =
                        mAppDatabase!!.getChatDao()
                            .getChat(
                                mToId,
                                SharedPreferenceEditor.getData(USER_ID)
                            ) as ArrayList<ChatModel>
                    newListChat = sentUpdatedData(newListChat)
                    newListChat.reverse()
                    mChatAdapter.updateMessageListItems(
                        newListChat,
                        binding.rvChat,
                        mChatModelDefault
                    )
                    val mData = Data.Builder()
                        .putString("video_path", listVideo[0].path)
                        .putString("message_id", getMessageId(mMessageId))
                        .build()
                    val constraints = Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                    val oneTimeRequest = OneTimeWorkRequest
                        .Builder(VideoCompresserWorker::class.java)
                        .setInputData(mData)
                        .setConstraints(constraints.build()) // i added constraints
                        .build()
                    WorkManager.getInstance(this).enqueue((oneTimeRequest))
                }
                MY_REQUEST_CODE_VIDEO -> {
                    try {
                        var resultUri: Uri? = null
                        if (data != null) {
                            resultUri = data.data
                        }
                        if (null != resultUri) {
                            val filePath: String = ImageFilePath.getPath(this, resultUri)!!
                            val mMessageId =
                                getUTCTime()
                                    .plus("-").plus(SharedPreferenceEditor.getData(USER_ID))
                                    .plus("-")
                                    .plus(mToId)
                            val listChatUpdate =
                                mAppDatabase!!.getChatDao()
                                    .getChatData(
                                        getMessageId(mMessageId),
                                        SharedPreferenceEditor.getData(USER_ID)
                                    )

                            if (listChatUpdate.isEmpty()) {
                                val chatModel = ChatModel()
                                chatModel.checkForwarded = false
                                chatModel.checkReply = false
                                chatModel.message = ""
                                chatModel.messageId = getMessageId(mMessageId)
                                chatModel.messageStatus = ChatMessageStatus.NOT_SENT.ordinal
                                chatModel.messageType = ChatMessageTypes.VIDEO.type
                                chatModel.receiver = mToId
                                chatModel.sender = SharedPreferenceEditor.getData(USER_ID)
                                chatModel.uri = "null"
                                chatModel.chatTime = getUTCTime()
                                chatModel.chatType = ChatTypes.SINGLE.type
                                val mListCheckBlockedbyUser =
                                    mAppDatabase!!.getBlockedUsersDao().checkAlreadyBlockedByUser(
                                        mToId,
                                        SharedPreferenceEditor.getData(USER_ID)
                                    )
                                chatModel.checkBlockedByUser = mListCheckBlockedbyUser.isNotEmpty()
                                mAppDatabase!!.getChatDao().insert(chatModel)
                            } else {
                                mAppDatabase!!.getChatDao().update(listChatUpdate[0])
                            }
                            var newListChat = ArrayList<ChatModel>()
                            newListChat =
                                mAppDatabase!!.getChatDao()
                                    .getChat(
                                        mToId,
                                        SharedPreferenceEditor.getData(USER_ID)
                                    ) as ArrayList<ChatModel>
                            newListChat = sentUpdatedData(newListChat)
                            newListChat.reverse()
                            mChatAdapter.updateMessageListItems(
                                newListChat,
                                binding.rvChat,
                                mChatModelDefault
                            )
                            val mData = Data.Builder()
                                .putString("video_path", filePath)
                                .putString("message_id", getMessageId(mMessageId))
                                .build()
                            val constraints = Constraints.Builder()
                                .setRequiredNetworkType(NetworkType.CONNECTED)
                            val oneTimeRequest = OneTimeWorkRequest
                                .Builder(VideoCompresserWorker::class.java)
                                .setInputData(mData)
                                .setConstraints(constraints.build()) // i added constraints
                                .build()
                            WorkManager.getInstance(this).enqueue((oneTimeRequest))
                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                }
                CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                    val result = CropImage.getActivityResult(data)
                    mUri = result?.uri
                    if (mUri != null) {
                        val filePath: String = ImageFilePath.getPath(this, mUri!!)!!
                        if (null != getMimeType(filePath)) {
                            if (getMimeType(filePath)!!.contains("image")) {
                                var bitmap: Bitmap? = null
                                bitmap = getBitmapFromUri(mUri, this)
                                imgRotate(filePath, bitmap!!)
                            }
                        }
                    }
                }
                DOCUMENT_PICKER_SELECT -> {
                    try {
                        val list: ArrayList<NormalFile> =
                            data!!.getParcelableArrayListExtra(Constant.RESULT_PICK_FILE)!!
                        val filePath = list[0].path
                        if (null != list[0].mimeType) {
                            if (list[0].mimeType == "application/pdf") {
                                try {
                                    val folder =
                                        File(
                                            Environment.getExternalStorageDirectory()
                                                .toString() + "/Bee Bush Messenger/Documents"
                                        )
                                    if (!folder.isDirectory) {
                                        folder.mkdirs()
                                    }
                                    val mMessageId =
                                        getUTCTime().plus("-")
                                            .plus(SharedPreferenceEditor.getData(USER_ID)).plus("-")
                                            .plus(mToId)
                                    val sourceLocation = File(filePath!!)
                                    val targetLocation =
                                        File(
                                            sDocumentDirectoryPath + "/" + getMessageId(
                                                mMessageId
                                            ) + ".pdf"
                                        )
                                    if (sourceLocation.exists()) {
                                        val `in`: InputStream =
                                            FileInputStream(sourceLocation)
                                        val out: OutputStream =
                                            FileOutputStream(targetLocation)
                                        val buf = ByteArray(1024)
                                        var len: Int
                                        while (`in`.read(buf).also { len = it } > 0) {
                                            out.write(buf, 0, len)
                                        }
                                        `in`.close()
                                        out.close()
                                        updateImageDocument(
                                            targetLocation.path,
                                            ChatMessageTypes.DOCUMENT.type,
                                            sourceLocation.name
                                        )
                                    } else {
                                        Timber.tag("Copy file: ").e("failed")
                                    }
                                } catch (e: java.lang.NullPointerException) {
                                    e.printStackTrace()
                                } catch (e: java.lang.Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                        /*if (null != getMimeType(filePath)) {
                            if (getMimeType(filePath)!!.contains("image")) {
                                var bitmap: Bitmap? = null
                                if (data != null) {
                                    bitmap = getBitmapFromUri(resultUri, this)
                                }
                                imgRotate(filePath!!, bitmap!!)
                            } else if (getMimeType(filePath)!!.contains("video")) {
                                try {
                                    val folder =
                                        File(Environment.getExternalStorageDirectory()
                                                 .toString() + "/Bee Bush Messenger/Videos")
                                    if (!folder.isDirectory) {
                                        folder.mkdirs()
                                    }
                                    val sourceLocation = File(filePath!!)
                                    val targetLocation =
                                        File(sVideoDirectoryPath + "/" + sourceLocation.name)
                                    if (sourceLocation.exists()) {
                                        val `in`: InputStream =
                                            FileInputStream(sourceLocation)
                                        val out: OutputStream =
                                            FileOutputStream(targetLocation)
                                        val buf = ByteArray(1024)
                                        var len: Int
                                        while (`in`.read(buf).also { len = it } > 0) {
                                            out.write(buf, 0, len)
                                        }
                                        `in`.close()
                                        out.close()
                                        updateImageDocument(targetLocation.path,
                                                            ChatMessageTypes.VIDEO.type)
                                    } else {
                                        Log.e("Copy file ", "failed.")
                                    }
                                } catch (e: java.lang.NullPointerException) {
                                    e.printStackTrace()
                                } catch (e: java.lang.Exception) {
                                    e.printStackTrace()
                                }
                            } else {
                                updateImageDocument(filePath!!,
                                                    ChatMessageTypes.DOCUMENT.type)
                            }
                        } else {
                            updateImageDocument(filePath!!,
                                                ChatMessageTypes.DOCUMENT.type)
                        }*/
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                }
                PLACE_PICKER_REQUEST -> {
                    //                    val place = PlacePicker.getPlace(data, this)
//                    val place: Place? = PingPlacePicker.getPlace(data!!)
                    val place: Place? = null
                    val mMessageId =
                        getUTCTime().plus("-").plus(SharedPreferenceEditor.getData(USER_ID))
                            .plus("-").plus(mToId)
                    val chatModel = ChatModel()
                    chatModel.checkForwarded = false
                    chatModel.checkReply = false
                    chatModel.message = "Location"
                    chatModel.messageId = getMessageId(mMessageId)
                    chatModel.messageStatus = ChatMessageStatus.NOT_SENT.ordinal
                    chatModel.messageType = ChatMessageTypes.LOCATION.type
                    chatModel.receiver = mToId
                    chatModel.sender = SharedPreferenceEditor.getData(USER_ID)
                    chatModel.chatTime = getUTCTime()
                    chatModel.chatType = ChatTypes.SINGLE.type
                    chatModel.latitude = 0.0
                    chatModel.longitude = 0.0
                    val mListCheckBlockedbyUser =
                        mAppDatabase!!.getBlockedUsersDao()
                            .checkAlreadyBlockedByUser(
                                mToId,
                                SharedPreferenceEditor.getData(USER_ID)
                            )
                    chatModel.checkBlockedByUser = mListCheckBlockedbyUser.isNotEmpty()
                    mAppDatabase!!.getChatDao().insert(chatModel)
                    val mLocationModel = LocationModel()
                    mLocationModel.messageId = chatModel.messageId
                    mLocationModel.latitude = chatModel.latitude!!
                    mLocationModel.longitude = chatModel.longitude!!
                    mAppDatabase!!.getLocationDao().insert(mLocationModel)
                    var newListChat = ArrayList<ChatModel>()
                    newListChat =
                        mAppDatabase!!.getChatDao()
                            .getChat(
                                mToId,
                                SharedPreferenceEditor.getData(USER_ID)
                            ) as ArrayList<ChatModel>
                    newListChat = sentUpdatedData(newListChat)
                    newListChat.reverse()
                    mChatAdapter.updateMessageListItems(
                        newListChat,
                        binding.rvChat,
                        mChatModelDefault
                    )
                    val chatJson = Gson().toJson(chatModel)
                    val jsonObject = JSONObject(chatJson)

                    ChatApp.mSocketHelper?.sendSingleChat(jsonObject)
                    binding.edittextMessage.setText("")
                }
                PHOTO_EDIT_CODE -> {
                    val filePath = data!!.getStringExtra("imagePath")
                    updateImageDocument(filePath!!, ChatMessageTypes.IMAGE.type)
                }
            }
        } else {
            Timber.tag("Krish").e("result: $resultCode")
            Timber.tag("Krish").e("result: $data")
        }
    }

    private fun imgRotate(filePath: String, bitmap: Bitmap) {
        try {
            val ei = ExifInterface(filePath)
            val orientation = ei.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
            )
            val rotatedBitmap: Bitmap
            rotatedBitmap = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> TransformationUtils.rotateImage(
                    bitmap,
                    90
                )
                ExifInterface.ORIENTATION_ROTATE_180 -> TransformationUtils.rotateImage(
                    bitmap,
                    180
                )
                ExifInterface.ORIENTATION_ROTATE_270 -> TransformationUtils.rotateImage(
                    bitmap,
                    270
                )
                ExifInterface.ORIENTATION_NORMAL -> bitmap
                else -> bitmap
            }
            val intent = Intent(this, PhotoEditNewActivity::class.java)
            intent.putExtra("imageUri", getImageUri(rotatedBitmap).toString())
            intent.putExtra("toId", mToId)
            startActivityForResult(intent, PHOTO_EDIT_CODE)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun getImageUri(inImage: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path =
            MediaStore.Images.Media.insertImage(
                contentResolver, inImage,
                "IMG_" + Calendar.getInstance().time, null
            )
        return Uri.parse(path)
    }

    private fun getMultipleImage(multipleImage: ArrayList<String>) {
        showProgressDialog(this)
        for (i in multipleImage.indices) {
            if (i == multipleImage.size - 1) {
                removeProgressDialog()
            }
            updateImageDocument(multipleImage[i], ChatMessageTypes.IMAGE.type)
            /*if (mPrefVideo.isEmpty()) {
                updateImageDocument(multipleImage[i], ChatMessageTypes.IMAGE.type)
            } else {
                val videoPathList =
                    java.util.ArrayList<String>()
                try {
                    val folder =
                        File(Environment.getExternalStorageDirectory()
                                 .toString() + "/Bee Bush Messenger/Videos")
                    if (!folder.isDirectory) {
                        folder.mkdirs()
                    }
                    val mMessageId =
                        getUTCTime().plus("-").plus(SharedPreferenceEditor.getData(USER_ID)).plus("-").plus(mToId)
                    val sourceLocation = File(multipleImage[i])
                    val targetLocation =
                        File(sVideoDirectoryPath + "/" + getMessageId(mMessageId) + ".mp4")
                    if (sourceLocation.exists()) {
                        val `in`: InputStream = FileInputStream(sourceLocation)
                        val out: OutputStream = FileOutputStream(targetLocation)
                        val buf = ByteArray(1024)
                        var len: Int
                        while (`in`.read(buf).also { len = it } > 0) {
                            out.write(buf, 0, len)
                        }
                        `in`.close()
                        out.close()
                    } else {
                        Log.e("Copy file ", "failed.")
                    }
                    videoPathList.add(targetLocation.absolutePath)
                } catch (e: NullPointerException) {
                    e.printStackTrace()
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
                updateImageDocument(videoPathList[i], ChatMessageTypes.VIDEO.type)
                mPrefVideo = ""
            }*/
        }
    }

    /*TODO: fun to upload files to server*/
    private fun updateImageDocument(mFilePath: String, mType: String) {
        try {
            val file = File(mFilePath)
            binding.edittextMessage.text = null
            var mMessageId = ""
            if (mType == ChatMessageTypes.IMAGE.type) {
                mMessageId = StringUtils.substringBetween(mFilePath, "app_Photos/", ".jpg")
            } else if (mType == ChatMessageTypes.VIDEO.type) {
                mMessageId = StringUtils.substringBetween(mFilePath, "app_Videos/", ".mp4")
            } else if (mType == ChatMessageTypes.AUDIO.type) {
                mMessageId = StringUtils.substringBetween(mFilePath, "app_Voice/", ".wav")
            } else if (mType == ChatMessageTypes.DOCUMENT.type) {
                val messageId =
                    getUTCTime().plus("-").plus(SharedPreferenceEditor.getData(USER_ID)).plus("-")
                        .plus(mToId)
                mMessageId = getMessageId(messageId)
            }
            val listChatUpdate =
                mAppDatabase!!.getChatDao()
                    .getChatData(mMessageId, SharedPreferenceEditor.getData(USER_ID))

            if (listChatUpdate.isEmpty()) {
                val chatModel = ChatModel()
                chatModel.checkForwarded = false
                chatModel.checkReply = false
                chatModel.message = file.name
                chatModel.messageId = mMessageId
                chatModel.messageStatus = ChatMessageStatus.NOT_SENT.ordinal
                chatModel.messageType = mType
                chatModel.receiver = mToId
                chatModel.sender = SharedPreferenceEditor.getData(USER_ID)
                chatModel.uri = "null"
                chatModel.chatTime = getUTCTime()
                chatModel.chatType = ChatTypes.SINGLE.type
                val mListCheckBlockedbyUser =
                    mAppDatabase!!.getBlockedUsersDao()
                        .checkAlreadyBlockedByUser(mToId, SharedPreferenceEditor.getData(USER_ID))
                chatModel.checkBlockedByUser = mListCheckBlockedbyUser.isNotEmpty()
                if (mType == ChatMessageTypes.AUDIO.type) {
                    chatModel.duration = duration
                }
                mAppDatabase!!.getChatDao().insert(chatModel)
            } else {
                mAppDatabase!!.getChatDao().update(listChatUpdate[0])
            }
            var newListChat = ArrayList<ChatModel>()
            newListChat =
                mAppDatabase!!.getChatDao()
                    .getChat(mToId, SharedPreferenceEditor.getData(USER_ID)) as ArrayList<ChatModel>
            newListChat = sentUpdatedData(newListChat)
            newListChat.reverse()
            mChatAdapter.updateMessageListItems(
                newListChat,
                binding.rvChat,
                mChatModelDefault
            )
            val mimeType = URLConnection.guessContentTypeFromName(file.name)
            val requestFile: RequestBody =
                file.asRequestBody(mimeType.toMediaTypeOrNull())
            val body: MultipartBody.Part =
                MultipartBody.Part.createFormData(
                    "media", file.name,
                    requestFile
                )
            uploadMediaViewModel.uploadImage(body)
        } catch (e: java.lang.Exception) {
            showToast(this, "Can't Open file. Please Select another file.")
            e.printStackTrace()
        }
    }

    /*TODO: fun to upload files to server*/
    @SuppressLint("SimpleDateFormat")
    private fun updateImageDocument(mFilePath: String, mType: String, mName: String) {
        try {
            binding.edittextMessage.text = null
            var mMessageId = ""
            if (mType == ChatMessageTypes.DOCUMENT.type) {
                //document type
                mMessageId = StringUtils.substringBetween(mFilePath, "Documents/", ".pdf")
            }
            val file = File(mFilePath)
            val chatModel = ChatModel()
            chatModel.checkForwarded = false
            chatModel.checkReply = false
            chatModel.message = mName
            chatModel.messageId = mMessageId
            chatModel.messageStatus = ChatMessageStatus.NOT_SENT.ordinal
            chatModel.messageType = mType
            chatModel.receiver = mToId
            chatModel.sender = SharedPreferenceEditor.getData(USER_ID)
            chatModel.uri = "null"
            chatModel.chatTime = getUTCTime()
            mAppDatabase!!.getChatDao().insert(chatModel)
            var newListChat = ArrayList<ChatModel>()
            newListChat =
                mAppDatabase!!.getChatDao()
                    .getChat(mToId, SharedPreferenceEditor.getData(USER_ID)) as ArrayList<ChatModel>
            newListChat = sentUpdatedData(newListChat)
            newListChat.reverse()
            mChatAdapter.updateMessageListItems(
                newListChat,
                binding.rvChat,
                mChatModelDefault
            )
            val mimeType = URLConnection.guessContentTypeFromName(file.name)
            val requestFile: RequestBody =
                file.asRequestBody(mimeType.toMediaTypeOrNull())
            val body: MultipartBody.Part =
                MultipartBody.Part.createFormData(
                    "media", file.name,
                    requestFile
                )
            uploadMediaViewModel.uploadImage(body)
        } catch (e: java.lang.Exception) {
            showToast(this, "Can't Open file. Please Select another file.")
            e.printStackTrace()
        }
    }

    override fun videoOptions(chatModel: ChatModel) {
        if (mMedia != null && mMedia!!.isPlaying()) {
            mMedia!!.playStop()
            setAudioPauseFun()
        }
        if (chatModel.sender == SharedPreferenceEditor.getData(USER_ID)) {
            val cw = ContextWrapper(applicationContext)
            val folder = cw.getDir("Videos", Context.MODE_PRIVATE)
            val videoDirectoryPath =
                folder.absolutePath.plus("/").plus(chatModel.messageId).plus(".mp4")
            if (null != pickImageFromStorage(this, chatModel.messageId, 2)) {
                startActivity(
                    Intent(this, ExoPlayerActivity::class.java)
                        .putExtra("URL", videoDirectoryPath)
                )
            } else {
                startActivity(
                    Intent(this, ExoPlayerActivity::class.java)
                        .putExtra("URL", chatModel.uri)
                )
            }
        } else {
            if ("null" != chatModel.uri) {
                val videoDirectoryPath =
                    Environment.getExternalStorageDirectory().toString().plus("/")
                        .plus(Environment.DIRECTORY_PICTURES).plus("/")
                        .plus("Bee Bush Messenger Videos").plus("/").plus(chatModel.messageId)
                        .plus(".mp4")
                if (null != loadVideoPath(chatModel.messageId)) {
                    startActivity(
                        Intent(this, ExoPlayerActivity::class.java)
                            .putExtra("URL", videoDirectoryPath)
                    )
                } else {
                    startActivity(
                        Intent(this, ExoPlayerActivity::class.java)
                            .putExtra("URL", chatModel.uri)
                    )
                }
            }
        }
    }

    override fun imageOptions(chatModel: ChatModel) {
        if (chatModel.sender == SharedPreferenceEditor.getData(USER_ID)) {
            val cw = ContextWrapper(applicationContext)
            val folder = cw.getDir("Photos", Context.MODE_PRIVATE)
            val imageDirectoryPath =
                folder.absolutePath.plus("/").plus(chatModel.messageId).plus(".jpg")
            if (null != pickImageFromStorage(this, chatModel.messageId, 1)) {
                startActivity(
                    Intent(this, ImageZoomActivity::class.java)
                        .putExtra("value", 1)
                        .putExtra("PATH", imageDirectoryPath)
                )
            } else {
                startActivity(
                    Intent(this, ImageZoomActivity::class.java)
                        .putExtra("value", 2)
                        .putExtra("URL", chatModel.uri)
                )
            }
        } else {
            if ("null" != chatModel.uri) {
                if (null != loadVideoPath(chatModel.messageId)) {
                    startActivity(
                        Intent(this, ImageZoomActivity::class.java)
                            .putExtra("value", 1)
                            .putExtra("PATH", loadVideoPath(chatModel.messageId))
                    )
                } else {
                    startActivity(
                        Intent(this, ImageZoomActivity::class.java)
                            .putExtra("value", 2)
                            .putExtra("URL", chatModel.uri)
                    )
                }
            }
        }
    }

    override fun documentOptions(chatModel: ChatModel) {
        if ("null" != chatModel.uri) {
            if (chatModel.sender == SharedPreferenceEditor.getData(USER_ID)) {
                var mFilePath: String? = null
                mFilePath = if (null != pickImageFromStorage(
                        this,
                        chatModel.messageId,
                        4
                    )
                ) {
                    pickImageFromStorage(this, chatModel.messageId, 4)
                } else {
                    chatModel.uri
                }
                val target = Intent(Intent.ACTION_VIEW)
                target.setDataAndType(Uri.fromFile(File(mFilePath!!)), "application/pdf")
                target.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                val intent = Intent.createChooser(target, "Open File")
                try {
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    // Instruct the user to install a PDF reader here, or something
                }
            } else {
                /* var mFilePath: String? = null
                mFilePath = if (listLocalData.isNotEmpty()) {
                    if (validDownload(this, listLocalData[0].downloadId)) {
                        if (null != pickImageFromStorage(this, chatModel.messageId, 4)) {
                            pickImageFromStorage(this, chatModel.messageId, 4)
                        } else {
                            chatModel.uri
                        }
                    } else {
                        chatModel.uri
                    }
                } else {
                    chatModel.uri
                }
                val target = Intent(Intent.ACTION_VIEW)
                target.setDataAndType(Uri.fromFile(File(mFilePath!!)), "application/pdf")
                target.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                val intent = Intent.createChooser(target, "Open File")
                try {
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    // Instruct the user to install a PDF reader here, or something
                }
            }*/
            }
        }
    }

    override fun locationOptions(chatModel: ChatModel) {
        val locationData =
            mAppDatabase!!.getLocationDao()
                .getLocationMessage(chatModel.messageId, SharedPreferenceEditor.getData(USER_ID))
        if (locationData.isNotEmpty()) {
            val mUri =
                Uri.parse(
                    "geo:0,0?q=".plus(locationData.get(0).latitude).plus(",")
                        .plus(locationData.get(0).longitude)
                )
            val mMapIntent = Intent(Intent.ACTION_VIEW, mUri)
            mMapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mMapIntent)
        }
    }

    fun sentUpdatedData(listChatNew: ArrayList<ChatModel>): ArrayList<ChatModel> {
        var tempList = ArrayList<ChatModel>()
        var dateList = ArrayList<String>()
        for (i in listChatNew.indices) {
            var convertedDate = BaseUtils.getDate(
                listChatNew[i].chatTime.toLong(),
                "yyyy-MM-dd"
            )
            if (!dateList.contains(convertedDate)) {
                val chatModelDummy = ChatModel()
                chatModelDummy.receiver = mToId
                chatModelDummy.sender = SharedPreferenceEditor.getData(USER_ID)
                chatModelDummy.messageStatus = ChatMessageStatus.READ.ordinal
                chatModelDummy.messageType = ChatMessageTypes.DATE.type
                chatModelDummy.chatTime = listChatNew[i].chatTime
                tempList.add(chatModelDummy)
                dateList.add(convertedDate)
            }
            tempList.add(listChatNew[i])
        }

        return tempList
        Log.e("Nive ", "sentUpdatedData:Temp " + Gson().toJson(tempList))

    }

    override fun retryOptions(chatModel: ChatModel) {
        retryFile(chatModel)
    }

    override fun replyRedirection(mReplyMessageId: String) {
        if (mReplyMessageId.isNotEmpty()) {
            if (listChat.isNotEmpty()) {
                val childCount = binding.rvChat.childCount
                for (i in listChat.indices) {
                    if (listChat[i].messageId == mReplyMessageId) {
                        binding.rvChat.scrollToPosition(i)
                        var holder: RecyclerView.ViewHolder? = null
                        binding.rvChat.post(object : Runnable {
                            override fun run() {
                                holder = binding.rvChat.findViewHolderForAdapterPosition(i)
                                var pastVisiblesItems =
                                    mLayoutManager.findFirstVisibleItemPosition()
                                if (pastVisiblesItems > 0) {
                                    binding.fabDown.visibility = View.VISIBLE
                                }
                                if (null != holder) {
                                    when (listChat[i].messageType) {
                                        ChatMessageTypes.TEXT.type -> {
                                            if (listChat[i].replyMessageId == null) {
                                                val mHolder =
                                                    holder as ChatAdapter.ChatViewHolder
                                                mHolder.layoutParent.setBackgroundColor(
                                                    resources.getColor(R.color.wheat)
                                                )
                                                Handler(Looper.getMainLooper()).postDelayed(
                                                    object :
                                                        Runnable {
                                                        override fun run() {
                                                            mHolder.layoutParent.setBackgroundColor(
                                                                resources.getColor(android.R.color.transparent)
                                                            )
                                                        }
                                                    },
                                                    1000
                                                )
                                            } else {
                                                val mHolder =
                                                    holder as ChatAdapter.ReplyViewHolder
                                                mHolder.layoutParent.setBackgroundColor(
                                                    resources.getColor(R.color.wheat)
                                                )
                                                Handler(Looper.getMainLooper()).postDelayed(
                                                    object :
                                                        Runnable {
                                                        override fun run() {
                                                            mHolder.layoutParent.setBackgroundColor(
                                                                resources.getColor(android.R.color.transparent)
                                                            )
                                                        }
                                                    },
                                                    1000
                                                )
                                            }
                                        }
                                        ChatMessageTypes.AUDIO.type -> {
                                            val mHolder = holder as ChatAdapter.AudioViewHolder
                                            mHolder.layoutParentAudio.setBackgroundColor(
                                                resources.getColor(R.color.wheat)
                                            )
                                            Handler(Looper.getMainLooper()).postDelayed(object :
                                                Runnable {
                                                override fun run() {
                                                    mHolder.layoutParentAudio.setBackgroundColor(
                                                        resources.getColor(android.R.color.transparent)
                                                    )
                                                }
                                            }, 1000)
                                        }
                                        ChatMessageTypes.VIDEO.type -> {
                                            val mHolder = holder as ChatAdapter.VideoViewHolder
                                            mHolder.layoutVideoParent.setBackgroundColor(
                                                resources.getColor(R.color.wheat)
                                            )
                                            Handler(Looper.getMainLooper()).postDelayed(object :
                                                Runnable {
                                                override fun run() {
                                                    mHolder.layoutVideoParent.setBackgroundColor(
                                                        resources.getColor(android.R.color.transparent)
                                                    )
                                                }
                                            }, 1000)
                                        }
                                        ChatMessageTypes.IMAGE.type -> {
                                            val mHolder = holder as ChatAdapter.ImageViewHolder
                                            mHolder.layoutImageParent.setBackgroundColor(
                                                resources.getColor(R.color.wheat)
                                            )
                                            Handler(Looper.getMainLooper()).postDelayed(object :
                                                Runnable {
                                                override fun run() {
                                                    mHolder.layoutImageParent.setBackgroundColor(
                                                        resources.getColor(android.R.color.transparent)
                                                    )
                                                }
                                            }, 1000)
                                        }
                                        ChatMessageTypes.DOCUMENT.type -> {
                                            val mHolder =
                                                holder as ChatAdapter.DocumentViewHolder
                                            mHolder.layoutDocumentParent.setBackgroundColor(
                                                resources.getColor(R.color.wheat)
                                            )
                                            Handler(Looper.getMainLooper()).postDelayed(object :
                                                Runnable {
                                                override fun run() {
                                                    mHolder.layoutDocumentParent.setBackgroundColor(
                                                        resources.getColor(android.R.color.transparent)
                                                    )
                                                }
                                            }, 1000)
                                        }
                                        ChatMessageTypes.LOCATION.type -> {
                                            val mHolder =
                                                holder as ChatAdapter.LocationViewHolder
                                            mHolder.layoutLocationParent.setBackgroundColor(
                                                resources.getColor(R.color.wheat)
                                            )
                                            Handler(Looper.getMainLooper()).postDelayed(object :
                                                Runnable {
                                                override fun run() {
                                                    mHolder.layoutLocationParent.setBackgroundColor(
                                                        resources.getColor(android.R.color.transparent)
                                                    )
                                                }
                                            }, 1000)
                                        }
                                    }
                                }
                            }
                        })
                    }
                }
            }
        }
    }

    override fun gifOptions(gifData: GifResponse.GifData) {
        if (isOnline(this)) {
            uploadGif(gifData.images.original.url, ChatMessageTypes.GIF.type)
        } else {
            snackBar(this, binding.layoutParent, resources.getString(R.string.no_internet))
        }
    }

    /**
     * Method Call from GifAdapter
     *
     * @param mUrl     - Gif or Sticker url
     * @param mMessageType - To differentiate Gif
     * msgType -> 8
     */
    @SuppressLint("SimpleDateFormat")
    private fun uploadGif(mUrl: String, mMessageType: String) {
        binding.layoutGif.root.visibility = View.GONE
        val mMessageId =
            getUTCTime().plus("-").plus(SharedPreferenceEditor.getData(USER_ID)).plus("-")
                .plus(mToId)
        val messageId = getMessageId(mMessageId)
        val chatModel = ChatModel()
        chatModel.checkForwarded = false
        chatModel.checkReply = false
        chatModel.message = "gif"
        chatModel.messageId = messageId
        chatModel.messageStatus = ChatMessageStatus.NOT_SENT.ordinal
        chatModel.messageType = mMessageType
        chatModel.receiver = mToId
        chatModel.sender = SharedPreferenceEditor.getData(USER_ID)
        chatModel.uri = mUrl
        chatModel.chatTime = getUTCTime()
        chatModel.chatType = ChatTypes.SINGLE.type
        val mListCheckBlockedbyUser =
            mAppDatabase!!.getBlockedUsersDao()
                .checkAlreadyBlockedByUser(mToId, SharedPreferenceEditor.getData(USER_ID))
        chatModel.checkBlockedByUser = mListCheckBlockedbyUser.isNotEmpty()

        mAppDatabase!!.getChatDao().insert(chatModel)
        var newListChat = ArrayList<ChatModel>()
        newListChat =
            mAppDatabase!!.getChatDao()
                .getChat(mToId, SharedPreferenceEditor.getData(USER_ID)) as ArrayList<ChatModel>
        newListChat = sentUpdatedData(newListChat)
        newListChat.reverse()
        mChatAdapter.updateMessageListItems(
            newListChat,
            binding.rvChat,
            mChatModelDefault
        )
        val chatJson = Gson().toJson(chatModel)
        val jsonObject = JSONObject(chatJson)
        ChatApp.mSocketHelper?.sendSingleChat(jsonObject)
        binding.edittextMessage.setText("")
    }

    /*TODO: fun to retry sending items*/
    private fun retryFile(chatModel: ChatModel) {
        Log.e("Nive ", "retryFile: ")
        if (isOnline(this)) {
            try {
                var file: File? = null
                if (chatModel.messageType == ChatMessageTypes.AUDIO.type) {
                    if (null != pickImageFromStorage(this, chatModel.messageId, 3)) {
                        file = File(
                            pickImageFromStorage(
                                this,
                                chatModel.messageId,
                                3
                            )!!
                        )
                    } else {
                        showToast(this, resources.getString(R.string.str_file_not_found))
                        return
                    }
                } else if (chatModel.messageType == ChatMessageTypes.VIDEO.type) {
                    if (null != pickImageFromStorage(this, chatModel.messageId, 2)) {
                        file = File(
                            pickImageFromStorage(
                                this,
                                chatModel.messageId,
                                2
                            )!!
                        )
                    } else {
                        showToast(this, resources.getString(R.string.str_file_not_found))
                        return
                    }
                } else if (chatModel.messageType == ChatMessageTypes.IMAGE.type) {
                    if (null != pickImageFromStorage(this, chatModel.messageId, 1)) {
                        file = File(
                            pickImageFromStorage(
                                this,
                                chatModel.messageId,
                                1
                            )!!
                        )
                    } else {
                        showToast(this, resources.getString(R.string.str_file_not_found))
                        return
                    }
                } else if (chatModel.messageType == ChatMessageTypes.DOCUMENT.type) {
                    if (null != pickImageFromStorage(this, chatModel.messageId, 4)) {
                        file = File(
                            pickImageFromStorage(
                                this,
                                chatModel.messageId,
                                4
                            )!!
                        )
                    } else {
                        showToast(this, resources.getString(R.string.str_file_not_found))
                        return
                    }
                }
                val listChatUpdate =
                    mAppDatabase!!.getChatDao()
                        .getChatData(chatModel.messageId, SharedPreferenceEditor.getData(USER_ID))
                listChatUpdate[0].messageStatus = ChatMessageStatus.NOT_SENT.ordinal
                mAppDatabase!!.getChatDao().update(listChatUpdate[0])
                var newListChat = ArrayList<ChatModel>()
                newListChat =
                    mAppDatabase!!.getChatDao()
                        .getChat(
                            mToId,
                            SharedPreferenceEditor.getData(USER_ID)
                        ) as ArrayList<ChatModel>
                newListChat.reverse()
                mChatAdapter.updateMessageListItems(
                    newListChat,
                    null,
                    mChatModelDefault
                )
                val mimeType = URLConnection.guessContentTypeFromName(file!!.name)
                val requestFile: RequestBody =
                    file.asRequestBody(mimeType.toMediaTypeOrNull())
                val body: MultipartBody.Part =
                    MultipartBody.Part.createFormData(
                        "media", file.name,
                        requestFile
                    )
                uploadMediaViewModel.uploadImage(body)
            } catch (e: java.lang.Exception) {
                showToast(this, resources.getString(R.string.str_file_not_found))
            }
        } else {
            snackBar(this, binding.layoutParent, resources.getString(R.string.no_internet))
        }
    }

}

