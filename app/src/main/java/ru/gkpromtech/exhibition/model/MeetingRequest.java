/* THIS FILE IS MACHINE GENERATED. DO NOT CHANGE IT BY HAND.
   TO UPDATE THIS FILE START ./gen_model FROM THE "app/tools" DIR */

package ru.gkpromtech.exhibition.model;

import ru.gkpromtech.exhibition.model.annotation.FK;
import ru.gkpromtech.exhibition.model.annotation.Null;
import ru.gkpromtech.exhibition.model.annotation.Default;
import ru.gkpromtech.exhibition.model.annotation.TableRef;

@TableRef(name="meeting_requests")
public class MeetingRequest extends Entity {
    public @FK(entity=Person.class, field="id", onDelete="CASCADE", onUpdate="CASCADE") Integer creator;
    public @FK(entity=Person.class, field="id", onDelete="CASCADE", onUpdate="CASCADE") Integer target_person;
    public String request_text;
    public String denial_text;
    public @Default("0") Integer req_status;
    public @FK(entity=Meeting.class, field="id", onDelete="CASCADE", onUpdate="CASCADE") @Null Integer meeting_id;
    public @Default("0") Integer private_flag;
}
