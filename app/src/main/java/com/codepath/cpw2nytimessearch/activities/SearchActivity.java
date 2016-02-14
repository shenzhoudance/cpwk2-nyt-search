package com.codepath.cpw2nytimessearch.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Parcel;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.codepath.cpw2nytimessearch.ArticleArrayAdapter;
import com.codepath.cpw2nytimessearch.EndlessScrollListener;
import com.codepath.cpw2nytimessearch.R;
import com.codepath.cpw2nytimessearch.activities.SearchOptionDialog.SearchOptionListener;
import com.codepath.cpw2nytimessearch.models.Article;
import com.codepath.cpw2nytimessearch.models.SearchOption;
import com.google.common.base.Joiner;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

public class SearchActivity extends AppCompatActivity implements SearchOptionListener {
  @Bind(R.id.gvResults) GridView gvResults;
  @BindString(R.string.NYAS_key) String NYAS_KEY;

  ArrayList<Article> articles;
  ArticleArrayAdapter adapter;
  SearchOption option;
  String query;
  Boolean hasMore;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_search);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    ButterKnife.bind(this);
    articles = new ArrayList<>();
    adapter = new ArticleArrayAdapter(this, articles);
    option = new SearchOption("", "", new ArrayList<String>());
    gvResults.setAdapter(adapter);
    gvResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent i = new Intent(getApplicationContext(), ArticleActivity.class);
        i.putExtra("url", articles.get(position).getUrl());
        startActivity(i);
      }
    });
    gvResults.setOnScrollListener(new EndlessScrollListener() {
      @Override
      public boolean onLoadMore(int page, int totalItemsCount) {
        Log.d("onLoadMore:", page + "");
        // WHY? WHY? WHY? page == 2 when this function is called on the first time. :(
        search(page - 1);
        return SearchActivity.this.hasMore;
      }
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_search, menu);

    MenuItem searchItem = menu.findItem(R.id.action_search);
    final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
      @Override
      public boolean onQueryTextSubmit(String query) {
        // perform query here
        SearchActivity.this.query = query;
        search(0);
        // workaround to avoid issues with some emulators and keyboard devices firing twice if a keyboard enter is used
        // see https://code.google.com/p/android/issues/detail?id=24599
        searchView.clearFocus();

        return true;
      }

      @Override
      public boolean onQueryTextChange(String newText) {
        return false;
      }
    });

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      FragmentManager fm = getSupportFragmentManager();
      SearchOptionDialog searchOptionDialog = SearchOptionDialog.newInstance(option);
      searchOptionDialog.show(fm, "fragment_search_option");

      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  public void search(int page) {
    if (!checkInternet()) {
      return;
    }
    if (page == 0) {
      adapter.clear();
    }
    String query = this.query;
    AsyncHttpClient client = new AsyncHttpClient();
    String url = "http://api.nytimes.com/svc/search/v2/articlesearch.json";
    RequestParams params = new RequestParams();
    params.add("api-key", NYAS_KEY);
    params.add("q", query);
    params.add("page", "" + page);
    Log.d("Search", "page:" + page);
    String beginDate = option.getBeginDate();
    if (!TextUtils.isEmpty(beginDate) && !beginDate.equals("NOT SET")) {
      params.add("begin_date", beginDate);
    }
    String sortStr = option.getSortOrder();
    if (!TextUtils.isEmpty(sortStr)) {
      params.add("sort", sortStr.toLowerCase());
    }
    ArrayList<String> newsDesk = option.getNewsDesk();
    if (newsDesk != null && newsDesk.size() > 0) {
      params.add("fq", "news_desk:(\"" + Joiner.on("\" \"").join(newsDesk) + "\")");

    }
    client.get(this, url, params, new JsonHttpResponseHandler() {
      @Override
      public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
        try {
          JSONObject meta = response.getJSONObject("response").getJSONObject("meta");
          int hits = meta.getInt("hits");
          int offset = meta.getInt("offset");
          Log.d("search", "hits:" + hits);
          Log.d("search", "offset:" + offset);
          JSONArray articlesJson = response.getJSONObject("response").getJSONArray("docs");
          List<Article> articles = Article.FromJsonArray(articlesJson);
          adapter.addAll(articles);
          if (articles.size() < 1) {
            Toast.makeText(getApplicationContext(), "Not result found", Toast.LENGTH_LONG).show();
          }
          SearchActivity.this.hasMore = ((offset + 10) < hits);
        } catch (JSONException e) {
          e.printStackTrace();
        }
      }

      @Override
      public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
        if (!isOnline()) {
          Toast.makeText(getApplicationContext(), "Please check your network", Toast.LENGTH_LONG).show();
        } else {
          Toast.makeText(getApplicationContext(), "Server issue", Toast.LENGTH_LONG).show();
        }
        super.onFailure(statusCode, headers, throwable, errorResponse);
      }
    });
  }

  private Boolean isNetworkAvailable() {
    ConnectivityManager connectivityManager
      = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
    return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
  }

  private boolean checkInternet() {
    if (!isNetworkAvailable()) {
      Toast.makeText(this, "Network not available", Toast.LENGTH_LONG).show();
      return false;
    }
    return true;
  }

  public boolean isOnline() {
    Runtime runtime = Runtime.getRuntime();
    try {
      Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
      int     exitValue = ipProcess.waitFor();
      return (exitValue == 0);
    } catch (IOException e)          { e.printStackTrace(); }
    catch (InterruptedException e) { e.printStackTrace(); }
    return false;
  }

  @Override
  public void onFinishSearchOption(SearchOption option) {
    Parcel p = Parcel.obtain();
    option.writeToParcel(p, 0);
    p.setDataPosition(0);
    this.option = SearchOption.CREATOR.createFromParcel(p);
  }
}
