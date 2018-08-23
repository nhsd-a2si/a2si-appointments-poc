package uk.nhs.careconnect.nosql.dao.transform;

import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.*;
import org.springframework.stereotype.Component;

import uk.nhs.careconnect.nosql.entities.Identifier;
import uk.nhs.careconnect.nosql.entities.Name;
import uk.nhs.careconnect.nosql.entities.PatientEntity;
import uk.nhs.careconnect.nosql.entities.Telecom;


@Component
public class PatientEntityToFHIRPatient implements Transformer<PatientEntity, Patient> {
    @Override
    public Patient transform(PatientEntity patientEntity) {
        final Patient patient = new Patient();

        Meta meta = new Meta().addProfile("https://fhir.hl7.org.uk/STU3/StructureDefinition/CareConnect-Patient-1");

        patient.setMeta(meta);

        patient.setId(patientEntity.getId().toString());

        for (Identifier identifier : patientEntity.getIdentifiers()) {
            patient.addIdentifier()
                    .setSystem(identifier.getSystem())
                    .setValue(identifier.getValue());
        }
        for (Name name : patientEntity.getNames() ) {
            HumanName humanName = patient.addName();

            humanName.setFamily(name.getFamilyName())
                    .addGiven(name.getGivenName());
            if (name.getNameUse() != null) {
                 humanName.setUse(name.getNameUse());
            }
            if (name.getPrefix()!=null) {
                humanName.getPrefix().add(new StringType(name.getPrefix()));
            }
        }
        if (patientEntity.getDateOfBirth() != null) {
            patient.setBirthDate(patientEntity.getDateOfBirth());
        }
        if (patientEntity.getGender()!=null) {
            patient.setGender(patientEntity.getGender());
        }
        for (Telecom telecom : patientEntity.getTelecoms()) {
            ContactPoint contactPoint = new ContactPoint();
            contactPoint.setValue(telecom.getValue());
            if (telecom.getSystem() != null) contactPoint.setSystem(telecom.getSystem());
            if (telecom.getTelecomUse()!=null) {
                contactPoint.setUse(telecom.getTelecomUse());
            }
            patient.getTelecom().add(contactPoint);
        }
        for (uk.nhs.careconnect.nosql.entities.Address addressEntity : patientEntity.getAddresses()) {
            Address address = new Address();
            patient.getAddress().add(address);

            for (String line : addressEntity.getLines()) {
                address.addLine(line);
            }
            if (addressEntity.getCity()!=null) address.setCity(addressEntity.getCity());
            if (addressEntity.getCounty()!=null) address.setDistrict(addressEntity.getCounty());
            if (addressEntity.getPostcode()!=null) address.setPostalCode(addressEntity.getPostcode());
            if (addressEntity.getUse()!=null) address.setUse(addressEntity.getUse());
        }


        return patient;
    }
}
