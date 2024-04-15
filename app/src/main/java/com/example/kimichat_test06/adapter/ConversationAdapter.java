package com.example.kimichat_test06.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kimichat_test06.R;
import com.example.kimichat_test06.ShowConversation;
import com.example.kimichat_test06.bean.Conversation;
import com.example.kimichat_test06.dao.ChatDatabaseHelper;
import com.example.kimichat_test06.utils.ParentOnTouchChildClickLinearLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ConversationAdapter extends BaseAdapter {

    private final Context context;
    private final List<Conversation> conversationList;
    private float max = 210;

    public ConversationAdapter(Context context, List<Conversation> conversationList) {
        this.context = context;
        this.conversationList = conversationList;
    }

    @Override
    public int getCount() {
        return conversationList.size();
    }

    @Override
    public Object getItem(int position) {
        return conversationList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.item_his_chat, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.saveText = convertView.findViewById(R.id.saveText);
            viewHolder.saveTime = convertView.findViewById(R.id.saveTime);
            viewHolder.saveCID = convertView.findViewById(R.id.saveCID);
            viewHolder.item_lv = convertView.findViewById(R.id.item_lv);
            viewHolder.item_del = convertView.findViewById(R.id.item_del);
            viewHolder.root = convertView.findViewById(R.id.root);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        Conversation conversation = this.conversationList.get(position);
        viewHolder.saveText.setText(conversation.getChatMessages().get(0).getMessage());
        //---------------获取时间并格式化, 设置时间-------------------//
        long startTime = conversation.getStartTimeStamp();
        // 创建一个SimpleDateFormat对象，指定输出的日期/时间格式
        SimpleDateFormat formatter = new SimpleDateFormat("MM月dd日 HH:mm", Locale.getDefault());
        Date date = new Date(startTime);// 将long时间戳转换为Date对象
        String formattedDateString = formatter.format(date);// 将Date对象格式化为字符串
        viewHolder.saveTime.setText(formattedDateString);
        //-------------------------------------------------------//
        viewHolder.saveCID.setText(String.valueOf(conversation.getConversationId()));
        // 点击项目跳转页面显示对话内容
        viewHolder.item_lv.setOnClickListener(v -> {
            Toast.makeText(context, "点击了项目", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent( context, ShowConversation.class);
            intent.putExtra("conversationId", conversation.getConversationId());
            intent.putExtra("conversationName", conversation.getConPayerName());
            intent.putExtra("startTimeStamp", formattedDateString);
            context.startActivity(intent);
        });
        /*
         *  滑动初始化参数
         */
        ViewTreeObserver vto = viewHolder.item_del.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                //监听一次马上结束
                viewHolder.item_del.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                max = viewHolder.item_del.getWidth();
                //得到删除按钮长度 得到最大拖动限定
                Log.i("rex", "max--" + max);
            }
        });
        viewHolder.item_lv.setTranslationX(0);
        viewHolder.item_del.setTranslationX(0);
        convertView.setScaleY(1);
        convertView.setTranslationY(0);

        final View finalView = convertView;
        //------------------删除---------------------//
        viewHolder.item_del.setOnClickListener(v->{
            //删除
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(finalView, "scaleY", 1, 0);
            scaleY.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    //dbHelper.deleteRecord(getItem(position).getSecond());
                    try (ChatDatabaseHelper dbHelper = new ChatDatabaseHelper(context)) {
                        dbHelper.deleteConversation(conversation.getConversationId());
                        conversationList.remove(position);
                        notifyDataSetChanged();
                    }
                }
            });
            scaleY.setDuration(300).start();
            for (int i = 1; i < getCount() - position; i++) {
                ObjectAnimator.ofFloat(getItemId(i + position), "translationY", 0, -finalView.getMeasuredHeight()).setDuration(300).start();
            }
        });

        viewHolder.root.setOnTouchListener(new View.OnTouchListener() {

            private float diff; // 记录两个触摸点之间的水平距离差
            float x = -1; // 记录触摸点的初始X坐标
            float mx; // 记录当前触摸点的X坐标
            boolean isMove; // 标记是否进行了滑动操作

            @SuppressLint("ObjectAnimatorBinding")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (max == 0) {
                    return false;
                }
                // 处理触摸移动事件
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    // 如果x为-1，则表示还未记录初始触摸点，进行初始化
                    if (x == -1)
                        x = event.getRawX();

                    mx = event.getRawX(); // 当前触摸点的X坐标
                    isMove = true; // 标记为滑动状态
                    diff = mx - x; // 计算滑动距离

                    // 限制滑动距离不超过最大值
                    if (diff < -max)
                        diff = -max;

                    // 如果已经滑动到最左且继续滑动，则不再移动
                    if (viewHolder.item_lv.getTranslationX() > 0 && diff > viewHolder.item_lv.getTranslationX())
                        diff = viewHolder.item_lv.getTranslationX();

                    // 更新被滑动视图的位置
                    viewHolder.item_lv.setTranslationX(diff);
                    viewHolder.item_del.setTranslationX(diff);

                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    x = -1; // 重置初始触摸点坐标
                    if (isMove) {
                        // 根据滑动距离判断是恢复原位还是显示删除布局
                        if (diff < -max / 2.0f) {
                            // 如果滑动距离超过一半，则显示删除布局
                            ObjectAnimator.ofFloat(viewHolder.item_lv, "translationX", diff, -max).setDuration(600).start();
                            ObjectAnimator.ofFloat(viewHolder, "translationX", diff, -max).setDuration(600).start();
                        } else {
                            // 否则，恢复原位
                            ObjectAnimator.ofFloat(viewHolder.item_lv, "translationX", diff, 0).setDuration(600).start();
                            ObjectAnimator.ofFloat(viewHolder, "translationX", diff, 0).setDuration(600).start();
                        }
                        return true;
                    } else {
                        // 如果没有进行滑动操作，则不处理
                        return false;
                    }
                }
                return true;
            }
        });

        return convertView;
    }

    private static class ViewHolder {
        TextView saveText;
        TextView saveTime;
        TextView saveCID;

        LinearLayout item_lv;
        LinearLayout item_del;
        ParentOnTouchChildClickLinearLayout root;
    }
}
