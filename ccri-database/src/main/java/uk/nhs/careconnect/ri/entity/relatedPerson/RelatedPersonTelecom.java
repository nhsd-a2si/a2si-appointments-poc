package uk.nhs.careconnect.ri.entity.relatedPerson;

import uk.nhs.careconnect.ri.entity.BaseContactPoint;


import javax.persistence.*;


@Entity
@Table(name="RelatedPersonTelecom",
		uniqueConstraints= @UniqueConstraint(name="PK_PERSON_TELECOM", columnNames={"PERSON_TELECOM_ID"})
		,indexes =
		{
				@Index(name = "IDX_PERSON_TELECOM", columnList="CONTACT_VALUE,SYSTEM_ID")
		})
public class RelatedPersonTelecom extends BaseContactPoint {

	public RelatedPersonTelecom() {

	}

	public RelatedPersonTelecom(RelatedPersonEntity personEntity) {
		this.personEntity = personEntity;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "PERSON_TELECOM_ID")
	private Long identifierId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "PERSON_ID",foreignKey= @ForeignKey(name="FK_PERSON_PERSON_TELECOM"))
	private RelatedPersonEntity personEntity;


    public Long getTelecomId() { return identifierId; }
	public void setTelecomId(Long identifierId) { this.identifierId = identifierId; }

	public RelatedPersonEntity getRelatedPerson() {
	        return this.personEntity;
	}
	public void setRelatedPersonEntity(RelatedPersonEntity organisationEntity) {
	        this.personEntity = personEntity;
	}

}
