package com.example.newsplug;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.newsplug.ui.common.HeadlinesList;
import com.example.newsplug.ui.detailspage.detailspagePOJO;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.ArrayList;

public class DetailedArticleActivity extends AppCompatActivity {
    private static Context context;
    private JsonObjectRequest request;
    private RequestQueue requestQueue;
    private detailspagePOJO details;
    private String gotoLink = "";
    private Intent intent;
    private RelativeLayout spinner;

    public static Context getContext() {
        return context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();

        setContentView(R.layout.activity_detailed_article);

        final Toolbar myToolbar = findViewById(R.id.my_toolbar_detailed_page);
        setSupportActionBar(myToolbar);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        spinner = findViewById(R.id.progressBar_detailed);
        spinner.setVisibility(View.VISIBLE);


        intent = getIntent();
        String id = intent.getExtras().getString("ID");
        String URL = "http://ec2-54-197-200-149.compute-1.amazonaws.com:7000/api/guardian/article?id=" + id;

        request = new JsonObjectRequest(Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(JSONObject res) {
                try {
                    JSONObject response = res.getJSONObject("response");
                    String title = response.getString("Title");
                    if (title.length() > 25) {
                        title = title.substring(0, 25) + "...";
                    }
                    myToolbar.setTitle(title);
                    gotoLink = response.getString("ID");
                    details = new detailspagePOJO(response.getString("ID"),
                            response.getString("Image"),
                            response.getString("Title"),
                            response.getString("Date"),
                            response.getString("Description"),
                            response.getString("Link"),
                            response.getString("SectionName"));
                    setData(details);
                } catch (JSONException | ParseException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error);
            }
        });
        request.setRetryPolicy(new DefaultRetryPolicy(
                50000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue = Volley.newRequestQueue(DetailedArticleActivity.getContext());
        requestQueue.add(request);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detailed_action_page_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        String Name = "bookmarks";
        SharedPreferences pref_outer = context.getSharedPreferences("news", 0);
        Type type_outer = new TypeToken<ArrayList<HeadlinesList>>() {
        }.getType();
        Gson gson_outer = new Gson();

        // Change icon for the values that are already in the bookmanks
        if (pref_outer.contains(Name)) {
            ArrayList<HeadlinesList> bookmarks_list = gson_outer.fromJson(pref_outer.getString(Name, ""), type_outer);
            for (int i = 0; i < bookmarks_list.size(); i++) {
                if (bookmarks_list.get(i).getId().equals(intent.getExtras().getString("ID"))) {
                    System.out.println(menu.findItem(R.id.bookmark_detailed_page));
                    menu.findItem(R.id.bookmark_detailed_page).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_bookmarked_40dp));
                    break;
                }
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.twitter_deatiled_page:
                if (gotoLink.length() > 0) {
                    String message = "Check out this link:";
                    String tweetUrl = "https://twitter.com/intent/tweet?text=" + message + "&url=" +
                            gotoLink + "&hashtags=CSCI571NewsSearch";
                    Uri uri = Uri.parse(tweetUrl);
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                }
                return true;

            case R.id.bookmark_detailed_page:
                String Name = "bookmarks";
                SharedPreferences pref = context.getSharedPreferences("news", 0);
                SharedPreferences.Editor editor = pref.edit();
                Type type = new TypeToken<ArrayList<HeadlinesList>>() {
                }.getType();
                Gson gson = new Gson();

                // Toggle icon for the curremt id
                if (pref.contains(Name)) {
                    ArrayList<HeadlinesList> bookmarks_list = gson.fromJson(pref.getString(Name, ""), type);
                    boolean found = false;
                    for (int i = 0; i < bookmarks_list.size(); i++) {
                        if (bookmarks_list.get(i).getId().equals(intent.getExtras().getString("ID"))) {
                            found = true;
                            bookmarks_list.remove(i);
                            String bookmarked_json = gson.toJson(bookmarks_list);
                            editor.putString("bookmarks", bookmarked_json);
                            editor.commit();

                            item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_bookmark_border_black_40dp));
                            Toast.makeText(getContext(), "\"" + intent.getExtras().getString("Title") + "\" was removed from bookmarks", Toast.LENGTH_SHORT).show();
                            break;
                        }
                    }
                    if (found == false) {
                        HeadlinesList record = new HeadlinesList(intent.getExtras().getString("ID"),
                                intent.getExtras().getString("Image"),
                                intent.getExtras().getString("Title"),
                                intent.getExtras().getString("Date"),
                                intent.getExtras().getString("Section_Name"),
                                intent.getExtras().getString("Link"));
                        bookmarks_list.add(record);
                        String bookmarked_json = gson.toJson(bookmarks_list);
                        editor.putString("bookmarks", bookmarked_json);
                        editor.commit();
                        item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_bookmarked_40dp));
                        Toast.makeText(getContext(), "\"" + record.getTitle() + "\" was added to bookmarks", Toast.LENGTH_SHORT).show();
                    }
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        String Name = "bookmarks";
        SharedPreferences pref = context.getSharedPreferences("news", 0);
        Type type = new TypeToken<ArrayList<HeadlinesList>>() {
        }.getType();
        Gson gson = new Gson();

        // Change icon for the values that are already in the bookmanks
        if (pref.contains(Name)) {
            ArrayList<HeadlinesList> bookmarks_list = gson.fromJson(pref.getString(Name, ""), type);
            System.out.println(bookmarks_list.size());
        }
    }

    private void setData(final detailspagePOJO details) throws ParseException {
        spinner.setVisibility(View.GONE);
        CardView cv = findViewById(R.id.detailed_cards);
        cv.setVisibility(View.VISIBLE);
        ImageView img = findViewById(R.id.detailed_page_image);
        Picasso.get()
                .load(details.getImageurl())
                .into(img);

        TextView title = findViewById(R.id.detailed_page_title);
        title.setText(details.getTitle());

        TextView tag = findViewById(R.id.detailed_page_tag);
        tag.setText(details.getTag());

        TextView date = findViewById(R.id.detailed_page_date);
        date.setText(details.getDate());

        TextView desc = findViewById(R.id.detailed_page_description);
        desc.setText(Html.fromHtml(details.getDesc()));

        TextView link = findViewById(R.id.detailed_page_link);
        link.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String Url = details.getLink();
                Uri uri = Uri.parse(Url);
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
            }
        });
    }
}
