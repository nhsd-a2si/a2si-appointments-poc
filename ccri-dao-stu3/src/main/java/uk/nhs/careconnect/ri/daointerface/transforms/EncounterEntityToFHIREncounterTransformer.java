package uk.nhs.careconnect.ri.daointerface.transforms;

import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.entity.BaseAddress;
import uk.nhs.careconnect.ri.entity.encounter.EncounterEntity;
import uk.nhs.careconnect.ri.entity.encounter.EncounterIdentifier;
import uk.org.hl7.fhir.core.Stu3.CareConnectProfile;

@Component
public class EncounterEntityToFHIREncounterTransformer implements Transformer<EncounterEntity, Encounter> {

    private final Transformer<BaseAddress, Address> addressTransformer;

    public EncounterEntityToFHIREncounterTransformer(@Autowired Transformer<BaseAddress, Address> addressTransformer) {
        this.addressTransformer = addressTransformer;
    }

    @Override
    public Encounter transform(final EncounterEntity encounterEntity) {
        final Encounter encounter = new Encounter();

        Meta meta = new Meta().addProfile(CareConnectProfile.Encounter_1);

        if (encounterEntity.getUpdated() != null) {
            meta.setLastUpdated(encounterEntity.getUpdated());
        }
        else {
            if (encounterEntity.getCreated() != null) {
                meta.setLastUpdated(encounterEntity.getCreated());
            }
        }
        encounter.setMeta(meta);

        encounter.setId(encounterEntity.getId().toString());

        for(EncounterIdentifier identifier : encounterEntity.getIdentifiers())
        {
            encounter.addIdentifier()
                    .setSystem(identifier.getSystem().getUri())
                    .setValue(identifier.getValue());
        }

        if (encounterEntity.getPatient() != null) {
            encounter.setSubject(new Reference("Patient/"+encounterEntity.getPatient().getId()));
        }
        if (encounterEntity.getServiceProvider() != null) {
            encounter.setServiceProvider(new Reference("Organization/"+encounterEntity.getServiceProvider().getId()));
        }
        if (encounterEntity.getType() != null) {
            encounter.addType().addCoding()
                    .setCode(encounterEntity.getType().getCode())
                    .setSystem(encounterEntity.getType().getSystem())
                    .setDisplay(encounterEntity.getType().getDisplay());
        }
        if (encounterEntity._getClass() != null) {
            encounter.getClass_()
                    .setCode(encounterEntity._getClass().getCode())
                    .setSystem(encounterEntity._getClass().getSystem())
                    .setDisplay(encounterEntity._getClass().getDisplay());
        }
        if (encounterEntity.getStatus() !=null) {
            encounter.setStatus(encounterEntity.getStatus());
        }
        if (encounterEntity.getPeriodStartDate() != null || encounterEntity.getPeriodEndDate() != null)
        {
            Period period = new Period();
            if (encounterEntity.getPeriodStartDate() != null ) {
                period.setStart(encounterEntity.getPeriodStartDate());
            }
            if (encounterEntity.getPeriodEndDate() != null) {
                period.setEnd(encounterEntity.getPeriodEndDate());
            }
            encounter.setPeriod(period);
        }
        if (encounterEntity.getLocation()!=null) {
            encounter.addLocation().setLocation(new Reference("Location/"+encounterEntity.getLocation().getId()));
        }






        return encounter;

    }
}