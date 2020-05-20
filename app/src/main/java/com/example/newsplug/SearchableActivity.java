package com.example.newsplug;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.newsplug.ui.common.HeadlinesList;
import com.example.newsplug.ui.home.MyAdapter;
import com.example.newsplug.ui.search.SearchableAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SearchableActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private List<HeadlinesList> hdlist;

    private SearchableAdapter myAdapter;
    private JsonObjectRequest request;
    private RequestQueue requestQueue;
    private String SearchQuery;
    private RelativeLayout spinner;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchable);

        final Toolbar myToolbar = findViewById(R.id.my_toolbar_search_page);
        setSupportActionBar(myToolbar);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        spinner = findViewById(R.id.progressBar_search);
        spinner.setVisibility(View.VISIBLE);

        recyclerView = findViewById(R.id.search_recycler_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        hdlist = new ArrayList<>();

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            if (query.length() > 20) {
                query = "Search Results for " + query.substring(0, 20) + "...";
            } else {
                query = "Search Results for " + query;
            }
            ab.setTitle(query);
            SearchQuery = intent.getStringExtra(SearchManager.QUERY);
            getData(SearchQuery);
        }

        mSwipeRefreshLayout = findViewById(R.id.swiperefresh_items_search);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                recyclerView.setLayoutManager(new LinearLayoutManager(SearchableActivity.this));
                hdlist = new ArrayList<>();
                getData(SearchQuery);
            }
        });
    }

    private void getData(String query) {
        String URL = "http://ec2-54-197-200-149.compute-1.amazonaws.com:7000/api/guardian/search?q=" + query;
        request = new JsonObjectRequest(Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(JSONObject res) {
                JSONObject jsonObject;
                try {
                    JSONArray response = res.getJSONArray("response");
                    for (int i = 0; i < response.length(); i++) {
                        jsonObject = response.getJSONObject(i);

                        HeadlinesList listData = new HeadlinesList(jsonObject.getString("ID"),
                                jsonObject.getString("Image"),
                                jsonObject.getString("Title"),
                                jsonObject.getString("Date"),
                                jsonObject.getString("Section_Name"),
                                jsonObject.getString("Link"));
                        hdlist.add(listData);
                    }
                    if(mSwipeRefreshLayout.isRefreshing()) {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                setupData(hdlist);

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
        requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);

    }
    private void setupData(List<HeadlinesList> list_data) {
        myAdapter = new SearchableAdapter(list_data, this);
        recyclerView.setAdapter(myAdapter);
        spinner.setVisibility(View.GONE);
    }
}
