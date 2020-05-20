package com.example.newsplug.ui.bookmarks;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsplug.DetailedArticleActivity;
import com.example.newsplug.R;
import com.example.newsplug.ui.common.HeadlinesList;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;

public class BookmarksAdapter extends RecyclerView.Adapter<BookmarksAdapter.ViewHolder> {
    private ArrayList<HeadlinesList> bookmarks_list;
    private Context context;
    private View GodView;

    public BookmarksAdapter(ArrayList<HeadlinesList> bookmarks_list, Context context, View parentView) {
        this.bookmarks_list = bookmarks_list;
        this.context = context;
        this.GodView = parentView;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_bookmarks_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HeadlinesList b_list = bookmarks_list.get(position);
        Picasso.get()
                .load(b_list.getImageurl())
                .into(holder.img);
        holder.title.setText(Html.fromHtml("<font color='#000000'>" + b_list.getTitle() + "</font>"));
        String newsTag = b_list.getNewsTag();
        if (newsTag.length() > 11) {
            newsTag = newsTag.substring(0, 9) + "...";
        }
        String expectedDate = b_list.getTimeTillDate();
        if (expectedDate.contains("Z")) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            format.setTimeZone(TimeZone.getTimeZone("PST"));
            try {
                expectedDate = format.parse(expectedDate).toString();
                expectedDate = expectedDate.split(" ")[2] + " " + expectedDate.split(" ")[1];
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            expectedDate = expectedDate.split(" ")[0] + " " + expectedDate.split(" ")[1];
        }
        String txt = expectedDate + "<font color='#6200EE' weight='bold'> | </font>" + newsTag;
        holder.timeTillDate.setText(Html.fromHtml(txt));
    }

    @Override
    public int getItemCount() {
        return bookmarks_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView img, bookmarkButton;
        public TextView title, timeTillDate;

        public ViewHolder(View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.bookmark_image);
            title = itemView.findViewById(R.id.bookmark_title);
            timeTillDate = itemView.findViewById(R.id.bookmark_timeTillNow);

            bookmarkButton = itemView.findViewById(R.id.bookmark_button);

            // Single Tapup on the bookmark button
            bookmarkButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    final HeadlinesList hdlist = bookmarks_list.get(position);

                    String Name = "bookmarks";
                    SharedPreferences pref = v.getContext().getSharedPreferences("news", 0);
                    SharedPreferences.Editor editor = pref.edit();
                    Type type = new TypeToken<ArrayList<HeadlinesList>>() {
                    }.getType();
                    Gson gson = new Gson();

                    // Remove from the Local Storage
                    if (pref.contains(Name)) {
                        String old_id = hdlist.getId();
                        bookmarks_list = gson.fromJson(pref.getString(Name, ""), type);
                        for (int i = 0; i < bookmarks_list.size(); i++) {
                            if (bookmarks_list.get(i).getId().equals(old_id)) {
                                bookmarks_list.remove(i);
                                break;
                            }
                        }
                        String bookmarked_json = gson.toJson(bookmarks_list);
                        editor.putString("bookmarks", bookmarked_json);
                        editor.commit();

                        if (getItemCount() == 0) {
                            TextView tx= GodView.findViewById(R.id.no_bookmarks);
                            tx.setVisibility(View.VISIBLE);
                        }

                        notifyItemRemoved(getAdapterPosition());
                        notifyItemRangeChanged(getAdapterPosition(), getItemCount());

                        Toast.makeText(v.getContext(), "\"" + hdlist.getTitle() + "\" was removed from bookmarks", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            // Single Tapup on the item except image
            LinearLayout text_part = itemView.findViewById(R.id.text_part_bookmark);
            text_part.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    final HeadlinesList hdlist = bookmarks_list.get(position);
                    Intent intent = new Intent(context, DetailedArticleActivity.class);
                    intent.putExtra("ID",hdlist.getId());
                    intent.putExtra("Image", hdlist.getImageurl());
                    intent.putExtra("Title", hdlist.getTitle());
                    intent.putExtra("Date",hdlist.getTimeTillDate());
                    intent.putExtra("Section_Name", hdlist.getNewsTag());
                    intent.putExtra("Link",hdlist.getLink());

                    context.startActivity(intent);
                }
            });

            // Long press on the item
            text_part.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = getAdapterPosition();
                    final HeadlinesList b_list = bookmarks_list.get(position);

                    final Dialog dialog = new Dialog(context);
                    dialog.setContentView(R.layout.dialogue_home);

                    TextView text = dialog.findViewById(R.id.textDialog);
                    text.setText(b_list.getTitle());
                    ImageView image = dialog.findViewById(R.id.imageDialog);
                    Picasso.get()
                            .load(b_list.getImageurl())
                            .into(image);
                    dialog.show();

                    ImageView twitter_share = dialog.findViewById(R.id.dialogTwitter);
                    twitter_share.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            String message = "Check out this link:";
                            String tweetUrl = "https://twitter.com/intent/tweet?text=" + message + "&url="
                                    + b_list.getLink() + "&hashtags=CSCI571NewsSearch";
                            Uri uri = Uri.parse(tweetUrl);
                            dialog.getContext().startActivity(new Intent(Intent.ACTION_VIEW, uri));
                            dialog.cancel();
                        }
                    });

                    final ImageView DialogBookmarkButton = dialog.findViewById(R.id.dialogBookmark);
                    DialogBookmarkButton.setTag("True");
                    DialogBookmarkButton.setImageResource(R.drawable.ic_bookmarked_24dp);

                    // Single Tapup on the bookmark button
                    DialogBookmarkButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int position = getAdapterPosition();
                            final HeadlinesList hdlist = bookmarks_list.get(position);

                            DialogBookmarkButton.setTag("False");
                            DialogBookmarkButton.setImageResource(R.drawable.ic_bookmark_border_black_24dp);

                            dialog.cancel();

                            // Remove from the Local Storage
                            String Name = "bookmarks";
                            SharedPreferences pref = v.getContext().getSharedPreferences("news", 0);
                            SharedPreferences.Editor editor = pref.edit();
                            Type type = new TypeToken<ArrayList<HeadlinesList>>() {
                            }.getType();
                            Gson gson = new Gson();
                            if (pref.contains(Name)) {
                                String oldId = hdlist.getId();
                                bookmarks_list = gson.fromJson(pref.getString(Name, ""), type);
                                for (int i = 0; i < bookmarks_list.size(); i++) {
                                    if (bookmarks_list.get(i).getId().equals(oldId)) {
                                        bookmarks_list.remove(i);
                                        break;
                                    }
                                }
                                String bookmarked_json = gson.toJson(bookmarks_list);
                                editor.putString("bookmarks", bookmarked_json);
                                editor.commit();

                                if (getItemCount() == 0) {
                                    TextView tx= GodView.findViewById(R.id.no_bookmarks);
                                    tx.setVisibility(View.VISIBLE);
                                }

                                notifyItemRemoved(getAdapterPosition());
                                notifyItemRangeChanged(getAdapterPosition(), getItemCount());

                                Toast.makeText(v.getContext(), "\"" + hdlist.getTitle() + "\" was removed from bookmarks", Toast.LENGTH_SHORT).show();
                            }

                            Toast.makeText(v.getContext(), "\"" + hdlist.getTitle() + "\" was removed from bookmarks", Toast.LENGTH_SHORT).show();
                        }
                    });

                    return false;
                }
            });
        }
    }
}
