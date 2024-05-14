package com.example.kimichat_test06.adapter;

import android.text.Spannable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kimichat_test06.R;
import com.example.kimichat_test06.bean.ChatMessage;

import java.util.List;
import java.util.Objects;

import io.noties.markwon.Markwon;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder>{
    private final List<ChatMessage> chatMessages;
    private final Markwon markwon;
    public String ChatMode;
    //-------语音合成接口回调------//
    private final OnItemClickListener mListener;


    public ChatAdapter(List<ChatMessage> chatMessages, Markwon markwon, String ChatMode, OnItemClickListener listener) {
        this.chatMessages = chatMessages;
        this.markwon = markwon;
        this.ChatMode = ChatMode;
        this.mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        return chatMessages.get(position).isUser() ? 0 : 1;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatMessage message = chatMessages.get(position);
        String msg = message.getMessage(); // 假设getMessage()返回Markdown文本

        // 使用Markwon渲染Markdown
        Spannable markdownMsg = (Spannable) markwon.toMarkdown(msg);
        if (ChatMode.equals("english")) {
            holder.receiverIcon.setImageResource(R.drawable.teacher);
            holder.receiverName.setText(R.string.kunkun_english);
        } else {
            holder.receiverIcon.setImageResource(R.drawable.kunkun);
            holder.receiverName.setText(R.string.bot_name);
        }
        if (message.isUser()) {
            holder.userIcon.setVisibility(View.VISIBLE);
            holder.userName.setVisibility(View.VISIBLE);
            holder.userMessageTextView.setVisibility(View.VISIBLE);
            holder.userVoice.setVisibility(View.VISIBLE);

            holder.receiverIcon.setVisibility(View.GONE);
            holder.receiverName.setVisibility(View.GONE);
            holder.receiverMessageTextView.setVisibility(View.GONE);
            holder.receiveVoice.setVisibility(View.GONE);
//            holder.userMessageTextView.setText(message.getMessage());
            // 使用Markwon渲染Markdown
            holder.userMessageTextView.setText(markdownMsg);
        } else {
            holder.userIcon.setVisibility(View.GONE);
            holder.userName.setVisibility(View.GONE);
            holder.userMessageTextView.setVisibility(View.GONE);
            holder.userVoice.setVisibility(View.GONE);

            holder.receiverName.setVisibility(View.VISIBLE);
            holder.receiverIcon.setVisibility(View.VISIBLE);
            holder.receiverMessageTextView.setVisibility(View.VISIBLE);
            holder.receiveVoice.setVisibility(View.VISIBLE);
//            holder.receiverMessageTextView.setText(message.getMessage());

            // 使用Markwon渲染Markdown
            holder.receiverMessageTextView.setText(markdownMsg);
        }

        // 点击后Voice按钮后将
        holder.userVoice.setOnClickListener(v->{
            // 用户消息
            // 将要播放的消息传回去
            if (mListener != null) {
                mListener.onItemClick(msg);
            }

        });

        holder.receiveVoice.setOnClickListener(v->{
            // Ai消息
            // 同上
            if (mListener != null) {
                mListener.onItemClick(msg);
            }
        });

//        int ChatSize = getItemCount();
//        if(position == ChatSize-1 && ) {
//            holder.msgTime_L.setVisibility(View.VISIBLE);
//        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    public String getItem() {
        // 返回机器人的回复
        return chatMessages.get(chatMessages.size() - 1).getMessage();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView userIcon;
        public ImageView receiverIcon;
        public TextView userName;
        public TextView receiverName;
        public TextView userMessageTextView;
        public TextView receiverMessageTextView;
        public LinearLayout msgTime_L;  // 时间, 待定
        public Button userVoice;
        public Button receiveVoice;


        public ViewHolder(View itemView) {
            super(itemView);
            userMessageTextView = itemView.findViewById(R.id.messageSend);
            receiverMessageTextView = itemView.findViewById(R.id.messageReceive);

            userIcon = itemView.findViewById(R.id.user_icon);
            receiverIcon = itemView.findViewById(R.id.receive_icon);

            userName = itemView.findViewById(R.id.user_name);
            receiverName = itemView.findViewById(R.id.bot_name);

            userVoice = itemView.findViewById(R.id.userVoice);
            receiveVoice = itemView.findViewById(R.id.receiveVoice);

            // todo
            /*
             * 显示时间在消息列表中,且仅有一个
             * 可能的方案:
             * 在Bean中新建属性TimeShowFlag, 只在最后一条消息中显示
             * 如何判断最后一条消息?
             * 1. 在Adapter中, 在onBindViewHolder中, 获取position, 判断是否是最后一条消息
             * 如何判读是否对话结束,仅在对话结束时才有显示时间,并且全局只有一个
             */
            msgTime_L = itemView.findViewById(R.id.msgTime_L);

        }
    }
}

