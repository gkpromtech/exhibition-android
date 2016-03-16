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
package ru.gkpromtech.exhibition;

import ru.gkpromtech.exhibition.about.AboutActivity;
import ru.gkpromtech.exhibition.catalog.CatalogActivity;
import ru.gkpromtech.exhibition.events.EventsActivity;
import ru.gkpromtech.exhibition.media.MediaActivity;
import ru.gkpromtech.exhibition.news.NewsActivity;
import ru.gkpromtech.exhibition.organizations.OrganizationsActivity;

public class Navigation {

    public static class Item {
        private final int title;
        private final Class activity;

        public int getTitle() {
            return title;
        }

        public Class getActivity() {
            return activity;
        }

        public Item(int title, Class activity) {
            this.title = title;
            this.activity = activity;
        }
    }

    public final static Item[] ITEMS = new Item[] {
            new Item(R.string.title_section_now, EventsActivity.class),
            new Item(R.string.title_section_organizations, OrganizationsActivity.class),
            new Item(R.string.title_section_catalogue, CatalogActivity.class),
            new Item(R.string.title_section_news, NewsActivity.class),
            new Item(R.string.title_section_media, MediaActivity.class),
            new Item(R.string.title_activity_about, AboutActivity.class),
    };
}
