package uk.nhs.careconnect.nosql.dao;

import ca.uhn.fhir.context.FhirContext;
import com.mongodb.DBObject;
import org.bson.types.ObjectId;
import org.hl7.fhir.dstu3.model.Binary;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Resource;


public interface IBinaryResource {

    ObjectId save(FhirContext ctx, Binary binary);

    Binary read(FhirContext ctx, IdType theId);
}
