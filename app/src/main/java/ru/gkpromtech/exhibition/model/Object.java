/* THIS FILE IS MACHINE GENERATED. DO NOT CHANGE IT BY HAND.
   TO UPDATE THIS FILE START ./gen_model FROM THE "app/tools" DIR */

package ru.gkpromtech.exhibition.model;

import java.util.Date;

import ru.gkpromtech.exhibition.model.annotation.Null;
import ru.gkpromtech.exhibition.model.annotation.Default;
import ru.gkpromtech.exhibition.model.annotation.TableRef;

@TableRef(name="objects")
public class Object extends Entity {
    public @Default("CURRENT_TIMESTAMP") Date createtime;
    public @Null Date modifytime;
}
