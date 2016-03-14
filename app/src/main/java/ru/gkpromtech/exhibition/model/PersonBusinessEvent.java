/* THIS FILE IS MACHINE GENERATED. DO NOT CHANGE IT BY HAND.
   TO UPDATE THIS FILE START ./gen_model FROM THE "app/tools" DIR */

package ru.gkpromtech.exhibition.model;

import ru.gkpromtech.exhibition.model.annotation.FK;
import ru.gkpromtech.exhibition.model.annotation.TableRef;

@TableRef(name="person_business_events")
public class PersonBusinessEvent extends Entity {
    public @FK(entity=Person.class, field="id", onDelete="CASCADE", onUpdate="CASCADE") Integer personid;
    public @FK(entity=BusinessEvent.class, field="id", onDelete="CASCADE", onUpdate="CASCADE") Integer business_eventid;
}
