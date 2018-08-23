package uk.nhs.careconnect.ri.extranet.providers;

import ca.uhn.fhir.context.FhirContext;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.ValidationModeEnum;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;



@Component
public class BundleExtranetProvider implements IResourceProvider {

    @Autowired
    FhirContext ctx;




    public Class<? extends IBaseResource> getResourceType() {
        return Bundle.class;
    }


    @Validate
    public MethodOutcome validate(@ResourceParam Bundle bundle,
                                         @Validate.Mode ValidationModeEnum theMode,
                                         @Validate.Profile String theProfile) {

        // Actually do our validation: The UnprocessableEntityException
        // results in an HTTP 422, which is appropriate for business rule failure


        // This method returns a MethodOutcome object
        MethodOutcome retVal = new MethodOutcome();

        // You may also add an OperationOutcome resource to return
        // This part is optional though:
        OperationOutcome outcome = ValidationFactory.validateResource(bundle);


        retVal.setOperationOutcome(outcome);

        return retVal;
    }



}
