package com.sparklounge.client.views;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sparklounge.client.R;
import com.sparklounge.client.SparkApplication;
import com.sparklounge.client.interfaces.OnItemClickListener;
import com.sparklounge.client.models.Conversation;

import java.util.List;

/**
 * Created by Chuang on 9/8/2015.
 */
public class ConversationAdapter extends RecyclerView.Adapter<ConversationViewHolder>{

    private List<Conversation> mConversations;
    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void updateUserInfo(List<Conversation> conversations) {
        this.mConversations = conversations;
        notifyDataSetChanged();
    }

    @Override
    public ConversationViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.conversation_card, viewGroup, false);
        return new ConversationViewHolder(itemView, this.onItemClickListener);
    }

    @Override
    public void onBindViewHolder(final ConversationViewHolder conversationViewHolder, final int position) {
        final Conversation conversation = mConversations.get(position);
        final String profilePiclink = conversation.getUserInfo().getProfilePic();

        Bitmap profilePic = SparkApplication.getCache().get(profilePiclink);
        if (profilePic == null) {

        }
        if (profilePic != null) {
            conversationViewHolder.userProfilePic.setImageBitmap(profilePic);
        }
        conversationViewHolder.userName.setText(conversation.getUserInfo().getUserId());
        conversationViewHolder.lastMsg.setText(conversation.getMessage());
        conversationViewHolder.lastMsgTime.setText(conversation.getMessageTime());
    }

    @Override
    public int getItemCount() {
        if (mConversations == null) return 0;
        else return mConversations.size();
    }

}

class ConversationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    protected final ImageView userProfilePic;
    protected final TextView userName;
    protected final TextView lastMsg;
    protected final TextView lastMsgTime;
    private final OnItemClickListener onItemClickListener;

    public ConversationViewHolder(View itemView, OnItemClickListener onItemClickListener) {
        super(itemView);

        userProfilePic = (ImageView) itemView.findViewById(R.id.img_user_profile_pic);
        userName = (TextView) itemView.findViewById(R.id.txt_friend_name);
        lastMsgTime = (TextView) itemView.findViewById(R.id.txt_last_msg_time);
        lastMsg = (TextView) itemView.findViewById(R.id.txt_last_msg);

        this.onItemClickListener = onItemClickListener;
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        onItemClickListener.onClick(v, getPosition());
    }
}