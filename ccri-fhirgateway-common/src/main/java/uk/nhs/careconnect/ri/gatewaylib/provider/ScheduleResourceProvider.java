package uk.nhs.careconnect.ri.gatewaylib.provider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import org.apache.camel.*;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Schedule;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.lib.ProviderResponseLibrary;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

@Component
public class ScheduleResourceProvider implements IResourceProvider {

    @Autowired
    CamelContext context;

    @Autowired
    FhirContext ctx;

    @Autowired
    ResourceTestProvider resourceTestProvider;

    private static final Logger log = LoggerFactory.getLogger(ScheduleResourceProvider.class);

    @Override
    public Class<Schedule> getResourceType() {
        return Schedule.class;
    }

/*
    @Validate
    public MethodOutcome testResource(@ResourceParam Schedule resource,
                                  @Validate.Mode ValidationModeEnum theMode,
                                  @Validate.Profile String theProfile) {
        return resourceTestProvider.testResource(resource,theMode,theProfile);
    }
*/

    @Read
    public Schedule getScheduleById(HttpServletRequest httpRequest, @IdParam IdType internalId) throws Exception {

        ProducerTemplate template = context.createProducerTemplate();


        Schedule organization = null;
        IBaseResource resource = null;
        try {
            InputStream inputStream = null;
            if (httpRequest != null) {
                inputStream = (InputStream) template.sendBody("direct:FHIRSchedule",
                        ExchangePattern.InOut, httpRequest);
            } else {
                Exchange exchange = template.send("direct:FHIRSchedule",ExchangePattern.InOut, new Processor() {
                    public void process(Exchange exchange) throws Exception {
                        exchange.getIn().setHeader(Exchange.HTTP_QUERY, null);
                        exchange.getIn().setHeader(Exchange.HTTP_METHOD, "GET");
                        exchange.getIn().setHeader(Exchange.HTTP_PATH, "/"+internalId.getValue());
                    }
                });
                inputStream = (InputStream) exchange.getIn().getBody();
            }
            Reader reader = new InputStreamReader(inputStream);
            resource = ctx.newJsonParser().parseResource(reader);
        } catch(Exception ex) {
            log.error("JSON Parse failed " + ex.getMessage());
            throw new InternalErrorException(ex.getMessage());
        }
        if (resource instanceof Schedule) {
            organization = (Schedule) resource;
        } else {
            ProviderResponseLibrary.createException(ctx,resource);
        }
        return organization;
    }

    @Create
    public MethodOutcome create(HttpServletRequest httpRequest, @ResourceParam Schedule schedule) throws Exception {



        ProducerTemplate template = context.createProducerTemplate();

        IBaseResource resource = null;
        try {
            InputStream inputStream = null;
            String newXmlResource = ctx.newXmlParser().encodeResourceToString(schedule);

            Exchange exchangeBundle = template.send("direct:FHIRSchedule", ExchangePattern.InOut, new Processor() {
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setBody(newXmlResource);
                    exchange.getIn().setHeader("Prefer", "return=representation");
                    exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/fhir+xml");
                    exchange.getIn().setHeader(Exchange.HTTP_QUERY, null);
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, "POST");
                    exchange.getIn().setHeader(Exchange.HTTP_PATH, "Schedule");
                }
            });

            // This response is coming from an external FHIR Server, so uses inputstream
            resource = ProviderResponseLibrary.processMessageBody(ctx,resource,exchangeBundle.getIn().getBody());

        } catch(Exception ex) {
            log.error("XML Parse failed " + ex.getMessage());
            throw new InternalErrorException(ex.getMessage());
        }
        log.trace("RETURNED Resource "+resource.getClass().getSimpleName());

        if (resource instanceof Schedule) {
            schedule = (Schedule) resource;
        } else {
            ProviderResponseLibrary.createException(ctx,resource);
        }

        MethodOutcome method = new MethodOutcome();

        ProviderResponseLibrary.setMethodOutcome(resource,method);

        return method;
    }

    @Search
    public List<Schedule> searchSchedule(HttpServletRequest httpRequest,
                                                           @OptionalParam(name = Schedule.SP_IDENTIFIER) TokenParam identifier,
                                                           @OptionalParam(name = Schedule.SP_NAME) StringParam name,
                                                         //  @OptionalParam(name= Schedule.SP_TYPE) TokenOrListParam codes,
                                                           @OptionalParam(name = Schedule.SP_RES_ID) TokenParam id
                                                        //   @OptionalParam(name = Schedule.SP_ORGANIZATION) ReferenceParam organisation
              ) throws Exception {

        List<Schedule> results = new ArrayList<Schedule>();

        ProducerTemplate template = context.createProducerTemplate();

        InputStream inputStream = (InputStream) template.sendBody("direct:FHIRSchedule",
                ExchangePattern.InOut,httpRequest);

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
                Schedule patient = (Schedule) entry.getResource();
                results.add(patient);
            }
        } else {
            ProviderResponseLibrary.createException(ctx,resource);
        }

        return results;

    }



}
