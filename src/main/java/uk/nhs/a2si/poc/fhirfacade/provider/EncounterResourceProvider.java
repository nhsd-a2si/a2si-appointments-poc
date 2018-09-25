package uk.nhs.a2si.poc.fhirfacade.provider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.valueset.BundleTypeEnum;
import ca.uhn.fhir.rest.annotation.*;
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
import org.springframework.stereotype.Component;
import uk.nhs.a2si.poc.fhirfacade.server.ProviderResponseLibrary;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class EncounterResourceProvider implements IResourceProvider {

    @Autowired
    CamelContext context;

    @Autowired
    FhirContext ctx;

    @Autowired
    ResourceTestProvider resourceTestProvider;

    private static final Logger log = LoggerFactory.getLogger(EncounterResourceProvider.class);

    @Override
    public Class<Encounter> getResourceType() {
        return Encounter.class;
    }
/*
    @Validate
    public MethodOutcome testResource(@ResourceParam Encounter resource,
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

        List<Resource> resources = searchEncounter(null, new ReferenceParam().setValue(patientId.getValue()),null,null, null);

        for (Resource resource : resources) {
            if (resource instanceof Encounter) {
                Encounter encounter = (Encounter) resource;
                for (Encounter.EncounterParticipantComponent component : encounter.getParticipant()) {
                    Reference reference = component.getIndividual();
                    if (reference.getReference().contains("Practitioner")) {
                        completeBundle.addGetPractitioner(new IdType(reference.getReference()));
                    }
                    if (reference.getReference().contains("Organization")) {
                        completeBundle.addGetOrganisation(new IdType(reference.getReference()));
                    }
                }
                bundle.addEntry().setResource(resource);
            }
        }
        // Populate bundle with matching resources
        return bundle;
    }
    */

    @Operation(name = "document", idempotent = true, bundleType= BundleTypeEnum.DOCUMENT)
    public Bundle encounterDocumentOperation(
            @IdParam IdType encounterId

    ) throws Exception {
        ProducerTemplate template = context.createProducerTemplate();

        InputStream inputStream = null;
        // https://purple.testlab.nhs.uk/careconnect-ri/STU3/Encounter/804/$document?_count=50
        Exchange exchange = template.send("direct:FHIREncounterDocument",ExchangePattern.InOut, new Processor() {
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setHeader(Exchange.HTTP_QUERY, "_count=50");
                exchange.getIn().setHeader(Exchange.HTTP_METHOD, "GET");
                exchange.getIn().setHeader(Exchange.HTTP_PATH, "Encounter/"+encounterId.getIdPart()+"/$document");
            }
        });
        inputStream = (InputStream) exchange.getIn().getBody();

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

            return bundle;
            /*
            for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {

                results.add(entry.getResource());
            }
            */
        } else {
            ProviderResponseLibrary.createException(ctx,resource);
        }

        return null;

    }

    @Read
    public Encounter getEncounterById(HttpServletRequest httpRequest, @IdParam IdType internalId) throws Exception {

        ProducerTemplate template = context.createProducerTemplate();



        Encounter encounter = null;
        IBaseResource resource = null;
        try {
            InputStream inputStream = (InputStream)  template.sendBody("direct:FHIREncounter",
                    ExchangePattern.InOut,httpRequest);


            Reader reader = new InputStreamReader(inputStream);
            resource = ctx.newJsonParser().parseResource(reader);
        } catch(Exception ex) {
            log.error("JSON Parse failed " + ex.getMessage());
            throw new InternalErrorException(ex.getMessage());
        }
        if (resource instanceof Encounter) {
            encounter = (Encounter) resource;
        } else {
            ProviderResponseLibrary.createException(ctx,resource);
        }

        return encounter;
    }



    @Search
    public List<Resource> searchEncounter(HttpServletRequest httpRequest,
                                           @OptionalParam(name = Encounter.SP_PATIENT) ReferenceParam patient
            ,@OptionalParam(name = Encounter.SP_DATE) DateRangeParam date
          //  ,@OptionalParam(name = Encounter.SP_EPISODEOFCARE) ReferenceParam episode
            , @OptionalParam(name = Encounter.SP_RES_ID) TokenParam resid
            , @IncludeParam(reverse=true, allow = {"*"}) Set<Include> reverseIncludes
            , @IncludeParam(allow = { "Encounter.participant" , "Encounter.subject", "Encounter.service-provider", "Encounter.location", "*"
    }) Set<Include> includes
    ) throws Exception {

        List<Resource> results = new ArrayList<>();

        ProducerTemplate template = context.createProducerTemplate();

        InputStream inputStream = null;
        if (httpRequest != null) {
            inputStream = (InputStream) template.sendBody("direct:FHIREncounter",
                ExchangePattern.InOut,httpRequest);
        } else {
            Exchange exchange = template.send("direct:FHIREncounter",ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, "?patient="+patient.getIdPart());
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, "GET");
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, "Encounter");
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
                //Encounter resourceEntry = (Resource) entry.getResource();
                results.add(entry.getResource());
            }
        } else {
            ProviderResponseLibrary.createException(ctx,resource);
        }

        return results;

    }



}
