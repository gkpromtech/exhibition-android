/* THIS FILE IS MACHINE GENERATED. DO NOT CHANGE IT BY HAND.
   TO UPDATE THIS FILE START ./gen_model FROM THE "app/tools" DIR */

package ru.gkpromtech.exhibition.model;

import ru.gkpromtech.exhibition.model.annotation.FK;
import ru.gkpromtech.exhibition.model.annotation.Null;
import ru.gkpromtech.exhibition.model.annotation.TableRef;

@TableRef(name="maps_points")
public class MapsPoint extends Entity {
    public @FK(entity=Map.class, field="id", onDelete="CASCADE", onUpdate="CASCADE") Integer mapid;
    public @Null String placename;
    public @Null String coords;
}
