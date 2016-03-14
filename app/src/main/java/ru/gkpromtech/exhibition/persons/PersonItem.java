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
