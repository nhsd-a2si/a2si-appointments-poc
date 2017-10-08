package uk.nhs.careconnect.ri.fhirserver.provider;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.daointerface.PatientRepository;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
public class PatientProvider implements IResourceProvider {


    @Autowired
    private PractitionerProvider practitionerResourceProvider;

    @Autowired
    private OrganizationProvider organizationResourceProvider;

    @Autowired
    private PatientRepository patientDao;


    @Override
    public Class<Patient> getResourceType() {
        return Patient.class;
    }


    @Read
    public Patient getPatientById(@IdParam IdType internalId) {
        Patient patient = patientDao.read(internalId);

        if (patient == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No patient details found for patient ID: " + internalId.getIdPart()),
                    OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTFOUND);
        }

        return patient;
    }

    @Search
    public List<Patient> searchPatient(HttpServletRequest theRequest,

                                       @OptionalParam(name= Patient.SP_ADDRESS_POSTALCODE) StringParam addressPostcode,
                                       @OptionalParam(name= Patient.SP_BIRTHDATE) DateRangeParam birthDate,
                                       @OptionalParam(name= Patient.SP_EMAIL) StringParam email,
                                       @OptionalParam(name = Patient.SP_FAMILY) StringParam familyName,
                                       @OptionalParam(name= Patient.SP_GENDER) StringParam gender ,
                                       @OptionalParam(name= Patient.SP_GIVEN) StringParam givenName ,
                                       @OptionalParam(name = Patient.SP_IDENTIFIER) TokenParam identifier,
                                       @OptionalParam(name= Patient.SP_NAME) StringParam name,
                                       @OptionalParam(name= Patient.SP_PHONE) StringParam phone
                                       ) {


        return patientDao.searchPatient(addressPostcode, birthDate, email, familyName, gender,givenName, identifier, name, phone);


    }



}