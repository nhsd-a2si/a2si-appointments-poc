package uk.nhs.careconnect.nosql.dao;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSFile;
import com.mongodb.gridfs.GridFSInputFile;
import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.hl7.fhir.dstu3.model.Binary;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.nosql.entities.CompositionEntity;

import javax.transaction.Transactional;
import java.io.FileInputStream;
import java.io.InputStream;

@Transactional
@Repository
public class BinaryResourceDao implements IBinaryResource {

    @Autowired
    protected MongoTemplate mongoTemplate;


    private static final Logger log = LoggerFactory.getLogger(BinaryResourceDao.class);


    @Override
    public ObjectId save(FhirContext ctx, Binary binary) {

        GridFS gridFS = new GridFS(mongoTemplate.getDb());


        GridFSInputFile gridFSInputFile = gridFS.createFile(binary.getContent());
        gridFSInputFile.setContentType(binary.getContentType());
        gridFSInputFile.save();

        return (ObjectId) gridFSInputFile.get("_id");
    }

    @Override
    public Binary read(FhirContext ctx, IdType theId) {

        GridFS gridFS = new GridFS(mongoTemplate.getDb());
        GridFSDBFile gridFSDBFile = null;
        try {
            gridFSDBFile = gridFS.find(new ObjectId(theId.getIdPart()));
        } catch (Exception ex) {
            throw new ResourceNotFoundException("Document " + theId.getIdPart() + " Not found");
        }

        Binary binary = null;
        if (gridFSDBFile != null) {
            try {
                binary = new Binary();
                binary.setContentType(gridFSDBFile.getContentType());
                binary.setContent(IOUtils.toByteArray(gridFSDBFile.getInputStream()));
            } catch(
            Exception ex) {
                log.info(ex.getMessage());
                throw new InternalErrorException(ex.getMessage());
            }
        }
        return binary;
    }
}
