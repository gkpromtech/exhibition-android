package ru.gkpromtech.exhibition.utils;

import android.content.Context;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.SearchView;
import android.util.AttributeSet;
import android.widget.AdapterView;
import android.widget.Filterable;
import android.widget.ListAdapter;

public class AutoCompleteSearchView extends SearchView {

    private SearchView.SearchAutoComplete mSearchAutoComplete;

    public AutoCompleteSearchView(Context context) {
        super(context);
        initialize();
    }

    public AutoCompleteSearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public void initialize() {
        mSearchAutoComplete = (SearchAutoComplete) findViewById(
                android.support.v7.appcompat.R.id.search_src_text);
        this.setAdapter(null);
        this.setOnItemClickListener(null);
    }

    @Override
    public void setSuggestionsAdapter(CursorAdapter adapter) {
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        mSearchAutoComplete.setOnItemClickListener(listener);
    }

    public <T extends ListAdapter & Filterable> void setAdapter(T adapter) {
        mSearchAutoComplete.setAdapter(adapter);
    }

}
