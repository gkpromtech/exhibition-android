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
package ru.gkpromtech.exhibition.about;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import ru.gkpromtech.exhibition.R;
import ru.gkpromtech.exhibition.model.Organization;
import ru.gkpromtech.exhibition.utils.ImageLoader;

public class OrganizationDetailsFragment extends Fragment {

    private final static String ARG_PARAM = "organization";
    private Organization mOrganization;
    private String[] mStatuses;

    public static OrganizationDetailsFragment newInstance(Organization organization) {
        OrganizationDetailsFragment fragment = new OrganizationDetailsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM, organization);
        fragment.setArguments(args);
        return fragment;
    }

    public OrganizationDetailsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
            mOrganization = (Organization) getArguments().getSerializable(ARG_PARAM);
        mStatuses = getResources().getStringArray(R.array.organization_status);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_organization_details, container, false);

        TextView textTitle = (TextView) layout.findViewById(R.id.textTitle);
        ImageView imageLogo = (ImageView) layout.findViewById(R.id.imageLogo);
        TextView textDescription = (TextView) layout.findViewById(R.id.textDescription);
        TextView textAddress = (TextView) layout.findViewById(R.id.textAddress);
        TextView textEmail = (TextView) layout.findViewById(R.id.textEmail);
        TextView textPhone = (TextView) layout.findViewById(R.id.textPhone);
        TextView textSite = (TextView) layout.findViewById(R.id.textSite);

        textTitle.setText(mStatuses[mOrganization.status]);
        ImageLoader.load(mOrganization.logo, imageLogo);
        textAddress.setText(mOrganization.address);
        textDescription.setText(mOrganization.fullname);
        textEmail.setText(mOrganization.email);
        textPhone.setText(mOrganization.phone);
        textSite.setText(mOrganization.site);

        return layout;
    }

}
