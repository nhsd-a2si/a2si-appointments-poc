package uk.nhs.careconnect.nosql.dao;

import ca.uhn.fhir.context.FhirContext;
import com.mongodb.DBObject;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.hl7.fhir.dstu3.model.Composition;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Resource;


public interface IFHIRResource {

    DBObject save(FhirContext ctx, Resource resource);

    Resource read(FhirContext ctx, IdType theId);
}
