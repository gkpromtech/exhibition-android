package ru.gkpromtech.exhibition.events;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import ru.gkpromtech.exhibition.R;
import ru.gkpromtech.exhibition.model.Event;
import ru.gkpromtech.exhibition.model.EventFavorite;
import ru.gkpromtech.exhibition.model.Place;

class EventsArrayAdapter extends ArrayAdapter<Event> {

    private int dayNumber;
    private Activity context;
    private OnEventsArrayAdapterInteraction listener;

    static class Holder {
        ImageView imageStatus;
        TextView textHeader;
        TextView textDate;
        TextView textPlace;
        ImageButton favorite;
    }

    private SimpleDateFormat mDateFormat =
            new SimpleDateFormat("HH:mm", Locale.getDefault());

    public EventsArrayAdapter(Activity context, List<Event> list, int dayNumber) {
        super(context, R.layout.layout_events_item, list);
        this.context = context;
        this.dayNumber = dayNumber;

        mDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+03:00"));
    }


    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Event item = this.getItem(position);

        String sPlace = new String();
        List<Place> places = EventReader.getInstance(context).getPlaces(item.id);
        for (Place p: places) {
            sPlace = sPlace + p.name + " ";
        }
        EventFavorite fav = EventReader.getInstance(context).getEventFavorite(item.id);

        View view = null;
        final Holder holder;
        if (convertView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            view = inflater.inflate(R.layout.layout_events_item, parent, false);

            holder = new Holder();
            holder.textHeader = (TextView) view.findViewById(R.id.textViewEventHeader);
            holder.textDate = (TextView) view.findViewById(R.id.textDate);
            holder.textPlace = (TextView) view.findViewById(R.id.textViewEventPlace);
            holder.favorite = (ImageButton) view.findViewById(R.id.imageFlag);

            holder.favorite.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Event element = (Event) holder.favorite.getTag();
                            holder.favorite.setSelected(!holder.favorite.isSelected());
                            int state = holder.favorite.isSelected()? 1: 0;

                            if (listener != null)
                                listener.favoriteChanged(element.id, state);
                        }
                    });


            view.setTag(holder);
            holder.favorite.setTag(getItem(position));


        } else {
            view = convertView;
            holder = (Holder) view.getTag();
            holder.favorite.setTag(getItem(position));
        }

        if ( position % 2 == 0)
            view.setBackgroundResource(R.drawable.listview_selector_even);
        else
            view.setBackgroundResource(R.drawable.listview_selector_odd);

        holder.textHeader.setText(item.header);
        holder.favorite.setSelected(fav.favorite != 0);
        holder.textDate.setText(mDateFormat.format(item.date));
        holder.textPlace.setText(sPlace);

        return view;
    }

    public interface OnEventsArrayAdapterInteraction {
        public void favoriteChanged(int eventId, int state);
    }
    public void setOnItemChanged(OnEventsArrayAdapterInteraction listener) {
        this.listener = listener;
    }

};