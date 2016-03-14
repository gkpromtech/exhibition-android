/* THIS FILE IS MACHINE GENERATED. DO NOT CHANGE IT BY HAND.
   TO UPDATE THIS FILE START ./gen_model FROM THE "app/tools" DIR */

package ru.gkpromtech.exhibition.model;

import ru.gkpromtech.exhibition.model.annotation.FK;
import ru.gkpromtech.exhibition.model.annotation.TableRef;

@TableRef(name="meetings_persons")
public class MeetingsPerson extends Entity {
    public @FK(entity=Meeting.class, field="id", onDelete="CASCADE", onUpdate="CASCADE") Integer meeting_id;
    public @FK(entity=Person.class, field="id", onDelete="CASCADE", onUpdate="CASCADE") Integer person_id;
}
