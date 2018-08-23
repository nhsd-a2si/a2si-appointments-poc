package uk.nhs.careconnect.ri.extranet.providers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Validate;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.ValidationModeEnum;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.dstu3.model.Location;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class LocationExtranetProvider implements IResourceProvider {

    @Autowired
    FhirContext ctx;




    public Class<? extends IBaseResource> getResourceType() {
        return Location.class;
    }


    @Validate
    public MethodOutcome validate(@ResourceParam Location resource,
                                  @Validate.Mode ValidationModeEnum theMode,
                                  @Validate.Profile String theProfile) {

        MethodOutcome retVal = new MethodOutcome();
        OperationOutcome outcome = ValidationFactory.validateResource(resource);
        retVal.setOperationOutcome(outcome);
        return retVal;
    }



}
