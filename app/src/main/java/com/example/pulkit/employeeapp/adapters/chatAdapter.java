package com.example.pulkit.employeeapp.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.pulkit.employeeapp.EmployeeLogin.EmployeeSession;
import com.example.pulkit.employeeapp.R;
import com.example.pulkit.employeeapp.model.ChatMessage;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.example.pulkit.employeeapp.EmployeeApp.DBREF;

public class chatAdapter extends RecyclerView.Adapter<chatAdapter.MyViewHolder> {
    ArrayList<ChatMessage> list = new ArrayList<>();
    private Context context;
    private EmployeeSession session;
    String dbTablekey;
    private SparseBooleanArray selectedItems;
    private static int currentSelectedIndex = -1;
    private SparseBooleanArray animationItemsIndex;
    private boolean reverseAllAnimations = false;
    private ChatAdapterListener listener;
    private HashMap<DatabaseReference, ValueEventListener> commentStatusHashMap, progressListenerHashmap;

    public chatAdapter(ArrayList<ChatMessage> list, Context context, String dbTableKey, ChatAdapterListener listener) {
        this.list = list;
        this.context = context;
        session = new EmployeeSession(context);
        this.dbTablekey = dbTableKey;
        selectedItems = new SparseBooleanArray();
        animationItemsIndex = new SparseBooleanArray();
        this.listener = listener;
        commentStatusHashMap = new HashMap<>();
        progressListenerHashmap = new HashMap<>();

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        ChatMessage comment = list.get(position);
        if (comment.getSenderUId().equals(session.getUsername()))
        {
            holder.parent_layout.setVisibility(View.VISIBLE);
            holder.messageContainer.setBackgroundResource(R.drawable.chatbubble_right);
            holder.parent_layout.setGravity(Gravity.RIGHT);
            holder.parent_layout.setPadding(150, 0, 0, 0);  //(left,top,right,bottom)
            holder.status.setVisibility(View.VISIBLE);
            holder.meSender_Timestampdate.setText(comment.getSendertimestamp().substring(0, 11));
            holder.meSender_Timestamptime.setText(comment.getSendertimestamp().substring(12));
            applyStatus(comment, holder);
            applyprogressbar(comment, holder);
        } else {
            holder.parent_layout.setVisibility(View.VISIBLE);
            holder.parent_layout.setGravity(Gravity.LEFT);
            holder.parent_layout.setPadding(0, 0, 150, 0);
            holder.messageContainer.setBackgroundResource(R.drawable.chatbubble_left);
            holder.meSender_Timestampdate.setText(comment.getSendertimestamp().substring(0, 11));
            holder.meSender_Timestamptime.setText(comment.getSendertimestamp().substring(12));
            holder.status.setVisibility(View.GONE);
            applyprogressbar2(comment, holder);
            if (!comment.getType().equals("text") && comment.getImgurl().equals("nourl"))
                showrow(holder, position);
        }
        applyClickEvents(holder, position);
        String type = comment.getType();
        switch (type) {
            case "text":
                holder.commentString.setVisibility(View.VISIBLE);
                holder.photo.setVisibility(View.GONE);
                holder.progressBar.setVisibility(View.GONE);
                holder.download_chatimage.setVisibility(View.GONE);
                holder.commentString.setText(comment.getCommentString());
                break;

            case "photo":
                holder.commentString.setVisibility(View.GONE);
                holder.photo.setVisibility(View.VISIBLE);
                holder.progressBar.setVisibility(View.GONE);
                holder.download_chatimage.setVisibility(View.GONE);
                if (comment.getSenderUId().equals(session.getUsername())) {
                    if (!comment.getMesenderlocal_storage().equals("")) {
                        holder.photo.setImageURI(Uri.parse(comment.getMesenderlocal_storage()));
                        break;
                    } else {
                        Glide.with(context)
                                .load(Uri.parse(comment.getImgurl()))
                                .placeholder(R.color.black)
                                .crossFade()
                                .centerCrop()
                                .into(holder.photo);
                        break;
                    }
                } else {
                    if (!comment.getOthersenderlocal_storage().equals("")) {
                        holder.download_chatimage.setVisibility(View.GONE);
                        holder.photo.setImageURI(Uri.parse(comment.getOthersenderlocal_storage()));
                        break;
                    } else {
                        holder.download_chatimage.setVisibility(View.VISIBLE);
                        Glide.with(context)
                                .load(Uri.parse(comment.getImgurl()))
                                .placeholder(R.color.black)
                                .crossFade()
                                .centerCrop()
                                .into(holder.photo);
                        break;
                    }
                }

            case "doc":
                holder.commentString.setVisibility(View.GONE);
                holder.photo.setVisibility(View.VISIBLE);
                holder.progressBar.setVisibility(View.GONE);
                holder.download_chatimage.setVisibility(View.GONE);
                if (comment.getSenderUId().equals(session.getUsername())) {
                    if (!comment.getMesenderlocal_storage().equals("")) {
                        Glide.with(context)
                                .load(R.drawable.download_pdf)
                                .crossFade()
                                .centerCrop()
                                .into(holder.photo);
                        break;
                    } else {
                        Glide.with(context)
                                .load(R.drawable.download_pdf)
                                .crossFade()
                                .centerCrop()
                                .into(holder.photo);
                        break;
                    }
                } else {
                    if (!comment.getOthersenderlocal_storage().equals("")) {
                        holder.download_chatimage.setVisibility(View.GONE);
                        Glide.with(context)
                                .load(R.drawable.download_pdf)
                                .crossFade()
                                .centerCrop()
                                .into(holder.photo);
                        break;
                    } else {
                        holder.download_chatimage.setVisibility(View.VISIBLE);
                        Glide.with(context)
                                .load(R.drawable.download_pdf)
                                .crossFade()
                                .centerCrop()
                                .into(holder.photo);
                        break;
                    }
                }
        }
    }

