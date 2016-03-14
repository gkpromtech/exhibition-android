/* THIS FILE IS MACHINE GENERATED. DO NOT CHANGE IT BY HAND.
   TO UPDATE THIS FILE START ./gen_model FROM THE "app/tools" DIR */

package ru.gkpromtech.exhibition.model;

import java.util.Date;

import ru.gkpromtech.exhibition.model.annotation.Null;
import ru.gkpromtech.exhibition.model.annotation.Default;
import ru.gkpromtech.exhibition.model.annotation.Translatable;
import ru.gkpromtech.exhibition.model.annotation.TableRef;

@TableRef(name="events", tr=true, trid="eventid")
public class Event extends Entity {
    public final static int TYPE_PUBLIC = 0;
    public final static int TYPE_DEMO = 1;

    public @Null Date date;
    public @Default("0") Integer type;

    // translatable fields
    public @Translatable String header;
    public @Translatable String details;
}
