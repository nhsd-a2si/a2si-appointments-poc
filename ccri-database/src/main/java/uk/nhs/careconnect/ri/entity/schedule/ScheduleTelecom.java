package uk.nhs.careconnect.ri.entity.schedule;
import uk.nhs.careconnect.ri.entity.BaseContactPoint;
import javax.persistence.*;


@Entity
@Table(name="ScheduleTelecom", uniqueConstraints= @UniqueConstraint(name="PK_SERVICE_TELECOM", columnNames={"SERVICE_TELECOM_ID"}))
public class ScheduleTelecom extends BaseContactPoint {

	public ScheduleTelecom() {

	}

	public ScheduleTelecom(ScheduleEntity serviceEntity) {
		this.service = serviceEntity;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "SERVICE_TELECOM_ID")
	private Long identifierId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "SERVICE_ID",foreignKey= @ForeignKey(name="FK_SERVICE_SERVICE_TELECOM"))
	private ScheduleEntity service;


    public Long getTelecomId() { return identifierId; }
	public void setTelecomId(Long identifierId) { this.identifierId = identifierId; }

	public ScheduleEntity getSchedule() {
	        return this.service;
	}
	public void setSchedule(ScheduleEntity serviceEntity) {
	        this.service = serviceEntity;
	}

}
