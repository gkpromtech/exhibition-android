/* THIS FILE IS MACHINE GENERATED. DO NOT CHANGE IT BY HAND.
   TO UPDATE THIS FILE START ./gen_model FROM THE "app/tools" DIR */

package ru.gkpromtech.exhibition.model;

import ru.gkpromtech.exhibition.model.annotation.FK;
import ru.gkpromtech.exhibition.model.annotation.Null;
import ru.gkpromtech.exhibition.model.annotation.Translatable;
import ru.gkpromtech.exhibition.model.annotation.TableRef;

@TableRef(name="exhibitions", tr=true, trid="exhibitionid")
public class Exhibition extends Entity {
    public @FK(entity=Organization.class, field="id", onDelete="CASCADE", onUpdate="CASCADE") Integer organizationid;
    public @FK(entity=Place.class, field="id", onDelete="CASCADE", onUpdate="CASCADE") @Null Integer placeid;
    public @FK(entity=Section.class, field="id", onDelete="CASCADE", onUpdate="CASCADE") @Null Integer sectionid;

    // translatable fields
    public @Translatable String name;
    public @Translatable String text;
}
