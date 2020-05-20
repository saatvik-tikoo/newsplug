package com.example.newsplug.ui.sections;

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
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
    private List<HeadlinesList> secList;
    private Context context;

    public RecyclerAdapter(List<HeadlinesList> secList, Context context) {
        this.secList = secList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_sectional_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HeadlinesList sectional_list = secList.get(position);
        Picasso.get()
                .load(sectional_list.getImageurl())
                .resize(120, 120)
                .into(holder.img);
        holder.title.setText(Html.fromHtml("<font color='#000000'>" + sectional_list.getTitle() + "</font>"));

        String dtEnd = sectional_list.getTimeTillDate();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        format.setTimeZone(TimeZone.getTimeZone("PST"));
        String date = "";
        try {
            date = getTimeDifference(format.parse(dtEnd), new Date());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String txt = date + "<font color='#6200EE' weight='bold'> | </font>" + sectional_list.getNewsTag();
        holder.timeTillDate.setText(Html.fromHtml(txt));

        String Name = "bookmarks";
        SharedPreferences pref_outer = context.getSharedPreferences("news", 0);
        Type type_outer = new TypeToken<ArrayList<HeadlinesList>>() {}.getType();
        Gson gson_outer = new Gson();

        // Change icon for the values that are already in the bookmanks
        if (pref_outer.contains(Name)) {
            ArrayList<HeadlinesList> bookmarks_list = gson_outer.fromJson(pref_outer.getString(Name, ""), type_outer);
            for (int i = 0; i < bookmarks_list.size(); i++) {
                if (bookmarks_list.get(i).getId().equals(sectional_list.getId())) {
                    holder.bookmarkButton.setTag("True");
                    holder.bookmarkButton.setImageResource(R.drawable.ic_bookmarked_24dp);
                    break;
                }
            }
        }
    }


    private String getTimeDifference(Date startDate, Date endDate) {
        long different = Math.abs(endDate.getTime() - startDate.getTime());

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;

        long elapsedHours = different / hoursInMilli;
        different = different % hoursInMilli;

        long elapsedMinutes = different / minutesInMilli;
        different = different % minutesInMilli;

        long elapsedSeconds = different / secondsInMilli;
        String Date = null;
        if (elapsedHours != 0) {
            Date = elapsedHours + "h ago";
        } else if (elapsedMinutes != 0) {
            Date = elapsedMinutes + "m ago";
        } else if (elapsedSeconds != 0) {
            Date = elapsedSeconds + "s";
        }
        return Date;
    }


    @Override
    public int getItemCount() {
        return secList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView img, bookmarkButton;
        public TextView title, timeTillDate;

        public ViewHolder(View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.sec_image);
            title = itemView.findViewById(R.id.sec_title);
            timeTillDate = itemView.findViewById(R.id.sec_timeTillNow);

            bookmarkButton = itemView.findViewById(R.id.sec_bookmark_me_button);

            // Single Tapup on the bookmark button
            bookmarkButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    final HeadlinesList sectionalList = secList.get(position);

                    String Name = "bookmarks";
                    SharedPreferences pref = v.getContext().getSharedPreferences("news", 0);
                    SharedPreferences.Editor editor = pref.edit();
                    Type type = new TypeToken<List<HeadlinesList>>(){}.getType();
                    Gson gson = new Gson();

                    if (bookmarkButton.getTag().equals("False")) {
                        bookmarkButton.setTag("True");
                        bookmarkButton.setImageResource(R.drawable.ic_bookmarked_24dp);

                        // Add to bookmarks in the Local Storage
                        if (pref.contains(Name)) {
                            ArrayList<HeadlinesList> bookmarks_list = gson.fromJson(pref.getString(Name, ""), type);
                            bookmarks_list.add(sectionalList);
                            System.out.println(bookmarks_list.get(bookmarks_list.size() - 1).getId());
                            String bookmarked_json = gson.toJson(bookmarks_list);
                            editor.putString("bookmarks", bookmarked_json);
                            editor.commit();
                        } else {
                            System.out.println("No Results to show");
                        }

                        Toast.makeText(v.getContext(), "\"" + sectionalList.getTitle() + "\" was added to bookmarks", Toast.LENGTH_SHORT).show();
                    } else if (bookmarkButton.getTag().equals("True")) {
                        bookmarkButton.setTag("False");
                        bookmarkButton.setImageResource(R.drawable.ic_bookmark_border_black_24dp);

                        // Remove from the Local Storage
                        if (pref.contains(Name)) {
                            ArrayList<HeadlinesList> bookmarks_list = gson.fromJson(pref.getString(Name, ""), type);
                            for (int i = 0; i < bookmarks_list.size(); i++) {
                                if (bookmarks_list.get(i).getId().equals(sectionalList.getId())) {
                                    bookmarks_list.remove(i);
                                    break;
                                }
                            }
                            if (bookmarks_list.size() > 0) {
                                System.out.println(bookmarks_list.get(bookmarks_list.size() - 1).getId());
                            }
                            String bookmarked_json = gson.toJson(bookmarks_list);
                            editor.putString("bookmarks", bookmarked_json);
                            editor.commit();
                        } else {
                            System.out.println("No Results to show");
                        }

                        Toast.makeText(v.getContext(), "\"" + sectionalList.getTitle() + "\" was removed from bookmarks", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            // Single Tapup on the item
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    final HeadlinesList sectional_list = secList.get(position);
                    Intent intent = new Intent(context, DetailedArticleActivity.class);
                    intent.putExtra("ID",sectional_list.getId());
                    intent.putExtra("Image", sectional_list.getImageurl());
                    intent.putExtra("Title", sectional_list.getTitle());
                    intent.putExtra("Date",sectional_list.getTimeTillDate());
                    intent.putExtra("Section_Name", sectional_list.getNewsTag());
                    intent.putExtra("Link",sectional_list.getLink());

                    context.startActivity(intent);
                }
            });

            // Long press on the item
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = getAdapterPosition();
                    final HeadlinesList sectional_list = secList.get(position);

                    final Dialog dialog = new Dialog(context);
                    dialog.setContentView(R.layout.dialogue_home);

                    TextView text = dialog.findViewById(R.id.textDialog);
                    text.setText(sectional_list.getTitle());
                    ImageView image = dialog.findViewById(R.id.imageDialog);
                    Picasso.get()
                            .load(sectional_list.getImageurl())
                            .into(image);
                    dialog.show();

                    ImageView twitter_share = dialog.findViewById(R.id.dialogTwitter);
                    twitter_share.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            String message = "Check out this link:";
                            String tweetUrl = "https://twitter.com/intent/tweet?text=" + message + "&url="
                                    + sectional_list.getLink() + "&hashtags=CSCI571NewsSearch";
                            Uri uri = Uri.parse(tweetUrl);
                            dialog.getContext().startActivity(new Intent(Intent.ACTION_VIEW, uri));
                            dialog.cancel();
                        }
                    });

                    final ImageView DialogBookmarkButton = dialog.findViewById(R.id.dialogBookmark);
                    if (bookmarkButton.getTag().equals("True")){
                        DialogBookmarkButton.setTag("True");
                        DialogBookmarkButton.setImageResource(R.drawable.ic_bookmarked_24dp);
                    } else if (bookmarkButton.getTag().equals("False")){
                        DialogBookmarkButton.setTag("False");
                        DialogBookmarkButton.setImageResource(R.drawable.ic_bookmark_border_black_24dp);
                    }

                    // Single Tapup on the bookmark button
                    DialogBookmarkButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int position = getAdapterPosition();
                            final HeadlinesList sectionalList = secList.get(position);

                            String Name = "bookmarks";
                            SharedPreferences pref = v.getContext().getSharedPreferences("news", 0);
                            SharedPreferences.Editor editor = pref.edit();
                            Type type = new TypeToken<List<HeadlinesList>>(){}.getType();
                            Gson gson = new Gson();

                            if (DialogBookmarkButton.getTag().equals("False")) {
                                DialogBookmarkButton.setTag("True");
                                DialogBookmarkButton.setImageResource(R.drawable.ic_bookmarked_24dp);

                                bookmarkButton.setTag("True");
                                bookmarkButton.setImageResource(R.drawable.ic_bookmarked_24dp);

                                // Add to bookmarks in the Local Storage
                                if (pref.contains(Name)) {
                                    ArrayList<HeadlinesList> bookmarks_list = gson.fromJson(pref.getString(Name, ""), type);
                                    bookmarks_list.add(sectionalList);
                                    System.out.println(bookmarks_list.get(bookmarks_list.size() - 1).getId());
                                    String bookmarked_json = gson.toJson(bookmarks_list);
                                    editor.putString("bookmarks", bookmarked_json);
                                    editor.commit();
                                } else {
                                    System.out.println("No Results to show");
                                }

                                Toast.makeText(v.getContext(), "\"" + sectionalList.getTitle() + "\" was added to bookmarks", Toast.LENGTH_SHORT).show();
                            } else if (DialogBookmarkButton.getTag().equals("True")) {
                                DialogBookmarkButton.setTag("False");
                                DialogBookmarkButton.setImageResource(R.drawable.ic_bookmark_border_black_24dp);

                                bookmarkButton.setTag("False");
                                bookmarkButton.setImageResource(R.drawable.ic_bookmark_border_black_24dp);

                                // Remove from the Local Storage
                                if (pref.contains(Name)) {
                                    ArrayList<HeadlinesList> bookmarks_list = gson.fromJson(pref.getString(Name, ""), type);
                                    for (int i = 0; i < bookmarks_list.size(); i++) {
                                        if (bookmarks_list.get(i).getId().equals(sectionalList.getId())) {
                                            bookmarks_list.remove(i);
                                            break;
                                        }
                                    }
                                    if (bookmarks_list.size() > 0) {
                                        System.out.println(bookmarks_list.get(bookmarks_list.size() - 1).getId());
                                    }
                                    String bookmarked_json = gson.toJson(bookmarks_list);
                                    editor.putString("bookmarks", bookmarked_json);
                                    editor.commit();
                                } else {
                                    System.out.println("No Results to show");
                                }

                                Toast.makeText(v.getContext(), "\"" + sectionalList.getTitle() + "\" was removed from bookmarks", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    return false;
                }
            });
        }
    }
}
