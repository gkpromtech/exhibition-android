package ru.gkpromtech.exhibition.model;

import ru.gkpromtech.exhibition.model.annotation.FK;
import ru.gkpromtech.exhibition.model.annotation.Null;
import ru.gkpromtech.exhibition.model.annotation.TableRef;

@TableRef(name="event_favorite", sync=false)
public class EventFavorite extends Entity {
    public @FK(entity=Event.class, field="id", onDelete="CASCADE", onUpdate="CASCADE") @Null Integer eventid;
    public @Null Integer favorite;
    public @Null Integer calendarEventId;
}
