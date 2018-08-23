package uk.nhs.careconnect.nosql.dao;

import ca.uhn.fhir.context.FhirContext;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.bson.Document;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Transactional
@Repository
public class FHIRResourceDao implements IFHIRResource {

    @Autowired
    protected MongoTemplate mongoTemplate;



    private static final Logger log = LoggerFactory.getLogger(FHIRResourceDao.class);


    @Override
    public DBObject save(FhirContext ctx, Resource resource) {

        Document doc = Document.parse(ctx.newJsonParser().encodeResourceToString(resource));
        // Convert to BasicDBObject to get object id
        DBObject mObj = new BasicDBObject(doc);
        mongoTemplate.insert(mObj, resource.getResourceType().name());
    // (ObjectId) mObj.get("_id")
        return mObj;
    }

    @Override
    public Resource read(FhirContext ctx, IdType theId) {
        return null;
    }
}
