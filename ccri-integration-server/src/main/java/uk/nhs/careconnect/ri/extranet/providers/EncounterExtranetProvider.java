package uk.nhs.careconnect.ri.extranet.providers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.valueset.BundleTypeEnum;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.ValidationModeEnum;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import org.apache.camel.*;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.extranet.dao.IComposition;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class EncounterExtranetProvider implements IResourceProvider {

    @Autowired
    CamelContext context;

    @Autowired
    FhirContext ctx;

    @Autowired
    IComposition compositionDao;

    @Value("${fhir.restserver.eprBase}")
    private String eprBase;

    private static final Logger log = LoggerFactory.getLogger(EncounterExtranetProvider.class);

    @Override
    public Class<Encounter> getResourceType() {
        return Encounter.class;
    }


    @Operation(name = "document", idempotent = true, bundleType= BundleTypeEnum.DOCUMENT)
    public Bundle encounterDocumentOperation(
            @IdParam IdType encounterId

    ) {
        log.info("In Encounter document " +encounterId.getIdPart());

        HttpServletRequest request =  null;

        IGenericClient client = FhirContext.forDstu3().newRestfulGenericClient(eprBase);

        log.info("Build client");
        client.setEncoding(EncodingEnum.XML);

        log.info("calling composition");
        Bundle fhirDocument = null;
        try {
            fhirDocument = compositionDao.buildEncounterDocument(client,encounterId);
        } catch (Exception ex) {
            throw new InternalErrorException(ex.getMessage());
        }
        return fhirDocument;

    }
    @Validate
    public MethodOutcome validate(@ResourceParam Encounter encounter,
                                         @Validate.Mode ValidationModeEnum theMode,
                                         @Validate.Profile String theProfile) {

        MethodOutcome retVal = new MethodOutcome();
        OperationOutcome outcome = ValidationFactory.validateResource(encounter);

        retVal.setOperationOutcome(outcome);
        return retVal;
    }





}
