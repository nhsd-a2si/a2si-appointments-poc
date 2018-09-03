package uk.nhs.careconnect.ri.entity.schedule;


import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import uk.nhs.careconnect.ri.entity.BaseResource;
import uk.nhs.careconnect.ri.entity.location.LocationEntity;


import javax.persistence.*;

@Entity
@Table(name = "ScheduleLocation")
public class ScheduleLocation extends BaseResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="SERVICE_LOCATION_ID")
    private Long myId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SERVICE_ID",foreignKey= @ForeignKey(name="FK_SERVICE_LOCATION_SERVICE_ID"))
    private ScheduleEntity service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="LOCATION_ID",foreignKey= @ForeignKey(name="FK_SERVICE_LOCATION_LOCATION_ID"))

    private LocationEntity location;

    public Long getId()
    {
        return this.myId;
    }

    public LocationEntity getLocation() {
        return location;
    }

    public ScheduleEntity setLocation(LocationEntity location) {
        this.location = location;
        return this.service;
    }

    public ScheduleEntity getSchedule() {
        return service;
    }

    public ScheduleLocation setSchedule(ScheduleEntity service) {
        this.service = service;
        return this;
    }


}
