package uk.nhs.careconnect.nosql.entities;


import com.mongodb.BasicDBObject;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;


import java.util.Collection;
import java.util.LinkedHashSet;


@Document(collection = "idxComposition")
public class CompositionEntity {

    @Id
    private ObjectId id;
    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }


    private Collection<Coding> type = new LinkedHashSet<>();

    private Collection<Coding> _class = new LinkedHashSet<>();

    private Reference subject;

    private Collection<Reference> author = new LinkedHashSet<>();

    private String title;

    private Collection<Reference> attester = new LinkedHashSet<>();

    private Reference custodian;

    @DBRef
    private PatientEntity idxPatient;


    com.mongodb.DBRef fhirDocument;

    String fhirDocumentlId;

    private Identifier identifier;

    String originalId;

    public com.mongodb.DBRef getFhirDocument() {
        return fhirDocument;
    }

    public void setFhirDocument(com.mongodb.DBRef fhirDocument) {
        this.fhirDocument = fhirDocument;
    }




    public String getFhirDocumentlId() {
        return fhirDocumentlId;
    }

    public void setFhirDocumentlId(String fhirDocumentlId) {
        this.fhirDocumentlId = fhirDocumentlId;
    }



    public PatientEntity getIdxPatient() {
        return idxPatient;
    }

    public void setIdxPatient(PatientEntity idxPatient) {
        this.idxPatient = idxPatient;
    }



    public String getOriginalId() {
        return originalId;
    }

    public void setOriginalId(String originalId) {
        this.originalId = originalId;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public void setIdentifier(Identifier identifier) {
        this.identifier = identifier;
    }


    public Reference getSubject() {
        return subject;
    }

    public void setSubject(Reference subject) {
        this.subject = subject;
    }

    public Collection<Reference> getAuthor() {
        return author;
    }

    public void setAuthor(Collection<Reference> author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Collection<Reference> getAttester() {
        return attester;
    }

    public void setAttester(Collection<Reference> attester) {
        this.attester = attester;
    }

    public Reference getCustodian() {
        return custodian;
    }

    public void setCustodian(Reference custodian) {
        this.custodian = custodian;
    }

    public Collection<Coding> getType() {
        return type;
    }

    public void setType(Collection<Coding> type) {
        this.type = type;
    }

    public Collection<Coding> get_class() {
        return _class;
    }

    public void set_class(Collection<Coding> _class) {
        this._class = _class;
    }
}