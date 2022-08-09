package com.gps.chat.ui.home.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.gps.chat.databinding.FragmentRecentChatBinding
import com.gps.chat.interfaces.ChatConversation
import com.gps.chat.ui.home.adapter.RecentChatAdapter
import com.gps.chat.utils.ChatResponse
import com.gps.chat.utils.hide
import com.gps.chat.utils.show
import com.sparkout.chat.common.ChatApp
import com.sparkout.chat.common.Global.USER_ID
import com.sparkout.chat.common.SharedPreferenceEditor
import com.sparkout.chat.common.chatenum.ChatTypes
import com.sparkout.chat.ui.chat.model.ChatListModel
import com.sparkout.chat.ui.chat.view.ChatActivity
import com.sparkout.chat.ui.groupchat.view.GroupChatActivity
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class RecentChatFragment(private val homeActivity: HomeActivity) : Fragment(), ChatConversation {

    private lateinit var mConversationAdapter: RecentChatAdapter
    private var listConversation: java.util.ArrayList<ChatListModel> = ArrayList()
    private lateinit var binding: FragmentRecentChatBinding
    var isStarted: Boolean = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentRecentChatBinding.inflate(layoutInflater)
        binding.rvRecent.layoutManager = LinearLayoutManager(homeActivity)
        ChatResponse.conversation(homeActivity, this)

        mConversationAdapter = RecentChatAdapter(homeActivity, listConversation) { chatListModel ->
            if (chatListModel.chatType == ChatTypes.SINGLE.type) {
                startActivity(
                    Intent(homeActivity, ChatActivity::class.java)
                        .putExtra("id", chatListModel.receiverId)
                )
            } else {
                val listGroupDetails = ChatApp.mAppDatabase!!.getGroupDetailsDao()
                    .getGroupDetails(
                        chatListModel.receiverId,
                        SharedPreferenceEditor.getData(USER_ID)
                    )
                if (listGroupDetails.isNotEmpty()) {
                    startActivity(
                        Intent(homeActivity, GroupChatActivity::class.java)
                            .putExtra("id", chatListModel.receiverId)
                    )
                }
            }
        }
        binding.rvRecent.adapter = mConversationAdapter

        return binding.root
    }


    override fun onStart() {
        super.onStart()
        isStarted = true
        EventBus.getDefault().register(this)
        if (isVisible) {
            ChatResponse.conversation(homeActivity, this)
        }
    }

    override fun onStop() {
        super.onStop()
        isStarted = false
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(string: String) {
        if (string != "chat" &&
            string != "user_status" &&
            string != "update_deliver_read" &&
            string != "update_profile" &&
            string != "password_changed"
        ) {
            ChatResponse.conversation(homeActivity, this)
        }
    }


    override fun conversationCallBack(listConversation: List<ChatListModel>) {
        this.listConversation.clear()
        if (listConversation.isNotEmpty()) {
            binding.layoutNoChat.hide()
            binding.rvRecent.show()
            this.listConversation.addAll(listConversation)
            if (this::mConversationAdapter.isInitialized) {
                mConversationAdapter.notifyDataSetChanged()
            }
        } else {
            binding.rvRecent.hide()
            binding.layoutNoChat.show()
        }
    }

}