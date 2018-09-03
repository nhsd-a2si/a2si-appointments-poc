package uk.nhs.careconnect.ri.entity.schedule;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import uk.nhs.careconnect.ri.entity.BaseResource;

import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.entity.organization.OrganisationEntity;


import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "Schedule")
public class ScheduleEntity extends BaseResource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="SERVICE_ID")
    private Long id;

    @Column(name="ACTIVE")
    private Boolean active;

    @Column(name="NAME")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="ORGANISATION_ID",foreignKey= @ForeignKey(name="FK_SERVICE_SPECIALTY_ORGANISATION_ID"))

    private OrganisationEntity providedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="CATEGORY_CONCEPT_ID",foreignKey= @ForeignKey(name="FK_SERVICE_CATEGORY_CONCEPT"))
    private ConceptEntity category;

    @OneToMany(mappedBy="service", targetEntity = ScheduleTelecom.class)

    Set<ScheduleTelecom> telecoms = new HashSet<>();

    @OneToMany(mappedBy="service", targetEntity = ScheduleIdentifier.class)

    Set<ScheduleIdentifier> identifiers = new HashSet<>();

    @OneToMany(mappedBy="service", targetEntity = ScheduleSpecialty.class)

    Set<ScheduleSpecialty> specialties = new HashSet<>();

    @OneToMany(mappedBy="service", targetEntity = ScheduleLocation.class)

    Set<ScheduleLocation> locations = new HashSet<>();

    @OneToMany(mappedBy="service", targetEntity = ScheduleType.class)

    Set<ScheduleType> types = new HashSet<>();

    public ConceptEntity getCategory() {
        return category;
    }

    public void setCategory(ConceptEntity category) {
        this.category = category;
    }

    public Set<ScheduleType> getTypes() {
        return types;
    }

    public ScheduleEntity setTypes(Set<ScheduleType> types) {
        this.types = types;
        return this;
    }

    public String getName() {
        return name;
    }

    public ScheduleEntity setName(String name) {
        this.name = name;
        return this;
    }

    public Set<ScheduleLocation> getLocations() {
        return locations;
    }

    public ScheduleEntity setLocations(Set<ScheduleLocation> locations) {
        this.locations = locations;
        return this;
    }

    public Boolean getActive() {
        return active;
    }

    public ScheduleEntity setActive(Boolean active) {
        this.active = active;
        return this;
    }

    public OrganisationEntity getProvidedBy() {
        return providedBy;
    }

    public ScheduleEntity setProvidedBy(OrganisationEntity providedBy) {
        this.providedBy = providedBy;
        return this;
    }

    public Set<ScheduleTelecom> getTelecoms() {
        return telecoms;
    }

    public ScheduleEntity setTelecoms(Set<ScheduleTelecom> telecoms) {
        this.telecoms = telecoms;
        return this;
    }

    public Set<ScheduleSpecialty> getSpecialties() {
        return specialties;
    }

    public ScheduleEntity setSpecialties(Set<ScheduleSpecialty> specialties) {
        this.specialties = specialties;
        return this;
    }

    public Long getId() {
        return id;
    }

    public Set<ScheduleIdentifier> getIdentifiers() {
        return identifiers;
    }

    public ScheduleEntity setIdentifiers(Set<ScheduleIdentifier> identifiers) {
        this.identifiers = identifiers;
        return this;
    }
}
