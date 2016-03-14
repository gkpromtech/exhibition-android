/* THIS FILE IS MACHINE GENERATED. DO NOT CHANGE IT BY HAND.
   TO UPDATE THIS FILE START ./gen_model FROM THE "app/tools" DIR */

package ru.gkpromtech.exhibition.model;

import ru.gkpromtech.exhibition.model.annotation.FK;
import ru.gkpromtech.exhibition.model.annotation.TableRef;

@TableRef(name="groups_places")
public class GroupsPlace extends Entity {
    public @FK(entity=Group.class, field="id", onDelete="CASCADE", onUpdate="CASCADE") Integer groupid;
    public @FK(entity=Place.class, field="id", onDelete="CASCADE", onUpdate="CASCADE") Integer placeid;
}
