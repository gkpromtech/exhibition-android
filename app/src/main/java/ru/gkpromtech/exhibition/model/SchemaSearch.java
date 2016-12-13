package ru.gkpromtech.exhibition.model;

import ru.gkpromtech.exhibition.model.annotation.TableRef;

@TableRef(name="schema_search")
public class SchemaSearch extends Entity {
    public String entity;
    public String name;
}
