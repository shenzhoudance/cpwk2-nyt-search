package com.codepath.cpw2nytimessearch.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class SearchOption implements Parcelable {
  String beginDate;
  String sortOrder;
  ArrayList<String> newsDesk;

  public String getBeginDate() {
    return beginDate;
  }

  public void setBeginDate(String beginDate) {
    this.beginDate = beginDate;
  }

  public String getSortOrder() {
    return sortOrder;
  }

  public void setSortOrder(String sortOrder) {
    this.sortOrder = sortOrder;
  }

  public ArrayList<String> getNewsDesk() {
    return newsDesk;
  }

  public void setNewsDesk(ArrayList<String> newsDesk) {
    this.newsDesk = newsDesk;
  }

  public SearchOption(String beginDate, String sortOrder, ArrayList<String> newsDesk) {
    this.beginDate = beginDate;
    this.sortOrder = sortOrder;
    this.newsDesk = newsDesk;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(this.beginDate);
    dest.writeString(this.sortOrder);
    dest.writeSerializable(this.newsDesk);
  }

  private SearchOption(Parcel in) {
    this.beginDate = in.readString();
    this.sortOrder = in.readString();
    this.newsDesk = (ArrayList<String>) in.readSerializable();
  }

  public static final Parcelable.Creator<SearchOption> CREATOR = new Parcelable.Creator<SearchOption>() {
    public SearchOption createFromParcel(Parcel source) {
      return new SearchOption(source);
    }

    public SearchOption[] newArray(int size) {
      return new SearchOption[size];
    }
  };
}
