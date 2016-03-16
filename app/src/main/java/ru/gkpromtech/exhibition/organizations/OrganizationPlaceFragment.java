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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import ru.gkpromtech.exhibition.R;
import ru.gkpromtech.exhibition.model.Media;
import ru.gkpromtech.exhibition.utils.ImageLoader;

public class OrganizationPlaceFragment extends OrganizationBaseFragment {

    public OrganizationPlaceFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_organization_place, container, false);
        super.init(v);

        OrganizationItem item = getOrganization();

        if (!item.organization.address.isEmpty()) {
            ((TextView) v.findViewById(R.id.textAddress)).setText(item.organization.address);
        } else {
            v.findViewById(R.id.textAddressHeader).setVisibility(View.GONE);
            v.findViewById(R.id.textAddress).setVisibility(View.GONE);
        }

        String text = "";
        if (!item.organization.phone.isEmpty())
            text += getString(R.string.phone) + ": " + item.organization.phone + "\n";
        if (!item.organization.email.isEmpty())
            text += getString(R.string.email) + ": " + item.organization.email + "\n";
        if (!item.organization.site.isEmpty())
            text += getString(R.string.www) + ": " + item.organization.site + "\n";

        if (!text.isEmpty()) {
            ((TextView) v.findViewById(R.id.textContacts)).setText(text);
        } else {
            v.findViewById(R.id.textContactsHeader).setVisibility(View.GONE);
            v.findViewById(R.id.textContacts).setVisibility(View.GONE);
        }

        if (item.organization.description != null && !item.organization.description.isEmpty()) {
            String data = "<html><body><p>" + item.organization.description + "</p></body></html>";
            data = data.replace("\r\n", "<br/>");
            ((WebView) v.findViewById(R.id.textDescription)).loadData(data , "text/html; charset=utf-8", "utf-8");
        }
        else {
            v.findViewById(R.id.textDescription).setVisibility(View.INVISIBLE);
        }

        ImageView imageView = (ImageView) v.findViewById(R.id.imagePlace);
        Media media = (Media) getArguments().getSerializable("media");
        String url = null;
        if (media != null)
            url = (media.type == Media.IMAGE) ? media.url : media.preview;
        if (url != null && !url.isEmpty()) {
            ImageLoader.load(url, imageView);
        } else {
            imageView.setVisibility(View.GONE);
        }

        return v;
    }

}
