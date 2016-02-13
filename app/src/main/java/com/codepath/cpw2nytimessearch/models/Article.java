package com.codepath.cpw2nytimessearch.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Article implements Parcelable {
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

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(this.url);
    dest.writeString(this.headline);
    dest.writeString(this.imageUrl);
  }

  public Article() {
  }

  private Article(Parcel in) {
    this.url = in.readString();
    this.headline = in.readString();
    this.imageUrl = in.readString();
  }

  public static final Parcelable.Creator<Article> CREATOR = new Parcelable.Creator<Article>() {
    public Article createFromParcel(Parcel source) {
      return new Article(source);
    }

    public Article[] newArray(int size) {
      return new Article[size];
    }
  };
}
