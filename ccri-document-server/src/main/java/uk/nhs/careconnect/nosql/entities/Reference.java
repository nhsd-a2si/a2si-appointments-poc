package uk.nhs.careconnect.nosql.entities;

import com.mongodb.DBRef;

public class Reference {
    String reference;
    String display;
    Identifier identifier;


    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public void setIdentifier(Identifier identifier) {
        this.identifier = identifier;
    }
}
