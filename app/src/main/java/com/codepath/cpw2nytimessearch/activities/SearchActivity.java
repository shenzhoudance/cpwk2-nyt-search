package com.codepath.cpw2nytimessearch.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;

import com.codepath.cpw2nytimessearch.ArticleArrayAdapter;
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

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cz.msebera.android.httpclient.Header;

public class SearchActivity extends AppCompatActivity implements SearchOptionListener {
  @Bind(R.id.etQuery) EditText etQuery;
  @Bind(R.id.gvResults) GridView gvResults;
  @Bind(R.id.btnSearch) Button btnSearch;
  @BindString(R.string.NYAS_key) String NYAS_KEY;

  ArrayList<Article> articles;
  ArticleArrayAdapter adapter;
  SearchOption option;

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

  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_search, menu);
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

  @OnClick(R.id.btnSearch)
  public void onArticleSearch(View view) {
    adapter.clear();
    AsyncHttpClient client = new AsyncHttpClient();
    String url = "http://api.nytimes.com/svc/search/v2/articlesearch.json";
    RequestParams params = new RequestParams();
    params.add("api-key", NYAS_KEY);
    params.add("q", etQuery.getText().toString());
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
          JSONArray articlesJson = response.getJSONObject("response").getJSONArray("docs");
          List<Article> articles = Article.FromJsonArray(articlesJson);
          adapter.addAll(articles);
        } catch (JSONException e) {
          e.printStackTrace();
        }
      }
    });
  }

  @Override
  public void onFinishSearchOption(SearchOption option) {
    Parcel p = Parcel.obtain();
    option.writeToParcel(p, 0);
    p.setDataPosition(0);
    this.option = SearchOption.CREATOR.createFromParcel(p);
  }
}
