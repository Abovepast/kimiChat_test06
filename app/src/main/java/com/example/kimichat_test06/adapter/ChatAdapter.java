package com.example.kimichat_test06.adapter;

import android.text.Spannable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kimichat_test06.R;
import com.example.kimichat_test06.bean.ChatMessage;

import java.util.List;

import io.noties.markwon.Markwon;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    private final List<ChatMessage> chatMessages;
    private final Markwon markwon;
    public String ChatMode;

    public ChatAdapter(List<ChatMessage> chatMessages, Markwon markwon, String ChatMode) {
        this.chatMessages = chatMessages;
        this.markwon = markwon;
        this.ChatMode = ChatMode;
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
        String markdownText = message.getMessage(); // 假设getMessage()返回Markdown文本

        // 使用Markwon渲染Markdown
        Spannable spannable = (Spannable) markwon.toMarkdown(markdownText);
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
            holder.receiverIcon.setVisibility(View.GONE);
            holder.receiverName.setVisibility(View.GONE);
            holder.receiverMessageTextView.setVisibility(View.GONE);
//            holder.userMessageTextView.setText(message.getMessage());
            holder.userMessageTextView.setText(spannable);
        } else {
            holder.userIcon.setVisibility(View.GONE);
            holder.userName.setVisibility(View.GONE);
            holder.userMessageTextView.setVisibility(View.GONE);
            holder.receiverName.setVisibility(View.VISIBLE);
            holder.receiverIcon.setVisibility(View.VISIBLE);
            holder.receiverMessageTextView.setVisibility(View.VISIBLE);
//            holder.receiverMessageTextView.setText(message.getMessage());
            holder.receiverMessageTextView.setText(spannable);
        }

//        int ChatSize = getItemCount();
//        if(position == ChatSize-1 && ) {
//            holder.msgTime_L.setVisibility(View.VISIBLE);
//        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView userIcon;
        public ImageView receiverIcon;
        public TextView userName;
        public TextView receiverName;
        public TextView userMessageTextView;
        public TextView receiverMessageTextView;
        public LinearLayout msgTime_L;


        public ViewHolder(View itemView) {
            super(itemView);
            userMessageTextView = itemView.findViewById(R.id.messageSend);
            receiverMessageTextView = itemView.findViewById(R.id.messageReceive);

            userIcon = itemView.findViewById(R.id.user_icon);
            receiverIcon = itemView.findViewById(R.id.bot_icon);

            userName = itemView.findViewById(R.id.user_name);
            receiverName = itemView.findViewById(R.id.bot_name);

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

