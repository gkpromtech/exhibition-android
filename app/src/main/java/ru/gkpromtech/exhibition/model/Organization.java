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

import ru.gkpromtech.exhibition.model.annotation.Null;
import ru.gkpromtech.exhibition.model.annotation.Translatable;
import ru.gkpromtech.exhibition.model.annotation.TableRef;

@TableRef(name="organizations", tr=true, trid="organizationid")
public class Organization extends Entity {
    public final static int STATUS_ORGANIZER = 0; // организатор
    public final static int STATUS_CO_ORGANIZER = 1; // соорганизатор
    public final static int STATUS_GENERAL_PARTNER = 2; // генеральный партнер
    public final static int STATUS_OFFICIAL_PARTNER = 3; // официальный партнер
    public final static int STATUS_OFFICIAL_TRANSLATOR = 4; // официальный переводчик
    public final static int STATUS_GENERAL_SPONSOR = 5; // генеральный спонсор
    public final static int STATUS_SPONSOR = 6; // спонсор
    public final static int STATUS_PARTICIPANT = 7; // участник выставки

    public Integer status;
    public String logo;
    public String phone;
    public String email;
    public @Null String site;

    // translatable fields
    public @Translatable String shortname;
    public @Translatable String fullname;
    public @Translatable @Null String description;
    public @Translatable String address;
}
