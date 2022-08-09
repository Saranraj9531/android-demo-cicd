package com.sparkout.chat.ui.chat.view;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import com.sparkout.chat.common.chatenum.ChatMessageTypes;
import com.sparkout.chat.ui.chat.model.ChatModel;

import java.util.List;

public class MessageDiffCallback extends DiffUtil.Callback {

    private final List<ChatModel> moldMesssageList;
    private final List<ChatModel> mnewMesssageList;
    private final ChatModel chatModel;
    private final boolean needUpdate;

    public MessageDiffCallback(List<ChatModel> oldMesssageList, List<ChatModel> newMesssageList, ChatModel chatModel, boolean needUpdate) {
        this.moldMesssageList = oldMesssageList;
        this.mnewMesssageList = newMesssageList;
        this.chatModel = chatModel;
        this.needUpdate = needUpdate;
    }

    @Override
    public int getOldListSize() {
        return moldMesssageList.size();
    }

    @Override
    public int getNewListSize() {
        return mnewMesssageList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return moldMesssageList.get(oldItemPosition).getMessageId().equals(mnewMesssageList.get(
                newItemPosition).getMessageId());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        final ChatModel oldMesssageList = moldMesssageList.get(oldItemPosition);
        final ChatModel newMesssageList = mnewMesssageList.get(newItemPosition);
        boolean isStatus = (oldMesssageList.getCheckEdit() == newMesssageList.getCheckEdit());
        if (isStatus) {
            isStatus = (oldMesssageList.getMessageStatus() == newMesssageList.getMessageStatus());
        }
        if (!isStatus &&
                oldMesssageList.getMessageType().equals(ChatMessageTypes.AUDIO.getType()) &&
                chatModel != null &&
                chatModel.getMessageId().equals(oldMesssageList.getMessageId())) {
            isStatus = true;
        }
        if (!needUpdate) {
            isStatus = false;
        }

        return isStatus;
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        // Implement method if you're going to use ItemAnimator
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}
