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
import com.sparklounge.client.models.UserInfo;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chuang on 9/1/2015.
 */
public class UserInfoAdapter extends RecyclerView.Adapter<UserInfoViewHolder>{

    private List<UserInfo> mUserInfos;
    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void updateUserInfo(List<UserInfo> userInfos) {
        this.mUserInfos = userInfos;
        notifyDataSetChanged();
    }

    @Override
    public UserInfoViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.user_info_card, viewGroup, false);
        return new UserInfoViewHolder(itemView, this.onItemClickListener);
    }

    @Override
    public void onBindViewHolder(final UserInfoViewHolder userInfoViewHolder, final int position) {
        final UserInfo currentUserInfo = mUserInfos.get(position);
        final String profilePiclink = currentUserInfo.getProfilePic();

        Bitmap profilePic = SparkApplication.getCache().get(profilePiclink);
        if (profilePic == null) {
            // TODO: retrieve from local file system
        }
        else {
            userInfoViewHolder.userProfilePic.setImageBitmap(profilePic);
            userInfoViewHolder.username.setText(currentUserInfo.getUserId());
            userInfoViewHolder.userCaption.setText(currentUserInfo.getCaption());
        }
    }

    @Override
    public int getItemCount() {
        if (mUserInfos == null) return 0;
        else return mUserInfos.size();
    }

}

class UserInfoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    protected final ImageView userProfilePic;
    protected final TextView username;
    protected final TextView userCaption;
    private final OnItemClickListener onItemClickListener;

    public UserInfoViewHolder(View itemView, OnItemClickListener onItemClickListener) {
        super(itemView);

        userProfilePic = (ImageView) itemView.findViewById(R.id.img_user_profile_pic);
        username = (TextView) itemView.findViewById(R.id.txt_user_name);
        userCaption = (TextView) itemView.findViewById(R.id.txt_user_caption);

        this.onItemClickListener = onItemClickListener;
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        onItemClickListener.onClick(v, getPosition());
    }
}
