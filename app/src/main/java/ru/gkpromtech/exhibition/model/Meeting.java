/* THIS FILE IS MACHINE GENERATED. DO NOT CHANGE IT BY HAND.
   TO UPDATE THIS FILE START ./gen_model FROM THE "app/tools" DIR */

package ru.gkpromtech.exhibition.model;

import java.util.Date;

import ru.gkpromtech.exhibition.model.annotation.FK;
import ru.gkpromtech.exhibition.model.annotation.Null;
import ru.gkpromtech.exhibition.model.annotation.Default;
import ru.gkpromtech.exhibition.model.annotation.TableRef;

@TableRef(name="meetings")
public class Meeting extends Entity {
    public @FK(entity=Person.class, field="id", onDelete="CASCADE", onUpdate="CASCADE") Integer personid;
    public String text;
    public @FK(entity=Place.class, field="id", onDelete="CASCADE", onUpdate="CASCADE") @Null Integer placeid;
    public @Default("0") Integer status;
    public @Default("CURRENT_TIMESTAMP") Date meeting_date;
    public @Default("0") Integer private_flag;
}
