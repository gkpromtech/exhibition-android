/* THIS FILE IS MACHINE GENERATED. DO NOT CHANGE IT BY HAND.
   TO UPDATE THIS FILE START ./gen_model FROM THE "app/tools" DIR */

package ru.gkpromtech.exhibition.model;

import java.util.Date;

import ru.gkpromtech.exhibition.model.annotation.FK;
import ru.gkpromtech.exhibition.model.annotation.Null;
import ru.gkpromtech.exhibition.model.annotation.Translatable;
import ru.gkpromtech.exhibition.model.annotation.TableRef;

@TableRef(name="business_events", tr=true, trid="business_eventid")
public class BusinessEvent extends Entity {
    public @Null Date date;
    public @FK(entity=Organization.class, field="id", onDelete="CASCADE", onUpdate="CASCADE") Integer organizationid;
    public @FK(entity=Person.class, field="id", onDelete="CASCADE", onUpdate="CASCADE") @Null Integer personid;
    public @Null Integer members_limit;
    public @FK(entity=Place.class, field="id", onDelete="CASCADE", onUpdate="CASCADE") @Null Integer placeid;
    public @FK(entity=BusinessSection.class, field="id", onDelete="CASCADE", onUpdate="CASCADE") @Null Integer business_sectionid;

    // translatable fields
    public @Translatable String header;
    public @Translatable String details;
}
