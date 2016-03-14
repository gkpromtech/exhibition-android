/* THIS FILE IS MACHINE GENERATED. DO NOT CHANGE IT BY HAND.
   TO UPDATE THIS FILE START ./gen_model FROM THE "app/tools" DIR */

package ru.gkpromtech.exhibition.model;

import ru.gkpromtech.exhibition.model.annotation.FK;
import ru.gkpromtech.exhibition.model.annotation.TableRef;

@TableRef(name="sections_places")
public class SectionsPlace extends Entity {
    public @FK(entity=Place.class, field="id", onDelete="CASCADE", onUpdate="CASCADE") Integer placeid;
    public @FK(entity=Section.class, field="id", onDelete="CASCADE", onUpdate="CASCADE") Integer sectionid;
}
