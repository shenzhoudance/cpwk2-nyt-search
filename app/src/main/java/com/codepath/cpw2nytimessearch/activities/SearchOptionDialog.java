package com.codepath.cpw2nytimessearch.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;

import com.codepath.cpw2nytimessearch.R;
import com.codepath.cpw2nytimessearch.models.SearchOption;

import java.util.ArrayList;
import java.util.Calendar;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by qi_zhu on 2/12/16.
 */
public class SearchOptionDialog extends DialogFragment implements DatePickerDialog.OnDateSetListener {
  SearchOption option;
  @Bind(R.id.btnSave) Button btnSave;
  @Bind(R.id.btnCancel) Button btnCancel;
  @Bind(R.id.btnDate) Button btnDate;
  @Bind(R.id.btnOrder) Button btnOrder;
  @Bind(R.id.cbArts) CheckBox cbArts;
  @Bind(R.id.cbFashion) CheckBox cbFashion;
  @Bind(R.id.cbSports) CheckBox cbSports;

  public SearchOptionDialog() {
  }

  @Override
  public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
    String dateStr = String.format("%d%02d%02d", year, monthOfYear + 1, dayOfMonth);
    option.setBeginDate(dateStr);
    btnDate.setText(dateStr);
  }

  public interface SearchOptionListener {
    void onFinishSearchOption(SearchOption option);
  }

  public static SearchOptionDialog newInstance(SearchOption option) {
    SearchOptionDialog frag = new SearchOptionDialog();
    Bundle args = new Bundle();
    args.putParcelable("option", option);
    frag.setArguments(args);
    return frag;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_search_option, container);
    ButterKnife.bind(this, view);
    return view;
  }

  @OnClick(R.id.btnCancel)
  public void onCancel(View view) {
    getDialog().dismiss();
  }

  @OnClick(R.id.btnSave)
  public void onSave(View view) {
    SearchOptionDialog.this.updateOption();
    SearchOptionListener listener = (SearchOptionListener) getActivity();
    listener.onFinishSearchOption(SearchOptionDialog.this.option);
    getDialog().dismiss();
  }

  @OnClick(R.id.btnOrder)
  public void onToggleSort(View view) {
    if (option.getSortOrder().equals("NEWEST")) {
      btnOrder.setText("OLDEST");
      option.setSortOrder("OLDEST");
    } else {
      btnOrder.setText("NEWEST");
      option.setSortOrder("NEWEST");
    }
  }

  @OnClick(R.id.btnDate)
  public void onSetDate(View view) {
    DatePickerDialog.OnDateSetListener listener = (DatePickerDialog.OnDateSetListener) this;
    final Calendar c = Calendar.getInstance();
    String dateStr = btnDate.getText().toString();
    int year, month_of_year, day_of_month;
    if (dateStr.equals("NOT SET")) {
      year = c.get(Calendar.YEAR);
      month_of_year = c.get(Calendar.MONTH);
      day_of_month = c.get(Calendar.DAY_OF_MONTH);
    } else {
      year = Integer.parseInt(dateStr.substring(0, 4));
      month_of_year = Integer.parseInt(dateStr.substring(4, 6)) - 1;
      day_of_month = Integer.parseInt(dateStr.substring(6));
    }
    DatePickerDialog d = new DatePickerDialog(getContext(), listener, year, month_of_year, day_of_month);
    d.show();
  }


  private void updateOption() {
    String startDate = btnDate.getText().toString();
    if (startDate.equals("NOT SET")) {
      option.setBeginDate(null);
    } else {
      option.setBeginDate(startDate);
    }
    String sortOrder = btnOrder.getText().toString();
    option.setSortOrder(sortOrder);
    ArrayList<String> newsDesk = new ArrayList<String>();

    if (cbArts.isChecked()) {
      newsDesk.add("Arts");
    }
    if (cbFashion.isChecked()) {
      newsDesk.add("Fashion & Style");
    }
    if (cbSports.isChecked()) {
      newsDesk.add("Sports");
    }
    option.setNewsDesk(newsDesk);
  }

  private void updateView() {
    if (TextUtils.isEmpty(this.option.getBeginDate())) {
      btnDate.setText("NOT SET");
    } else {
      btnDate.setText(this.option.getBeginDate());
    }
    if (TextUtils.isEmpty(this.option.getSortOrder())) {
      this.option.setSortOrder("NEWEST");
    } else {
      btnOrder.setText(this.option.getSortOrder());
    }
    cbArts.setChecked(false);
    cbFashion.setChecked(false);
    cbSports.setChecked(false);
    ArrayList<String> newsDesk = this.option.getNewsDesk();
    Log.d("newsDesk", newsDesk.toString());
    if (!newsDesk.isEmpty()) {
      for (int i = 0; i < newsDesk.size(); i++ ) {
        String value = newsDesk.get(i);
        if (value.equals("Sports")) {
          cbSports.setChecked(true);
        }
        if (value.equals("Arts")) {
          cbArts.setChecked(true);
        }
        if (value.equals("Fashion & Style")) {
          cbFashion.setChecked(true);
        }
      }
    }
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    this.option = (SearchOption) getArguments().getParcelable("option");
    this.updateView();
    /*
    // Get field from view
    mEditText = (EditText) view.findViewById(R.id.txt_your_name);
    // Fetch arguments from bundle and set title
    String title = getArguments().getString("title", "Enter Name");
    getDialog().setTitle(title);
    // Show soft keyboard automatically and request focus to field
    mEditText.requestFocus();
    getDialog().getWindow().setSoftInputMode(
      WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
      */
  }

  public void onSubmit(View view) {
    dismiss();
  }
}
