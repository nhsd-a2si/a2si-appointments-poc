package uk.nhs.careconnect.ri.entity.schedule;

import uk.nhs.careconnect.ri.entity.BaseIdentifier;
import uk.nhs.careconnect.ri.entity.condition.ConditionEntity;

import javax.persistence.*;


@Entity
@Table(name="ScheduleIdentifier", uniqueConstraints= @UniqueConstraint(name="PK_SERVICE_IDENTIFIER", columnNames={"SERVICE_IDENTIFIER_ID"})
		)
public class ScheduleIdentifier extends BaseIdentifier {

	public ScheduleIdentifier() {

	}

	public ScheduleIdentifier(ScheduleEntity service) {
		this.service = service;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "SERVICE_IDENTIFIER_ID")
	private Long identifierId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "SERVICE_ID",foreignKey= @ForeignKey(name="FK_SERVICE_SERVICE_IDENTIFIER"))
	private ScheduleEntity service;


    public Long getIdentifierId() { return identifierId; }
	public void setIdentifierId(Long identifierId) { this.identifierId = identifierId; }


	public ScheduleEntity getService() {
		return service;
	}

	public void setService(ScheduleEntity service) {
		this.service = service;
	}
}
