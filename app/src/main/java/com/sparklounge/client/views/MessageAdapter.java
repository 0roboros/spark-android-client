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
 * Created by Chuang on 9/10/2015.
 */
public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private static final int RECEIVED_MESSAGE = 1;
    private static final int SENT_MESSAGE = 2;

    private List<Conversation> mMessages;
    private OnItemClickListener onItemClickListener;
    private String friendName;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public MessageAdapter(String friendName) {
        this.friendName = friendName;
    }

    public void updateData(List<Conversation> messages) {
        this.mMessages = messages;
        notifyDataSetChanged();
    }

    class RecievedMessageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        protected final ImageView userProfilePic;
        protected final TextView message;
        private final OnItemClickListener onItemClickListener;

        public RecievedMessageViewHolder(View itemView, OnItemClickListener onItemClickListener) {
            super(itemView);

            userProfilePic = (ImageView) itemView.findViewById(R.id.img_user_profile_pic);
            message = (TextView) itemView.findViewById(R.id.txt_msg);

            this.onItemClickListener = onItemClickListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onItemClickListener.onClick(view, getPosition());
        }
    }

    class SentMessageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        protected final TextView message;
        private final OnItemClickListener onItemClickListener;

        public SentMessageViewHolder(View itemView, OnItemClickListener onItemClickListener) {
            super(itemView);
            message = (TextView) itemView.findViewById(R.id.txt_msg);

            this.onItemClickListener = onItemClickListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onItemClickListener.onClick(view, getPosition());

        }
    }


    @Override
    public int getItemViewType(int position) {
        Conversation convo = mMessages.get(position);
        if (friendName.equals(convo.getUserInfo().getUserId())) {
            return RECEIVED_MESSAGE;
        } else {
            return SENT_MESSAGE;
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == RECEIVED_MESSAGE) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.received_message_layout, parent, false);
            return new RecievedMessageViewHolder(itemView, this.onItemClickListener);
        } else {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.sent_message_layout, parent, false);
            return new SentMessageViewHolder(itemView, this.onItemClickListener);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Conversation convo = mMessages.get(position);
        int viewType = getItemViewType(position);
        if(viewType == RECEIVED_MESSAGE) {
            RecievedMessageViewHolder viewHolder = (RecievedMessageViewHolder) holder;
            String profilePicLink = convo.getUserInfo().getProfilePic();
            Bitmap profilePic = SparkApplication.getCache().get(profilePicLink);
            viewHolder.userProfilePic.setImageBitmap(profilePic);
            viewHolder.message.setText(convo.getMessage());
        } else {
            SentMessageViewHolder viewHolder = (SentMessageViewHolder) holder;
            viewHolder.message.setText(convo.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        if (mMessages == null) return 0;
        else return mMessages.size();
    }
}
