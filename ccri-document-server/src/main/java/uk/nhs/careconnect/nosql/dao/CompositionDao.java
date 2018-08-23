package uk.nhs.careconnect.nosql.dao;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.DBObject;

import com.mongodb.DBRef;
import org.hl7.fhir.dstu3.model.*;
import uk.nhs.careconnect.nosql.entities.CompositionEntity;
import org.bson.types.ObjectId;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Transactional
@Repository
public class CompositionDao implements IComposition {


    @Autowired
    protected MongoTemplate mongo;

    @Autowired
    IFHIRResource resourceDao;

    private static final Logger log = LoggerFactory.getLogger(CompositionDao.class);

    @Override
    public List<Resource> search(FhirContext ctx, TokenParam resid, ReferenceParam patient) {

        List<Resource> resources = new ArrayList<>();

        Criteria criteria = null;

        if (resid != null) {
            if (criteria == null) {
                criteria = Criteria.where("_id").is(new ObjectId(resid.getValue()));
            } else {
                criteria = criteria.and("_id").is(new ObjectId(resid.getValue()));
            }
        }
        if (patient != null) {

            if (criteria == null) {
                criteria = Criteria.where("idxPatient").is(new DBRef("idxPatient", patient.getValue()));
            } else {
                criteria = criteria.and("idxPatient").is(new DBRef("idxPatient", patient.getValue()));
            }
        }
        if (criteria != null) {
            Query qry = Query.query(criteria);
            log.info("qry = " + qry.toString());
            List<CompositionEntity> results = mongo.find(qry, CompositionEntity.class);
            log.info("Found = "+results.size());


            for (CompositionEntity compositionEntity : results) {
                // Retrieve the Bundle - this is a work around will move to CompositionEntity.
                // The reason for doing this is to build a cleaner composition with references expanded to include display
                Bundle bundle = getFhirDocument(ctx,(ObjectId) compositionEntity.getFhirDocument().getId() );

                for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {

                    if (entry.getResource() instanceof Composition) {
                        // Replace Bundle Composition Id with Composition Entity id
                        resolveCompositionReferences((Composition) entry.getResource(), bundle);
                        entry.getResource().setId(compositionEntity.getId().toString());
                        resources.add(entry.getResource());
                    }
                }
            }
        }

        return resources;
    }

    @Override
    public Composition read(FhirContext ctx, IdType theId) {


        Composition composition = null;

        Query qry = Query.query(Criteria.where("_id").is(new ObjectId(theId.getIdPart())));
        log.info("qry = " + qry.toString());
        CompositionEntity compositionEntity =  mongo.findOne(qry,CompositionEntity.class);
        if (compositionEntity != null) {
            // See note above about replacement with CompositionEntity
            if (compositionEntity.getFhirDocument() !=null) {
                Bundle bundle = getFhirDocument(ctx, (ObjectId) compositionEntity.getFhirDocument().getId());
                for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {

                    if (entry.getResource() instanceof Composition) {
                        // Replace Bundle Composition Id with Composition Entity id
                        resolveCompositionReferences((Composition) entry.getResource(), bundle);
                        entry.getResource().setId(compositionEntity.getId().toString());
                        composition = (Composition) entry.getResource();
                    }
                }
            }
        }


        return composition;

    }

    private Composition resolveCompositionReferences(Composition composition, Bundle bundle) {

        for (Extension extension : composition.getExtension()) {
            if (extension.getValue() instanceof Reference) {
                Reference reference = (Reference) extension.getValue();
                resolveReference(reference,bundle);
            }
        }
        if (composition.hasCustodian()) resolveReference(composition.getCustodian(),bundle);
        if (composition.hasAuthor() && composition.getAuthor().size()>0) resolveReference(composition.getAuthorFirstRep(),bundle);
        if (composition.hasAttester() && composition.getAttester().size()>0) resolveReference(composition.getAttesterFirstRep().getParty(),bundle);

        return composition;
    }

    private Reference resolveReference(Reference reference, Bundle bundle) {
        if (!reference.hasDisplay() || reference.getDisplay().isEmpty()) {
            reference.setDisplay(getDisplayReference(bundle, reference.getReference()));
        }
        return reference;
    }

    private String getDisplayReference(Bundle bundle, String reference) {
        String display = "Some Ref "+reference;
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            IBaseResource resource = null;
            if (entry.hasFullUrl() && entry.getFullUrl().equals(reference)) resource = entry.getResource();
            if (entry.hasResource() && entry.getResource().getId().equals(reference)) resource = entry.getResource();
            if (resource !=null) {
                display = resource.getClass().getCanonicalName();
                if (resource instanceof Practitioner) {
                    Practitioner practitioner = (Practitioner) resource;
                    if (practitioner.getNameFirstRep() != null) display = practitioner.getNameFirstRep().getNameAsSingleString();
                }
                if (resource instanceof Organization) {
                    Organization organization = (Organization) resource;
                    if (organization.hasName()) display = organization.getName();
                }
            }
        }
        return display;
    }

    private ObjectId converToObjectId(String id) {
        return new ObjectId(id);
    }

    private Bundle getFhirDocument(FhirContext ctx, ObjectId objectId) {
        Bundle bundle = null;
        Query qry = Query.query(Criteria.where("_id").is(objectId));
        log.info("qry = " + qry.toString());
        DBObject resourceObj =  mongo.findOne(qry,DBObject.class,"Bundle");
        if (resourceObj != null) {

            JsonParser jsonParser = new JsonParser();
            JsonObject jo = (JsonObject)jsonParser.parse(resourceObj.toString());
            jo.remove("_class");
            jo.remove("_id");
           // log.info("Raw "+jo.toString());
            IBaseResource resource = ctx.newJsonParser().parseResource(jo.toString());

            bundle = (Bundle) resource;
        }
        return bundle;
    }

    @Override
    public Bundle readDocument(FhirContext ctx, IdType theId) {

        Bundle bundle = null;

        Query qry = Query.query(Criteria.where("_id").is( new ObjectId(theId.getIdPart())) );

        log.info(qry.toString());
        CompositionEntity document = mongo.findOne(qry, CompositionEntity.class);

        if (document!=null) {
           if (document.getFhirDocument() !=null) {
               bundle = getFhirDocument(ctx, (ObjectId) document.getFhirDocument().getId());
               for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                   if (entry.getResource().getId() == null || entry.getResource().getId().isEmpty()) {
                       log.info("In resource Id");
                       if (entry.getFullUrl() !=null && !entry.getFullUrl().isEmpty()) {
                           entry.getResource().setId(entry.getFullUrl().replace("urn:uuid:",""));
                       }
                   } else {
                       log.info("Entry id = "+entry.getResource().getId());
                       if (entry.getResource().getId().contains("urn:uuid:")) entry.getResource().setId(entry.getResource().getId().replace("urn:uuid:",""));
                   }
                   if (entry.getResource() instanceof Composition) {
                       // Replace Bundle Composition Id with Composition Entity id
                       resolveCompositionReferences((Composition) entry.getResource(), bundle);

                   }
               }
            }
        }

        return bundle;
    }
}
