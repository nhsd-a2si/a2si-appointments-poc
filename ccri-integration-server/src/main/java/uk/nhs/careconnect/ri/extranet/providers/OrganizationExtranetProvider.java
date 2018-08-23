package uk.nhs.careconnect.ri.extranet.providers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Validate;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.ValidationModeEnum;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class OrganizationExtranetProvider implements IResourceProvider {

    @Autowired
    FhirContext ctx;




    public Class<? extends IBaseResource> getResourceType() {
        return Organization.class;
    }


    @Validate
    public MethodOutcome validate(@ResourceParam Organization organization,
                                         @Validate.Mode ValidationModeEnum theMode,
                                         @Validate.Profile String theProfile) {

        // Actually do our validation: The UnprocessableEntityException
        // results in an HTTP 422, which is appropriate for business rule failure



        MethodOutcome retVal = new MethodOutcome();

        OperationOutcome outcome = ValidationFactory.validateResource(organization);


        retVal.setOperationOutcome(outcome);

        return retVal;
    }



}
