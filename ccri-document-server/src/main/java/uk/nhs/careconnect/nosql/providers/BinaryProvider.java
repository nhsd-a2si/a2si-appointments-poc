package uk.nhs.careconnect.nosql.providers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.nosql.dao.CompositionDao;
import uk.nhs.careconnect.nosql.dao.IBinaryResource;
import uk.nhs.careconnect.nosql.dao.IComposition;
import uk.nhs.careconnect.nosql.dao.IFHIRResource;

import javax.servlet.http.HttpServletRequest;

@Component
public class BinaryProvider implements IResourceProvider {
    @Autowired
    FhirContext ctx;

    @Autowired
    IBinaryResource binaryResource;

    @Autowired
    IComposition compositionDao;

    public Class<? extends IBaseResource> getResourceType() {
        return Binary.class;
    }

    private static final Logger log = LoggerFactory.getLogger(BinaryProvider.class);

    @Read
    public Binary getBinaryById(HttpServletRequest request, @IdParam IdType internalId) {
        Binary binary =null;
        // Assume this is a file

            binary = binaryResource.read(ctx, internalId);

            if (binary == null) {
                log.info("Binary was null");
                // if no file return check it is not a composition

                Bundle bundle = compositionDao.readDocument(ctx, internalId);
                if (bundle != null) {
                    binary = new Binary();
                    String resource = ctx.newXmlParser().encodeResourceToString(bundle);
                    log.info("Resource returned from composition.readDocument as " + resource);
                    binary.setId(internalId.getIdPart());
                    binary.setContentType("application/fhir+xml");
                    binary.setContent(resource.getBytes());
                }
            } else {
                String resource = ctx.newXmlParser().encodeResourceToString(binary);
                log.debug("Resource returned from binary.read as " + resource);
            }


        return binary;
    }

    @Create
    public MethodOutcome create(HttpServletRequest httpRequest, @ResourceParam Binary binary) {

        OperationOutcome operationOutcome = new OperationOutcome();
        operationOutcome.setId("Binary/"+binaryResource.save(ctx,binary));

        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);

        method.setOperationOutcome(operationOutcome);
        method.setId(operationOutcome.getIdElement());

        return method;
    }
}
