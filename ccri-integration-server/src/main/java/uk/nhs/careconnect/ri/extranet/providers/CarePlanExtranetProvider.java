package uk.nhs.careconnect.ri.extranet.providers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.valueset.BundleTypeEnum;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Validate;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.ValidationModeEnum;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import org.apache.camel.CamelContext;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.CarePlan;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.extranet.dao.IComposition;

import javax.servlet.http.HttpServletRequest;

@Component
public class CarePlanExtranetProvider implements IResourceProvider {

    @Autowired
    CamelContext context;

    @Autowired
    FhirContext ctx;

    @Autowired
    IComposition compositionDao;

    @Value("${fhir.restserver.eprBase}")
    private String eprBase;

    private static final Logger log = LoggerFactory.getLogger(CarePlanExtranetProvider.class);

    @Override
    public Class<CarePlan> getResourceType() {
        return CarePlan.class;
    }


    @Operation(name = "document", idempotent = true, bundleType= BundleTypeEnum.DOCUMENT)
    public Bundle carePlanDocumentOperation(
            @IdParam IdType carePlanId

    ) {
        log.info("In CarePlan document " +carePlanId.getIdPart());

        HttpServletRequest request =  null;

        IGenericClient client = FhirContext.forDstu3().newRestfulGenericClient(eprBase);

        log.info("Build client");
        client.setEncoding(EncodingEnum.XML);

        log.info("calling composition");
        Bundle fhirDocument = null;
        try {
            fhirDocument = compositionDao.buildCarePlanDocument(client,carePlanId);
        } catch (Exception ex) {
            throw new InternalErrorException(ex.getMessage());
        }
        return fhirDocument;

    }
    @Validate
    public MethodOutcome validate(@ResourceParam CarePlan carePlan,
                                         @Validate.Mode ValidationModeEnum theMode,
                                         @Validate.Profile String theProfile) {

        MethodOutcome retVal = new MethodOutcome();
        OperationOutcome outcome = ValidationFactory.validateResource(carePlan);

        retVal.setOperationOutcome(outcome);
        return retVal;
    }





}
