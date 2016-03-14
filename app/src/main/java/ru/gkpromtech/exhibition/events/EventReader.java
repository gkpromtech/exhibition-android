package ru.gkpromtech.exhibition.events;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.gkpromtech.exhibition.db.DbHelper;
import ru.gkpromtech.exhibition.db.Table;
import ru.gkpromtech.exhibition.model.Event;
import ru.gkpromtech.exhibition.model.EventFavorite;
import ru.gkpromtech.exhibition.model.EventsPlace;
import ru.gkpromtech.exhibition.model.Media;
import ru.gkpromtech.exhibition.model.ObjectsMedia;
import ru.gkpromtech.exhibition.model.Place;
import ru.gkpromtech.exhibition.model.Tag;
import ru.gkpromtech.exhibition.model.TagsObject;

public class EventReader {

    private List<Date> days_cache;
    private Map<Date, List<Event>> events_cache;
    private List<EventFavorite> favorites_hash;

    private Context mContext;
    private Table<Event> eventsTable;
    private Table<EventFavorite> events_favoritesTable;
    private final SparseArray<Place> mPlaces;

    private static EventReader reader_instance = null;
    private EventReaderNotifier notify = null;

    public final static int EVENT_FILTER_ALL = 0;
    public final static int EVENT_FILTER_SHOW = 1;
    public final static int EVENT_FILTER_MY = 2;

    public final static int MEDIA_FILTER_ALL = 0;
    public final static int MEDIA_FILTER_IMAGE = 1;
    public final static int MEDIA_FILTER_VIDEO = 2;


    static public EventReader getInstance(Context context) {
        if (reader_instance == null)
            reader_instance = new EventReader(context);
        return reader_instance;
    }

    private EventReader(Context context) {
        this.mContext = context;

        days_cache = new ArrayList<>();
        events_cache = new HashMap<>();
        favorites_hash = new ArrayList<>();
        mPlaces = new SparseArray<>();


        eventsTable = DbHelper.getInstance(context).getTableFor(Event.class);
        events_favoritesTable = DbHelper.getInstance(context).getTableFor(EventFavorite.class);

        Table<Place> placesTable = DbHelper.getInstance(context).getTableFor(Place.class);
        List<Place> places = placesTable.select();
        for (Place place : places)
            mPlaces.put(place.id, place);
    }

    public List<Event> getEvents(int index, int filter) {
        Date date = getDay(index);
        return getEvents(date, filter);
    }

    public List<Event> getEvents(Date date, int filter) {
        List<Event> result = new ArrayList<>();

        List<Event> events = events_cache.get(date);
        if (events == null) {
            try {
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                cal.add(Calendar.DAY_OF_MONTH, 1);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                Date date2 = cal.getTime();

                events = new ArrayList<>();

                String selection = "date >= ? AND date < ?";
                String[] args = DbHelper.makeArguments(new Long[]{date.getTime(), date2.getTime()});
                events = eventsTable.select(selection, args, null, null, "date");
                if (events == null)
                    return result;

                favorites_hash = events_favoritesTable.select(null, null, null, null, null);
                if (favorites_hash == null)
                    return result;


            } catch (Exception e) {
                e.printStackTrace();
            }

            events_cache.put(date, events);
        }

        for (Event res : events) {
            boolean fav = false;
            if (filter == EVENT_FILTER_MY) {
                // check favorite flag
                for (EventFavorite f : favorites_hash) {
                    if (f.eventid.equals(res.id)) {
                        fav = (f.favorite != 0);
                        break;
                    }
                }
            }

            if (filter == EVENT_FILTER_ALL ||
                    (filter == EVENT_FILTER_SHOW && res.type == Event.TYPE_DEMO) ||
                    (filter == EVENT_FILTER_MY && fav)) {
                result.add(res);
            }

        }

        return result;
    }

