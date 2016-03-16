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
package ru.gkpromtech.exhibition.persons;

import java.io.Serializable;

import ru.gkpromtech.exhibition.model.Person;

/**
 * Created by karunass on 14.04.15.
 */
public class PersonItem implements Serializable{
    public Person person;

    public PersonItem(Person person) {
        this.person = person;
    }

    public PersonItem(){
    }

}
