package com.codepath.cpw2nytimessearch;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by qi_zhu on 2/8/16.
 */
public class Article {
  String url;
  String headline;
  String imageUrl;

  public String getUrl() {
    return url;
  }

  public String getHeadline() {
    return headline;
  }

  public String getImageUrl() {
    return imageUrl;
  }



  public static List<Article> FromJsonArray(JSONArray articlesJson) {
    ArrayList<Article> res = new ArrayList<>();
    for (int i = 0; i < articlesJson.length(); i++) {
      try {
        JSONObject articleJson = articlesJson.getJSONObject(i);
        Article article = new Article();
        article.url = articleJson.getString("web_url");
        if (articleJson.has("headline")) {
          article.headline = articleJson.getJSONObject("headline").getString("main");
        }
        if (articleJson.has("multimedia")) {
          JSONArray mediasJson = articleJson.getJSONArray("multimedia");
          if (mediasJson.length() > 0) {
            article.imageUrl = "http://www.nytimes.com/" + mediasJson.getJSONObject(0).getString("url");
          }
        }
        res.add(article);
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }
    return res;
  }
}
