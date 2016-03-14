/* THIS FILE IS MACHINE GENERATED. DO NOT CHANGE IT BY HAND.
   TO UPDATE THIS FILE START ./gen_model FROM THE "app/tools" DIR */

package ru.gkpromtech.exhibition.model;

import ru.gkpromtech.exhibition.model.annotation.FK;
import ru.gkpromtech.exhibition.model.annotation.Null;
import ru.gkpromtech.exhibition.model.annotation.Translatable;
import ru.gkpromtech.exhibition.model.annotation.TableRef;

@TableRef(name="places", tr=true, trid="placeid")
public class Place extends Entity {
    public @FK(entity=Group.class, field="id", onDelete="CASCADE", onUpdate="CASCADE") Integer groupid;
    public @FK(entity=MapsPoint.class, field="id", onDelete="SET NULL", onUpdate="CASCADE") @Null Integer mappointid;
    public String schemaid;

    // translatable fields
    public @Translatable String name;
}
