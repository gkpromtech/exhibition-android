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
package ru.gkpromtech.exhibition.organizations;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import ru.gkpromtech.exhibition.R;
import ru.gkpromtech.exhibition.utils.ImageLoader;

public abstract class OrganizationBaseFragment extends Fragment {

    private OrganizationItem mOrganization;

    public OrganizationBaseFragment() {
    }

    public static <T extends OrganizationBaseFragment> T newInstance(
            Class<T> cls, OrganizationItem organization) {

        T fragment;
        try {
            fragment = cls.newInstance();
        } catch (Exception e) {
            return null;
        }

        Bundle args = new Bundle();
        args.putSerializable("organization", organization);
        fragment.setArguments(args);
        return fragment;
    }

    protected OrganizationItem getOrganization() {
        return mOrganization;
    }

    public void init(View v) {
        mOrganization = (OrganizationItem) getArguments().getSerializable("organization");

        TextView textName = (TextView) v.findViewById(R.id.textName);
        TextView textPlace = (TextView) v.findViewById(R.id.textPlace);
        final ImageView imageLogo = (ImageView) v.findViewById(R.id.imageLogo);

        textName.setText(mOrganization.organization.fullname);
        textPlace.setText(mOrganization.place.name);

        String logo = mOrganization.organization.logo;
        ImageLoader.load(logo, imageLogo, R.drawable.no_logo);

        if (mOrganization.group == null || mOrganization.group.position == null)
            v.findViewById(R.id.imageShowOnSchema).setVisibility(View.GONE);
    }

}