    private void applyStatus(ChatMessage comment, final MyViewHolder holder) {
        final DatabaseReference dbCommentStatus = DBREF.child("Chats").child(dbTablekey).child("ChatMessages").child(comment.getId()).child("status").getRef();
        ValueEventListener dbCommentStatusListener = dbCommentStatus.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String status = dataSnapshot.getValue(String.class);
                    switch (status) {
                        case "0":
                            holder.status.setImageResource(R.mipmap.ic_sent);                   //pending
                            break;
                        case "1":
                            holder.status.setImageResource(R.mipmap.ic_sent);                   //sent
                            break;
                        case "2":
                            holder.status.setImageResource(R.mipmap.ic_delivered);              //delivered
                            break;
                        case "3":
                            holder.status.setImageResource(R.mipmap.ic_read);                   //read
                            dbCommentStatus.removeEventListener(this);
                            break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        if (dbCommentStatusListener != null)
            commentStatusHashMap.put(dbCommentStatus, dbCommentStatusListener);
    }

    private void applyprogressbar(ChatMessage comment, final MyViewHolder holder) {
        final DatabaseReference dbUploadProgress = DBREF.child("Chats").child(dbTablekey).child("ChatMessages").child(comment.getId()).child("imgurl").getRef();
        ValueEventListener dbUploadProgressListener = dbUploadProgress.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String imgurl = dataSnapshot.getValue(String.class);
                    if (imgurl.equals("nourl")) {
                        holder.progressBar.setVisibility(View.VISIBLE);
                    } else {
                        holder.progressBar.setVisibility(View.GONE);
                        dbUploadProgress.removeEventListener(this);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        if (dbUploadProgressListener != null) {
            progressListenerHashmap.put(dbUploadProgress, dbUploadProgressListener);
        }
    }

    private void applyprogressbar2(final ChatMessage comment, final MyViewHolder holder)
    {
        final DatabaseReference dbUploadProgress = DBREF.child("Chats").child(dbTablekey).child("ChatMessages").child(comment.getId()).child("othersenderlocal_storage").getRef();
        final ValueEventListener dbUploadProgressListener = dbUploadProgress.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String othersenderlocal_storage = dataSnapshot.getValue(String.class);
                    if (!othersenderlocal_storage.equals("")) {
                        if (holder.progressBar.getVisibility() == View.VISIBLE) {
                            holder.progressBar.setVisibility(View.GONE);
                            dbUploadProgress.removeEventListener(this);
                            comment.setOthersenderlocal_storage(othersenderlocal_storage);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        if (dbUploadProgressListener != null) {
            progressListenerHashmap.put(dbUploadProgress, dbUploadProgressListener);
        }

    }

    private void showrow( final MyViewHolder holder, final int position)
    {
        final DatabaseReference dbUploadProgress = DBREF.child("Chats").child(dbTablekey).child("ChatMessages").child(list.get(position).getId()).child("imgurl").getRef();
        ValueEventListener dbUploadProgressListener = dbUploadProgress.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String imgurl = dataSnapshot.getValue(String.class);
                    if (imgurl.equals("nourl")) {
                        holder.parent_layout.setVisibility(View.GONE);
                    } else {
                        holder.parent_layout.setVisibility(View.VISIBLE);
                        list.get(position).setImgurl(imgurl);
                        if (list.get(position).getType()=="photo")
                        {
                        Glide.with(context)
                                .load(Uri.parse(list.get(position).getImgurl()))
                                .placeholder(R.color.black)
                                .crossFade()
                                .centerCrop()
                                .into(holder.photo);
                    }
                    else
                        {
                            Glide.with(context)
                                    .load(R.drawable.download_pdf)
                                    .crossFade()
                                    .centerCrop()
                                    .into(holder.photo);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        if (dbUploadProgressListener != null) {
            progressListenerHashmap.put(dbUploadProgress, dbUploadProgressListener);
        }
    }

    public void showProgressBar(final MyViewHolder holder) {
        holder.download_chatimage.setVisibility(View.GONE);
        holder.progressBar.setVisibility(View.VISIBLE);
    }

    public void dismissProgressBar(final MyViewHolder holder) {
        holder.progressBar.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView meSender_Timestampdate, meSender_Timestamptime, commentString;
        LinearLayout parent_layout, messageContainer;
        ImageView photo, status;

        ProgressBar progressBar;
        ImageButton download_chatimage;

        public MyViewHolder(View itemView) {
            super(itemView);
            messageContainer = (LinearLayout) itemView.findViewById(R.id.sender_message_container);
            parent_layout = (LinearLayout) itemView.findViewById(R.id.parent_layout);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progress);
            status = (ImageView) itemView.findViewById(R.id.status);
            download_chatimage = (ImageButton) itemView.findViewById(R.id.download_chatimage);
            meSender_Timestampdate = (TextView) itemView.findViewById(R.id.meSender_TimeStampdate);
            meSender_Timestamptime = (TextView) itemView.findViewById(R.id.meSender_TimeStamptime);

            commentString = (TextView) itemView.findViewById(R.id.commentString);

            photo = (ImageView) itemView.findViewById(R.id.photo);

        }
    }

    public void resetAnimationIndex() {
        reverseAllAnimations = false;
        animationItemsIndex.clear();
    }

    public void toggleSelection(int pos) {
        currentSelectedIndex = pos;
        if (selectedItems.get(pos, false)) {
            selectedItems.delete(pos);
            animationItemsIndex.delete(pos);
        } else {
            selectedItems.put(pos, true);
            animationItemsIndex.put(pos, true);
        }
        notifyItemChanged(pos);
    }

    public void clearSelections() {
        reverseAllAnimations = true;
        selectedItems.clear();
        notifyDataSetChanged();
    }

    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    public List<Integer> getSelectedItems() {
        List<Integer> items =
                new ArrayList<>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); i++) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }

    public void removeData(int position) {
        list.remove(position);
        resetCurrentIndex();
    }

    private void resetCurrentIndex() {
        currentSelectedIndex = -1;
    }

    public interface ChatAdapterListener {


        void onMessageRowClicked(int position);

        void onRowLongClicked(int position);

        void download_chatimageClicked(int position, MyViewHolder holder);

    }

    private void applyRowAnimation(MyViewHolder holder, int position) {
        if ((reverseAllAnimations && animationItemsIndex.get(position, false)) || currentSelectedIndex == position) {
            //FlipAnimator.flipView(mContext, holder.iconBack, holder.iconFront, false);

            resetCurrentIndex();
        }

    }

    private void applyClickEvents(final MyViewHolder holder, final int position) {

        holder.messageContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onMessageRowClicked(position);
            }
        });

        holder.messageContainer.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                listener.onRowLongClicked(position);
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                return true;
            }
        });

        holder.download_chatimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.download_chatimageClicked(position, holder);
            }
        });
    }

    public void removeListeners() {
        Iterator<HashMap.Entry<DatabaseReference, ValueEventListener>> iterator2 = commentStatusHashMap.entrySet().iterator();
        while (iterator2.hasNext()) {
            HashMap.Entry<DatabaseReference, ValueEventListener> entry = (HashMap.Entry<DatabaseReference, ValueEventListener>) iterator2.next();
            if (entry.getValue() != null) entry.getKey().removeEventListener(entry.getValue());
        }
        Iterator<HashMap.Entry<DatabaseReference, ValueEventListener>> iterator = progressListenerHashmap.entrySet().iterator();
        while (iterator.hasNext()) {
            HashMap.Entry<DatabaseReference, ValueEventListener> entry = (HashMap.Entry<DatabaseReference, ValueEventListener>) iterator.next();
            if (entry.getValue() != null) entry.getKey().removeEventListener(entry.getValue());
        }
    }
}