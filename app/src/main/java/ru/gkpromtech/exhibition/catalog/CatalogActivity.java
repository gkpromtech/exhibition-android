package ru.gkpromtech.exhibition.catalog;

import android.app.Fragment;
import android.content.Intent;
import android.support.v7.widget.SearchView;
import android.view.Menu;

import ru.gkpromtech.exhibition.NavigationActivity;
import ru.gkpromtech.exhibition.R;


public class CatalogActivity extends NavigationActivity
        implements SectionsFragment.OnFragmentInteractionListener {

    @Override
    protected Fragment getFragment() {
        return SectionsFragment.newInstance();
    }

    @Override
    public void onFragmentInteraction(int  id, int num, String title) {
        Intent intent = new Intent(this, SectionContentActivity.class);
        intent.putExtra("sectionId", id);
        intent.putExtra("sectionNum", num);
        intent.putExtra("sectionTitle", title);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setOnQueryTextListener((SearchView.OnQueryTextListener) getActiveFragment());
        return super.onCreateOptionsMenu(menu);
    }

}
