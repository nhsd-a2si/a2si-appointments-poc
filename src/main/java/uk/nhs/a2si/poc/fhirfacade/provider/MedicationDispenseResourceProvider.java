package uk.nhs.a2si.poc.fhirfacade.provider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import org.apache.camel.*;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.MedicationDispense;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.a2si.poc.fhirfacade.server.ProviderResponseLibrary;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

@Component
public class MedicationDispenseResourceProvider implements IResourceProvider {

    @Autowired
    CamelContext context;

    @Autowired
    FhirContext ctx;

    @Autowired
    ResourceTestProvider resourceTestProvider;

    private static final Logger log = LoggerFactory.getLogger(MedicationDispenseResourceProvider.class);

    @Override
    public Class<MedicationDispense> getResourceType() {
        return MedicationDispense.class;
    }

    /*
    @Validate
    public MethodOutcome testResource(@ResourceParam MedicationDispense resource,
                                  @Validate.Mode ValidationModeEnum theMode,
                                  @Validate.Profile String theProfile) {
        return resourceTestProvider.testResource(resource,theMode,theProfile);
    }
    */
    /*
    public Bundle getEverythingOperation(
            @IdParam IdType patientId
            ,CompleteBundle completeBundle
    ) {

        Bundle bundle = completeBundle.getBundle();

        List<MedicationDispense> resources = searchMedicationDispense(null, new ReferenceParam().setValue(patientId.getValue()),null,null,null, null);

        for (MedicationDispense resource : resources) {
            if (resource.getRequester()!= null && resource.getRequester().hasAgent()) {
                Reference reference = resource.getRequester().getAgent();
                if (reference.getReference().contains("Practitioner")) {
                    completeBundle.addGetPractitioner(new IdType(reference.getReference()));
                }
                if (reference.getReference().contains("Organization")) {
                    completeBundle.addGetOrganisation(new IdType(reference.getReference()));
                }
            }
            bundle.addEntry().setResource(resource);
        }
        // Populate bundle with matching resources
        return bundle;
    }
*/
    @Read
    public MedicationDispense getMedicationDispenseById(HttpServletRequest httpRequest, @IdParam IdType internalId) throws Exception {

        ProducerTemplate template = context.createProducerTemplate();



        MedicationDispense dispense = null;
        IBaseResource resource = null;
        try {
            InputStream inputStream = (InputStream)  template.sendBody("direct:FHIRMedicationDispense",
                    ExchangePattern.InOut,httpRequest);


            Reader reader = new InputStreamReader(inputStream);
            resource = ctx.newJsonParser().parseResource(reader);
        } catch(Exception ex) {
            log.error("JSON Parse failed " + ex.getMessage());
            throw new InternalErrorException(ex.getMessage());
        }
        if (resource instanceof MedicationDispense) {
            dispense = (MedicationDispense) resource;
        } else {
            ProviderResponseLibrary.createException(ctx,resource);
        }

        return dispense;
    }

    @Search
    public List<MedicationDispense> searchMedicationDispense(HttpServletRequest httpRequest,
                                                             @OptionalParam(name = MedicationDispense.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = MedicationDispense.SP_STATUS) TokenParam status
            , @OptionalParam(name = MedicationDispense.SP_RES_ID) TokenParam id
            , @OptionalParam(name = MedicationDispense.SP_IDENTIFIER) TokenParam identifier
            , @OptionalParam(name = MedicationDispense.SP_CODE) TokenParam code
            , @OptionalParam(name= MedicationDispense.SP_MEDICATION) ReferenceParam medication
                                       ) throws Exception {

        List<MedicationDispense> results = new ArrayList<MedicationDispense>();

        ProducerTemplate template = context.createProducerTemplate();

        InputStream inputStream = null;
        if (httpRequest != null) {
            inputStream = (InputStream) template.sendBody("direct:FHIRMedicationDispense",
                ExchangePattern.InOut,httpRequest);
        } else {
            Exchange exchange = template.send("direct:FHIRMedicationDispense",ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "?patient="+patient.getIdPart());
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, "GET");
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, "MedicationDispense");
                }
            });
            inputStream = (InputStream) exchange.getIn().getBody();
        }
        Bundle bundle = null;

        Reader reader = new InputStreamReader(inputStream);
        IBaseResource resource = null;
        try {
            resource = ctx.newJsonParser().parseResource(reader);
        } catch(Exception ex) {
            log.error("JSON Parse failed " + ex.getMessage());
            throw new InternalErrorException(ex.getMessage());
        }
        if (resource instanceof Bundle) {
            bundle = (Bundle) resource;
            for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                MedicationDispense dispense = (MedicationDispense) entry.getResource();
                results.add(dispense);
            }

    } else {
        ProviderResponseLibrary.createException(ctx,resource);
    }

        return results;

    }



}
