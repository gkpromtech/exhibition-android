/*
 * Copyright 2016 Promtech. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.gkpromtech.exhibition.catalog;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.Menu;

import ru.gkpromtech.exhibition.NavigationActivity;
import ru.gkpromtech.exhibition.R;
import ru.gkpromtech.exhibition.utils.AnalyticsManager;


public class CatalogActivity extends NavigationActivity
        implements SectionsFragment.OnFragmentInteractionListener {

    @Override
    protected Fragment getFragment() {
        return SectionsFragment.newInstance();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AnalyticsManager.sendEvent(this, R.string.catalog_category, R.string.action_open);
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
