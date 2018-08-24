package uk.nhs.careconnect.nosql.dao;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.exceptions.ResourceVersionConflictException;

import com.mongodb.DBObject;
import com.mongodb.DBRef;
import uk.nhs.careconnect.nosql.entities.CompositionEntity;
import org.hl7.fhir.dstu3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.nosql.entities.PatientEntity;

import javax.transaction.Transactional;

@Transactional
@Repository
public class BundleDao implements IBundle {

    @Autowired
    MongoOperations mongo;

    @Autowired
    IFHIRResource fhirDocumentDao;

    @Autowired IPatient patientDao;

    private static final Logger log = LoggerFactory.getLogger(BundleDao.class);

    @Override
    public OperationOutcome update(FhirContext ctx, Bundle bundle, IdType theId, String theConditional) {
        log.debug("BundleDao.save");
        OperationOutcome operationOutcome = new OperationOutcome();



        CompositionEntity compositionEntity = null;
        uk.nhs.careconnect.nosql.entities.Identifier identifierE = new uk.nhs.careconnect.nosql.entities.Identifier();


        if (bundle.hasIdentifier()) {
            identifierE.setValue(bundle.getIdentifier().getValue());
            identifierE.setSystem(bundle.getIdentifier().getSystem());
            Query qry = Query.query(Criteria.where("identifier.system").is(bundle.getIdentifier().getSystem()).and("identifier.value").is(bundle.getIdentifier().getValue()));

            compositionEntity = mongo.findOne(qry, CompositionEntity.class);
            if (compositionEntity==null) {
                compositionEntity = new CompositionEntity();
                compositionEntity.setIdentifier(identifierE);
            } else {
                // Handle updated document, this should be history
            }
        }

        DBObject mObj = fhirDocumentDao.save(ctx,bundle);
        compositionEntity.setFhirDocument(new DBRef("Bundle",mObj.get("_id")));
        compositionEntity.setFhirDocumentlId(mObj.get("_id").toString());

        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            if (entry.hasResource() && entry.getResource() instanceof Patient) {
                // TODO ensure this is the correcct Patient (one referred to in the Composition)
                PatientEntity mpiPatient = patientDao.createEntity(ctx,(Patient) entry.getResource());
                compositionEntity.setIdxPatient(mpiPatient);
            }
        }

        mongo.save(compositionEntity);

        operationOutcome.setId("Composition/"+compositionEntity.getId());


        return operationOutcome;
    }

    @Override
    public OperationOutcome create(FhirContext ctx, Bundle bundle, IdType theId, String theConditional) {

        log.debug("BundleDao.save");
        OperationOutcome operationOutcome = new OperationOutcome();

        CompositionEntity compositionEntity = new CompositionEntity();


       if (bundle.hasIdentifier()) {
           uk.nhs.careconnect.nosql.entities.Identifier identifierE = new uk.nhs.careconnect.nosql.entities.Identifier();
           identifierE.setValue(bundle.getIdentifier().getValue());
           identifierE.setSystem(bundle.getIdentifier().getSystem());
           compositionEntity.setIdentifier(identifierE);

           Query qry = Query.query(Criteria.where("identifier.system").is(bundle.getIdentifier().getSystem()).and("identifier.value").is(bundle.getIdentifier().getValue()));

           CompositionEntity bundleE = mongo.findOne(qry, CompositionEntity.class);
           if (bundleE!=null) throw new ResourceVersionConflictException("FHIR Document already exists. Binary/"+bundleE.getId());
       }

        DBObject mObj = fhirDocumentDao.save(ctx,bundle);
        compositionEntity.setFhirDocument(new DBRef("Bundle",mObj.get("_id")));
        compositionEntity.setFhirDocumentlId(mObj.get("_id").toString());

        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            if (entry.hasResource() && entry.getResource() instanceof Patient) {
                    // TODO ensure this is the correcct Patient (one referred to in the Composition)
                    PatientEntity mpiPatient = patientDao.createEntity(ctx,(Patient) entry.getResource());
                    compositionEntity.setIdxPatient(mpiPatient);


            }
        }

        mongo.save(compositionEntity);

        operationOutcome.setId("Composition/"+compositionEntity.getId());


        return operationOutcome;
    }
}
