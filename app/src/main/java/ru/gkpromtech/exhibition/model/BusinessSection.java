/* THIS FILE IS MACHINE GENERATED. DO NOT CHANGE IT BY HAND.
   TO UPDATE THIS FILE START ./gen_model FROM THE "app/tools" DIR */

package ru.gkpromtech.exhibition.model;

import ru.gkpromtech.exhibition.model.annotation.FK;
import ru.gkpromtech.exhibition.model.annotation.Null;
import ru.gkpromtech.exhibition.model.annotation.Translatable;
import ru.gkpromtech.exhibition.model.annotation.TableRef;

@TableRef(name="business_sections", tr=true, trid="business_sectionid")
public class BusinessSection extends Entity {
    public Integer num;
    public @FK(entity=Organization.class, field="id", onDelete="CASCADE", onUpdate="CASCADE") @Null Integer organizationid;

    // translatable fields
    public @Translatable String name;
}
