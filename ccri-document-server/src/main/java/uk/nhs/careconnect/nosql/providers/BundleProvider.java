package uk.nhs.careconnect.nosql.providers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.nosql.dao.IBundle;


import javax.servlet.http.HttpServletRequest;


@Component
public class BundleProvider implements IResourceProvider {

    @Autowired
    FhirContext ctx;

    @Autowired
    IBundle bundleDao;

    private static final Logger log = LoggerFactory.getLogger(BundleProvider.class);

    @Override
    public Class<Bundle> getResourceType() {
        return Bundle.class;
    }


    @Create
    public MethodOutcome create(HttpServletRequest httpRequest, @ResourceParam Bundle bundle) {

        OperationOutcome opOutcome = bundleDao.create(ctx,bundle, null,null);

        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);

        method.setOperationOutcome(opOutcome);
        method.setId(opOutcome.getIdElement());

        return method;
    }

    @Update
    public MethodOutcome update(HttpServletRequest httpRequest, @ResourceParam Bundle bundle, @IdParam IdType bundleId,@ConditionalUrlParam String conditional) {

        OperationOutcome opOutcome = bundleDao.update(ctx,bundle, bundleId, conditional);

        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);

        method.setOperationOutcome(opOutcome);
        method.setId(opOutcome.getIdElement());

        return method;
    }


}
