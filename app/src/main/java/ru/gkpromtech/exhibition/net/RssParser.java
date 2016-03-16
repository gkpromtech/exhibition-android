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
package ru.gkpromtech.exhibition.net;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RssParser {

    public static final class Item implements Serializable {
        public String guid;         // идентификатор
        public String title;        // заголовок
        public String link;         // ссылка на новость
        public String description;  // описание
        public Date pubDate;        // дата публикации
        public String enclosureUrl; // ссылка на изображение новости

        @Override
        public String toString() {
            return title + "\n" + description;
        }
    }

    private static final String NS = null;
    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);


    public List<Item> parse(InputStream in)
            throws XmlPullParserException, IOException, ParseException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readFeed(parser);
        } finally {
            in.close();
        }
    }

    private List<Item> readFeed(XmlPullParser parser)
            throws XmlPullParserException, IOException, ParseException {
        List<Item> items = new ArrayList<>();

        parser.require(XmlPullParser.START_TAG, NS, "rss");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG)
                continue;

            parser.require(XmlPullParser.START_TAG, NS, "channel");

            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG)
                    continue;

                String name = parser.getName();
                if (name.equals("item")) {
                    items.add(readItem(parser));
                } else {
                    skip(parser);
                }
            }

            parser.require(XmlPullParser.END_TAG, NS, "channel");
            parser.next();
        }

        parser.require(XmlPullParser.END_TAG, NS, "rss");
        return items;
    }

    private Item readItem(XmlPullParser parser)
            throws XmlPullParserException, IOException, ParseException {
        parser.require(XmlPullParser.START_TAG, NS, "item");
        Item item = new Item();

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG)
                continue;

            String name = parser.getName();
            switch (name) {
                case "title":
                    item.title = readTag(parser, name).trim();
                    break;
                case "link":
                    item.link = readTag(parser, name).trim();
                    break;
                case "description":
                    item.description = readTag(parser, name).trim();
                    break;
                case "guid":
                    item.guid = readTag(parser, name).trim();
                    break;
                case "pubDate":
                    item.pubDate = DATE_FORMAT.parse(readTag(parser, name).trim());
                    break;
                case "enclosure":
                    item.enclosureUrl = readEnclosure(parser, name).trim();
                    break;
                default:
                    skip(parser);
                    break;
            }
        }

        parser.require(XmlPullParser.END_TAG, NS, "item");
        parser.next();
        return item;
    }

    private String readTag(XmlPullParser parser, String tag)
            throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, NS, tag);
        String result = readText(parser);
        parser.require(XmlPullParser.END_TAG, NS, tag);
        return result;
    }

    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = null;
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private String readEnclosure(XmlPullParser parser, String tag)
            throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, NS, tag);

        parser.next();
        String result = parser.getAttributeValue(null, "url");

        parser.require(XmlPullParser.END_TAG, NS, tag);
        return result;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG)
            throw new IllegalStateException();

        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}
