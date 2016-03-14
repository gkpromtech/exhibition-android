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
