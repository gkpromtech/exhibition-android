package ru.gkpromtech.exhibition.model;

import java.io.Serializable;

import ru.gkpromtech.exhibition.model.annotation.PK;

public abstract class Entity implements Serializable {
    public @PK Integer id;
}
