package com.example.newsplug.ui.bookmarks;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsplug.R;
import com.example.newsplug.ui.common.HeadlinesList;
import com.example.newsplug.ui.home.MyAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class BookmarksFragment extends Fragment {
    private RecyclerView recyclerView;
    private ArrayList<HeadlinesList> bookmarks_list;
    private BookmarksAdapter myAdapter;
    private View root;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_bookmarks, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.bookmarks_recycler_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        bookmarks_list = new ArrayList<>();
        getData();
    }

    private void getData() {
        String Name = "bookmarks";
        SharedPreferences pref = getContext().getSharedPreferences("news", 0);
        Type type = new TypeToken<ArrayList<HeadlinesList>>() {}.getType();
        Gson gson = new Gson();

        // Change icon for the values that are already in the bookmanks
        if (pref.contains(Name)) {
            bookmarks_list = gson.fromJson(pref.getString(Name, ""), type);
        }
        setdata(bookmarks_list);
    }

    private void setdata(ArrayList<HeadlinesList> bookmarks_list) {
        TextView tx= root.findViewById(R.id.no_bookmarks);
        if (bookmarks_list.size() == 0) {
            tx.setVisibility(View.VISIBLE);
        } else {
            tx.setVisibility(View.GONE);
            myAdapter = new BookmarksAdapter(bookmarks_list, getContext(), root);
            recyclerView.setAdapter(myAdapter);
        }
    }
}
