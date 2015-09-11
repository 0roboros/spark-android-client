package com.sparklounge.client.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.sparklounge.client.R;
import com.sparklounge.client.activities.ChatActivity;
import com.sparklounge.client.database.ConversationDb;
import com.sparklounge.client.interfaces.OnItemClickListener;
import com.sparklounge.client.models.Conversation;
import com.sparklounge.client.views.ConversationAdapter;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class MessagesFragment extends Fragment {


    public MessagesFragment() {
        // Required empty public constructor
    }

    private ConversationDb conversationDb;
    private List<Conversation> mConversations;
    private RecyclerView mConversationsRecycler;
    private ConversationAdapter mConversationAdapter;
    private LinearLayoutManager mLayoutManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        conversationDb = new ConversationDb(getActivity());
        mConversations = conversationDb.getAllConversations();
        // TODO: remove, for testing purpsoses only
        if(mConversations.size() == 0) {
            conversationDb.insertTestData();
            mConversations = conversationDb.getAllConversations();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_messages, container, false);

        mConversationsRecycler = (RecyclerView) view.findViewById(R.id.messages_recycler);
        mConversationAdapter = new ConversationAdapter();
        mConversationAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onClick(View v, int position) {
                Conversation conversation = mConversations.get(position);
                Toast.makeText(getActivity(), "You want to chat with: " + mConversations.get(position).getUserInfo().getUserId(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra("friend_name", conversation.getUserInfo().getUserId());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

            }
        });
        mLayoutManager = new LinearLayoutManager(getActivity());
        mConversationsRecycler.setLayoutManager(mLayoutManager);
        mConversationsRecycler.setAdapter(mConversationAdapter);

        mConversationAdapter.updateUserInfo(mConversations);
        return view;
    }

}
