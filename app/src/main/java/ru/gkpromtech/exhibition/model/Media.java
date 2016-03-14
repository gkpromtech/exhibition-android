/* THIS FILE IS MACHINE GENERATED. DO NOT CHANGE IT BY HAND.
   TO UPDATE THIS FILE START ./gen_model FROM THE "app/tools" DIR */

package ru.gkpromtech.exhibition.model;

import ru.gkpromtech.exhibition.model.annotation.FK;
import ru.gkpromtech.exhibition.model.annotation.Null;
import ru.gkpromtech.exhibition.model.annotation.Default;
import ru.gkpromtech.exhibition.model.annotation.Translatable;
import ru.gkpromtech.exhibition.model.annotation.TableRef;

@TableRef(name="media", tr=true, trid="mediaid")
public class Media extends Entity {
    public final static int IMAGE = 0;
    public final static int VIDEO = 1;
    public final static int FILE = 2;

    public @Default("0") Integer type;
    public @FK(entity=Organization.class, field="id", onDelete="CASCADE", onUpdate="CASCADE") Integer organizationid;

    // translatable fields
    public @Translatable String url;
    public @Translatable @Null String preview;
    public @Translatable @Null String name;
}