    public List<Date> getDays() {
        if (days_cache.isEmpty()) {
            SQLiteDatabase db = DbHelper.getInstance(mContext).getReadableDatabase();
            Cursor cursor = db.query(true, "events", new String[]{"date"}, null, null, null, null,
                    "date", null);

            try {
                while (cursor.moveToNext()) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(cursor.getLong(0));
                    calendar.set(Calendar.AM_PM, 0);
                    calendar.set(Calendar.HOUR, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);

                    if (!days_cache.contains(calendar.getTime())) {
                        days_cache.add(calendar.getTime());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                cursor.close();
                db.close();
            }
        }
        return days_cache;
    }

    public int getDaysCount() {
        return days_cache.size();
    }

    public Date getDay(int position) {
        List<Date> days = getDays();
        assert days.size() > position;

        return days.get(position);
    }

    public Event findItem(int itemId) {

        List<Event> events = eventsTable.select("id = ?", new String[]{String.valueOf(itemId)}, null, null, null);
        if (events == null)
            return null;

        if (events != null)
            return events.get(0);

        return null;
    }

    public List<Place> getPlaces(int eventId) {
        List<Place> result = new ArrayList<Place>();
        try {
            Table<EventsPlace> events_places = DbHelper.getInstance(mContext).getTableFor(EventsPlace.class);

            String selection = "eventid=" + eventId;
            List<EventsPlace> eventsPlaces = events_places.select(selection, null, null, null, null);

            for (EventsPlace eventsPlace : eventsPlaces) {
                Place place = mPlaces.get(eventsPlace.placeid);
                if (place != null)
                    result.add(place);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public List<Place> getPlaces(Integer[] eventIds) {
        List<Place> result = new ArrayList<Place>();

        try {
            Table<EventsPlace> events_places = DbHelper.getInstance(mContext).getTableFor(EventsPlace.class);

            String selection = "eventid IN (" + DbHelper.makePlaceholders(eventIds.length) + ")";
            String[] args = DbHelper.makeArguments(eventIds);
            List<EventsPlace> rows = events_places.select(selection, args, null, null, null);

            if (rows.size() > 0) {
                Table<Place> places = DbHelper.getInstance(mContext).getTableFor(Place.class);

                Integer[] placeIds = new Integer[rows.size()];
                for (int i = 0; i < rows.size(); i ++) {
                    placeIds[i] = rows.get(i).placeid;
                }

                String selection2 = "id IN (" + DbHelper.makePlaceholders(placeIds.length) + ")";
                args = DbHelper.makeArguments(placeIds);

                result = places.select(selection2, args, null, null, null);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public List<Media> getMedia(int eventId, int filter) {
        List<Media> result = new ArrayList<Media>();

        try {
            Table<ObjectsMedia> events_media = DbHelper.getInstance(mContext).getTableFor(ObjectsMedia.class);

            List<ObjectsMedia> rows = events_media.select("objectid = ?",
                    new String[]{String.valueOf(eventId)}, null, null, "ordernum");

            if (rows.size() > 0) {
                Table<Media> media = DbHelper.getInstance(mContext).getTableFor(Media.class);

                Integer[] mediaIds = new Integer[rows.size()];
                for (int i = 0; i < rows.size(); i ++) {
                    mediaIds[i] = rows.get(i).mediaid;
                }

                String selection2 = "id IN (" + DbHelper.makePlaceholders(mediaIds.length) + ")";
                String[] args = DbHelper.makeArguments(mediaIds);

                List<Media> temp_result = media.select(selection2, args, null, null, null);
                for (Media m: temp_result) {
                    if (filter == MEDIA_FILTER_ALL ||
                            (filter == MEDIA_FILTER_IMAGE && m.type != Media.IMAGE) ||
                            (filter == MEDIA_FILTER_VIDEO && m.type == Media.VIDEO)) {
                        result.add(m);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public List<Tag> getTags(int eventId) {
        List<Tag> result = new ArrayList<Tag>();

        try {
            Table<TagsObject> events_media = DbHelper.getInstance(mContext).getTableFor(TagsObject.class);

            String selection = "objectid = "+eventId;
            List<TagsObject> rows = events_media.select(selection, null, null, null, null);

            if (rows.size() > 0) {
                Table<Tag> tags = DbHelper.getInstance(mContext).getTableFor(Tag.class);

                Integer[] tagIds = new Integer[rows.size()];
                for (int i = 0; i < rows.size(); i ++) {
                    tagIds[i] = rows.get(i).tagid;
                }

                String selection2 = "id IN (" + DbHelper.makePlaceholders(tagIds.length) + ")";
                String[] args = DbHelper.makeArguments(tagIds);

                result = tags.select(selection2, args, null, null, null);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public EventFavorite getEventFavorite(int eventId) {
        for (EventFavorite f: favorites_hash) {
            if (f.eventid.equals(eventId)) {
                return f;
            }
        }

        EventFavorite fav = new EventFavorite();
        fav.id = new Integer(eventId);
        fav.eventid = new Integer(eventId);
        fav.favorite = new Integer(0);
        return fav;
    }

    public void updateFavorite(int eventId, int state, int conflictAlgorithm) {

        synchronized (this) {
            for (EventFavorite f : favorites_hash) {
                if (f.eventid.equals(eventId)) {
                    // update favorites cache
                    f.favorite = state;
                    events_favoritesTable.update(f, conflictAlgorithm);
                    return;
                }
            }
            // not found
            EventFavorite fav = new EventFavorite();
            fav.id = new Integer(eventId);
            fav.eventid = new Integer(eventId);
            fav.favorite = new Integer(state);
            favorites_hash.add(fav);
            events_favoritesTable.insert(fav, conflictAlgorithm);
        }
        notifyAboutChanges();
    }

    public void updateCalendarEvent(int eventId, int calendarEventId, int conflictAlgorithm) {

        synchronized (this) {
            for (EventFavorite f : favorites_hash) {
                if (f.eventid.equals(eventId)) {
                    // update favorites cache
                    f.calendarEventId = calendarEventId;
                    events_favoritesTable.update(f, conflictAlgorithm);
                    return;
                }
            }
            // not found
            EventFavorite fav = new EventFavorite();
            fav.id = new Integer(eventId);
            fav.eventid = new Integer(eventId);
            fav.calendarEventId = new Integer(calendarEventId);
            favorites_hash.add(fav);
            events_favoritesTable.insert(fav, conflictAlgorithm);
        }

        notifyAboutChanges();
    }

    public void notifyAboutChanges() {
        notify.onChanged(reader_instance);
    }

    // Уведомления об изменениях в Событиях
    public void notifyOnChanges(EventReaderNotifier notify) {
        this.notify = notify;
    }

    public static abstract class EventReaderNotifier {
        protected abstract void onChanged(EventReader reader);
    }
}
