/* THIS FILE IS MACHINE GENERATED. DO NOT CHANGE IT BY HAND.
   TO UPDATE THIS FILE START ./gen_model FROM THE "app/tools" DIR */

package ru.gkpromtech.exhibition.model;

import ru.gkpromtech.exhibition.model.annotation.FK;
import ru.gkpromtech.exhibition.model.annotation.Null;
import ru.gkpromtech.exhibition.model.annotation.Default;
import ru.gkpromtech.exhibition.model.annotation.Translatable;
import ru.gkpromtech.exhibition.model.annotation.TableRef;

@TableRef(name="persons", tr=true, trid="personid")
public class Person extends Entity {
    public @FK(entity=Organization.class, field="id", onDelete="CASCADE", onUpdate="CASCADE") Integer organizationid;
    public String photo;
    public @Null String phone;
    public @Null String email;
    public @Null String site;
    public @Default("0") Integer ordernum;

    // translatable fields
    public @Translatable @Null String position;
    public @Translatable @Null String name;
    public @Translatable @Null String rank;
}
