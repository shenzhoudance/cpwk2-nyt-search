package com.codepath.cpw2nytimessearch;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.cpw2nytimessearch.models.Article;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ArticleArrayAdapter extends ArrayAdapter<Article> {
  public ArticleArrayAdapter(Context context, List<Article> articles) {
    super(context, android.R.layout.simple_list_item_1, articles);
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    Article article = getItem(position);
    if (convertView == null) {
      convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_ariticle_result, parent, false);
    }
    // TODO: use butterknife
    ImageView imageView = (ImageView)convertView.findViewById(R.id.ivImage);
    imageView.setImageResource(0);

    TextView tvTitle = (TextView)convertView.findViewById(R.id.tvTitle);
    tvTitle.setText(article.getHeadline());

    String imgUrl = article.getImageUrl();
    if (!TextUtils.isEmpty(imgUrl)) {
      Picasso.with(getContext()).load(imgUrl).into(imageView);
    }
    return convertView;
  }
}
