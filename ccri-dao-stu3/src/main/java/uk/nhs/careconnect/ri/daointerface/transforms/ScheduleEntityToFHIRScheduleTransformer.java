package uk.nhs.careconnect.ri.daointerface.transforms;

import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.Schedule;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.Reference;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.entity.schedule.*;


@Component
public class ScheduleEntityToFHIRScheduleTransformer implements Transformer<ScheduleEntity, Schedule> {

    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ScheduleEntityToFHIRScheduleTransformer.class);
    

    @Override
    public Schedule transform(final ScheduleEntity serviceEntity) {
        final Schedule service = new Schedule();

        Meta meta = new Meta(); //.addProfile(CareConnectProfile.Location_1);

        if (serviceEntity.getUpdated() != null) {
            meta.setLastUpdated(serviceEntity.getUpdated());
        }
        else {
            if (serviceEntity.getCreated() != null) {
                meta.setLastUpdated(serviceEntity.getCreated());
            }
        }
        service.setMeta(meta);

        service.setId(serviceEntity.getId().toString());

        for(ScheduleIdentifier identifier : serviceEntity.getIdentifiers())
        {
            service.addIdentifier()
                    .setSystem(identifier.getSystem().getUri())
                    .setValue(identifier.getValue());
        }

        if (serviceEntity.getActive() != null) {
            service.setActive(serviceEntity.getActive());
        }
        if (serviceEntity.getName() != null) {
            service.setName(service.getName());
        }
        if (serviceEntity.getCategory() != null) {
            service.getCategory()
                    .addCoding()
                    .setDisplay(serviceEntity.getCategory().getDisplay())
                    .setSystem(serviceEntity.getCategory().getSystem())
                    .setCode(serviceEntity.getCategory().getCode());
        }

        if (serviceEntity.getProvidedBy() != null) {
            service.setProvidedBy(new Reference("Organization/"+serviceEntity.getProvidedBy().getId()));
        }
        for (ScheduleSpecialty serviceSpecialty : serviceEntity.getSpecialties()) {
            service.addSpecialty()
                    .addCoding()
                        .setCode(serviceSpecialty.getSpecialty().getCode())
                        .setSystem(serviceSpecialty.getSpecialty().getSystem())
                        .setDisplay(serviceSpecialty.getSpecialty().getDisplay());
        }
        for (ScheduleLocation serviceLocation : serviceEntity.getLocations()) {
            service.addLocation(new Reference("Location/"+serviceLocation.getLocation().getId()));
        }
        for (ScheduleTelecom serviceTelecom : serviceEntity.getTelecoms()) {
            service.addTelecom()
                    .setSystem(serviceTelecom.getSystem())
                    .setValue(serviceTelecom.getValue())
                    .setUse(serviceTelecom.getTelecomUse());

        }
        for (ScheduleType serviceType : serviceEntity.getTypes()) {
            service.addType()
                    .addCoding()
                    .setCode(serviceType.getType_().getCode())
                    .setSystem(serviceType.getType_().getSystem())
                    .setDisplay(serviceType.getType_().getDisplay());
        }


        return service;

    }
}
