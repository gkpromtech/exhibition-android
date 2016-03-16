/*
 * Copyright 2016 Promtech. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
