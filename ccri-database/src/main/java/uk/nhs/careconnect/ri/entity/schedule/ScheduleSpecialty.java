package uk.nhs.careconnect.ri.entity.schedule;


import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import uk.nhs.careconnect.ri.entity.BaseResource;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;


import javax.persistence.*;

@Entity
@Table(name = "ScheduleSpecialty")
public class ScheduleSpecialty extends BaseResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="SERVICE_SPECIALTY_ID")
    private Long myId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SERVICE_ID",foreignKey= @ForeignKey(name="FK_SERVICE_SPECIALTY_SERVICE_ROLE_ID"))
    private ScheduleEntity service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="SPECIALTY_CONCEPT_ID",foreignKey= @ForeignKey(name="FK_SERVICE_SPECIALTY_SPECIALTY_CONCEPT_ID"))

    private ConceptEntity specialty;

    public Long getId()
    {
        return this.myId;
    }

    public ConceptEntity getSpecialty() {
        return specialty;
    }

    public ScheduleEntity getSchedule() {
        return service;
    }

    public ScheduleSpecialty setSchedule(ScheduleEntity service) {
        this.service = service;
        return this;
    }

    public ScheduleEntity setSpecialty(ConceptEntity specialty) {
        this.specialty = specialty;
        return this.getSchedule();
    }
}
