/* THIS FILE IS MACHINE GENERATED. DO NOT CHANGE IT BY HAND.
   TO UPDATE THIS FILE START ./gen_model FROM THE "app/tools" DIR */

package ru.gkpromtech.exhibition.model;

import ru.gkpromtech.exhibition.model.annotation.Null;
import ru.gkpromtech.exhibition.model.annotation.Translatable;
import ru.gkpromtech.exhibition.model.annotation.TableRef;

@TableRef(name="online", tr=true, trid="onlineid")
public class Online extends Entity {
    public String url1;
    public @Null String url2;
    public @Null String preview;

    // translatable fields
    public @Translatable String name;
    public @Translatable @Null String description;
}
