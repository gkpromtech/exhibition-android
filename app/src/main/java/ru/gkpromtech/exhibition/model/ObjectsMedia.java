/* THIS FILE IS MACHINE GENERATED. DO NOT CHANGE IT BY HAND.
   TO UPDATE THIS FILE START ./gen_model FROM THE "app/tools" DIR */

package ru.gkpromtech.exhibition.model;

import ru.gkpromtech.exhibition.model.annotation.FK;
import ru.gkpromtech.exhibition.model.annotation.Default;
import ru.gkpromtech.exhibition.model.annotation.TableRef;

@TableRef(name="objects_media")
public class ObjectsMedia extends Entity {
    public @FK(entity=Object.class, field="id", onDelete="CASCADE", onUpdate="CASCADE") Integer objectid;
    public @FK(entity=Media.class, field="id", onDelete="CASCADE", onUpdate="CASCADE") Integer mediaid;
    public @Default("0") Integer ordernum;
}
