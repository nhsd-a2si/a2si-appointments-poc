package uk.nhs.careconnect.ri.entity.schedule;


import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import uk.nhs.careconnect.ri.entity.BaseResource;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;

import javax.persistence.*;

@Entity
@Table(name = "ScheduleType")
public class ScheduleType extends BaseResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="SERVICE_TYPE_ID")
    private Long myId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SERVICE_ID",foreignKey= @ForeignKey(name="FK_SERVICE_TYPE_SERVICE_ROLE_ID"))
    private ScheduleEntity service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="TYPE_CONCEPT_ID",foreignKey= @ForeignKey(name="FK_SERVICE_TYPE_TYPE_CONCEPT_ID"))

    private ConceptEntity type_;

    public Long getId()
    {
        return this.myId;
    }


    public ScheduleEntity getSchedule() {
        return service;
    }

    public ScheduleType setSchedule(ScheduleEntity service) {
        this.service = service;
        return this;
    }

    public ScheduleEntity getService() {
        return service;
    }

    public void setService(ScheduleEntity service) {
        this.service = service;
    }

    public ConceptEntity getType_() {
        return type_;
    }

    public void setType_(ConceptEntity type_) {
        this.type_ = type_;
    }
}
