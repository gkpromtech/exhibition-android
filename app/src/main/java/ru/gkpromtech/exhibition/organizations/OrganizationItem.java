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
package ru.gkpromtech.exhibition.organizations;

import java.io.Serializable;

import ru.gkpromtech.exhibition.model.Group;
import ru.gkpromtech.exhibition.model.Organization;
import ru.gkpromtech.exhibition.model.Place;

public class OrganizationItem implements Serializable {
    public Group group;
    public Place place;
    public Organization organization;

    public OrganizationItem(Group group, Place place, Organization organization) {
        this.group = group;
        this.place = place;
        this.organization = organization;
    }

    public OrganizationItem() {
    }
}
