/* THIS FILE IS MACHINE GENERATED. DO NOT CHANGE IT BY HAND.
   TO UPDATE THIS FILE START ./gen_model FROM THE "app/tools" DIR */

package ru.gkpromtech.exhibition.model;

import ru.gkpromtech.exhibition.model.annotation.FK;
import ru.gkpromtech.exhibition.model.annotation.TableRef;

@TableRef(name="events_persons")
public class EventsPerson extends Entity {
    public @FK(entity=Event.class, field="id", onDelete="CASCADE", onUpdate="CASCADE") Integer eventid;
    public @FK(entity=Person.class, field="id", onDelete="CASCADE", onUpdate="CASCADE") Integer personid;
}
