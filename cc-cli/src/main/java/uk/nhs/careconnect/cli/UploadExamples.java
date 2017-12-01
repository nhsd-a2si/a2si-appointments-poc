package uk.nhs.careconnect.cli;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.QuoteMode;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import uk.org.hl7.fhir.core.Stu3.CareConnectExtension;
import uk.org.hl7.fhir.core.Stu3.CareConnectProfile;
import uk.org.hl7.fhir.core.Stu3.CareConnectSystem;

import java.io.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class   UploadExamples extends BaseCommand {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(UploadExamples.class);

	private ArrayList<IIdType> myExcludes = new ArrayList<>();

    private ArrayList<IBaseResource> resources = new ArrayList<>();

    private Map<String,String> orgMap = new HashMap<>();

    private Map<String,String> docMap = new HashMap<>();

    private static String nokiaObs = "https://fhir.health.phr.example.com/Id/observation";

    FhirContext ctx ;

    IGenericClient client;

/* PROGRAM ARGUMENTS

upload-examples
-t
http://127.0.0.1:8080/careconnect-ri/STU3

    */

	@Override
	public String getCommandDescription() {
		return "Uploads sample resources.";
	}

	@Override
	public String getCommandName() {
		return "upload-examples";
	}

	public String stripChar(String string) {
	    string =string.replace(" ","");
        string =string.replace(":","");
        string =string.replace("-","");
        return string;
    }

	@Override
	public Options getOptions() {
		Options options = new Options();
		Option opt;

		addFhirVersionOption(options);

		opt = new Option("t", "target", true, "Base URL for the target server (e.g. \"http://example.com/fhir\")");
		opt.setRequired(true);
		options.addOption(opt);

        opt = new Option("a", "all", false, "All upload files");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("g", "gp", false, "GP upload files");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("phr", "phr", false, "PHR Examples upload files");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("pat", "patient", false, "Patient Examples upload files");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("o", "obs", false, "Observation Examples upload files");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("e", "encounter", false, "Encounter Examples upload files");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("c", "condition", false, "Condition Examples upload files");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("allergy", "allergyintolerance", false, "AllergyIntolerance Examples upload files");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("imms", "immunisations", false, "Immunization Examples upload files");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("proc", "procedures", false, "Procedure Examples upload files");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("pres", "prescriptions", false, "MedciationRequest Examples upload files");
        opt.setRequired(false);
        options.addOption(opt);

        return options;
	}

	@Override
	public void run(CommandLine theCommandLine) throws ParseException {
		String targetServer = theCommandLine.getOptionValue("t");
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		if (isBlank(targetServer)) {
			throw new ParseException("No target server (-t) specified");
		} else if (targetServer.startsWith("http") == false) {
			throw new ParseException("Invalid target server specified, must begin with 'http'");
		}

		ctx = getSpecVersionContext(theCommandLine);

        ClassLoader classLoader = getClass().getClassLoader();

		if (ctx.getVersion().getVersion() == FhirVersionEnum.DSTU3) {

            client = ctx.newRestfulGenericClient(targetServer);

            // BA Patient data file
            if (theCommandLine.hasOption("a") ||theCommandLine.hasOption("pat")) {
                try {

                    System.out.println("Patient.csv");

                    IRecordHandler handler = null;

                    handler = new PatientHandler(ctx, client);
                    processPatientCSV(handler, ctx, ',', QuoteMode.NON_NUMERIC, classLoader.getResourceAsStream("Examples/Patient.csv"));
                    for (IBaseResource resource : resources) {
                        Patient patient = (Patient) resource;
                        Identifier identifier = null;
                        for (Identifier ident : patient.getIdentifier()) {
                            if (ident.getSystem().contains("PPMIdentifier")) identifier = ident;
                        }
                        if (identifier != null) {
                            MethodOutcome outcome = client.update().resource(resource)
                                    .conditionalByUrl("Patient?identifier=" + identifier.getSystem() + "%7C" + identifier.getValue())
                                    .execute();

                            if (outcome.getId() != null) {
                                patient.setId(outcome.getId().getIdPart());
                            }
                        }
                    }
                    resources.clear();


                } catch (Exception ex) {
                    ourLog.error(ex.getMessage());
                }
            }

            if (theCommandLine.hasOption("a") ||theCommandLine.hasOption("o")) {
                try {

                    //File file = new File(classLoader.getResourceAsStream ("Examples/Obs.csv").getFile());

                    System.out.println("Obs.csv");

                    IRecordHandler handler = null;

                    handler = new ObsHandler();
                    processObsCSV(handler, ctx, ',', QuoteMode.NON_NUMERIC, classLoader.getResourceAsStream("Examples/Obs.csv"));
                    for (IBaseResource resource : resources) {
                        Observation observation = (Observation) resource;
                        MethodOutcome outcome = client.update().resource(resource)
                                .conditionalByUrl("Observation?identifier=" + observation.getIdentifier().get(0).getSystem() + "%7C" +observation.getIdentifier().get(0).getValue())
                                .execute();

                        if (outcome.getId() != null ) {
                            observation.setId(outcome.getId().getIdPart());
                        }
                    }
                    resources.clear();


                } catch (Exception ex) {
                    ourLog.error(ex.getMessage());
                }
            }

            if (theCommandLine.hasOption("a") ||theCommandLine.hasOption("e")) {
                try {
                    System.out.println("Encounter.csv");

                    IRecordHandler handler = null;

                    handler = new EncounterHandler();
                    processEncounterCSV(handler, ctx, ',', QuoteMode.NON_NUMERIC, classLoader.getResourceAsStream("Examples/Encounter.csv"));
                    for (IBaseResource resource : resources) {
                        Encounter encounter = (Encounter) resource;
                        MethodOutcome outcome = client.update().resource(resource)
                                .conditionalByUrl("Encounter?identifier=" + encounter.getIdentifier().get(0).getSystem() + "%7C" +encounter.getIdentifier().get(0).getValue())
                                .execute();

                        if (outcome.getId() != null ) {
                            encounter.setId(outcome.getId().getIdPart());
                        }
                    }
                    resources.clear();


                } catch (Exception ex) {
                    ourLog.error(ex.getMessage());
                }
            }

            if (theCommandLine.hasOption("a") ||theCommandLine.hasOption("c")) {
                try {
                    System.out.println("Condition.csv");

                    IRecordHandler handler = null;

                    handler = new ConditionHandler();
                    processConditionCSV(handler, ctx, ',', QuoteMode.NON_NUMERIC, classLoader.getResourceAsStream("Examples/Condition.csv"));
                    for (IBaseResource resource : resources) {
                        Condition condition = (Condition) resource;
                        MethodOutcome outcome = client.update().resource(resource)
                                .conditionalByUrl("Condition?identifier=" + condition.getIdentifier().get(0).getSystem() + "%7C" +condition.getIdentifier().get(0).getValue())
                                .execute();

                        if (outcome.getId() != null ) {
                            condition.setId(outcome.getId().getIdPart());
                        }
                    }
                    resources.clear();


                } catch (Exception ex) {
                    ourLog.error(ex.getMessage());
                }
            }
            if (theCommandLine.hasOption("a") ||theCommandLine.hasOption("allergy")) {
                try {
                    System.out.println("AllergyIntolerance.csv");

                    IRecordHandler handler = null;

                    handler = new AllergyHandler();
                    processAllergyCSV(handler, ctx, ',', QuoteMode.NON_NUMERIC, classLoader.getResourceAsStream("Examples/AllergyIntolerance.csv"));

                    handler = new AllergyReactionsHandler();
                    processAllergyReactionsCSV(handler, ctx, ',', QuoteMode.NON_NUMERIC, classLoader.getResourceAsStream("Examples/AllergyIntolerance.reactions.csv"));
                    for (IBaseResource resource : resources) {
                        AllergyIntolerance allergy = (AllergyIntolerance) resource;
                        MethodOutcome outcome = client.update().resource(resource)
                                .conditionalByUrl("AllergyIntolerance?identifier=" + allergy.getIdentifier().get(0).getSystem() + "%7C" +allergy.getIdentifier().get(0).getValue())
                                .execute();

                        if (outcome.getId() != null ) {
                            allergy.setId(outcome.getId().getIdPart());
                        }
                    }
                    resources.clear();


                } catch (Exception ex) {
                    ourLog.error(ex.getMessage());
                }
            }
            if (theCommandLine.hasOption("a") ||theCommandLine.hasOption("imms")) {
                try {
                    System.out.println("Immunisation.csv");

                    IRecordHandler handler = null;

                    handler = new ImmunisationHandler();
                    processImmunisationCSV(handler, ctx, ',', QuoteMode.NON_NUMERIC, classLoader.getResourceAsStream("Examples/Immunisation.csv"));

                    for (IBaseResource resource : resources) {
                        Immunization immunization = (Immunization) resource;
                        MethodOutcome outcome = client.update().resource(resource)
                                .conditionalByUrl("Immunization?identifier=" + immunization.getIdentifier().get(0).getSystem() + "%7C" +immunization.getIdentifier().get(0).getValue())
                                .execute();

                        if (outcome.getId() != null ) {
                            immunization.setId(outcome.getId().getIdPart());
                        }
                    }
                    resources.clear();


                } catch (Exception ex) {
                    ourLog.error(ex.getMessage());
                }
            }
            if (theCommandLine.hasOption("proc") ||theCommandLine.hasOption("a")) {
                try {
                    System.out.println("Procedure.csv");

                    IRecordHandler handler = null;

                    handler = new ProcedureHandler();
                    processProcedureCSV(handler, ctx, ',', QuoteMode.NON_NUMERIC, classLoader.getResourceAsStream("Examples/Procedure.csv"));

                    for (IBaseResource resource : resources) {
                        Procedure procedure = (Procedure) resource;
                        MethodOutcome outcome = client.update().resource(resource)
                                .conditionalByUrl("Procedure?identifier=" + procedure.getIdentifier().get(0).getSystem() + "%7C" +procedure.getIdentifier().get(0).getValue())
                                .execute();

                        if (outcome.getId() != null ) {
                            procedure.setId(outcome.getId().getIdPart());
                        }
                    }
                    resources.clear();


                } catch (Exception ex) {
                    ourLog.error(ex.getMessage());
                }
            }

            if (theCommandLine.hasOption("pres") ||theCommandLine.hasOption("a")) {
                try {
                    System.out.println("MedicationRequest.csv");

                    IRecordHandler handler = null;

                    handler = new PrescriptionHandler();

                    processPrescriptionCSV(handler, ctx, ',', QuoteMode.NON_NUMERIC, classLoader.getResourceAsStream("Examples/MedicationRequest.csv"));

                    for (IBaseResource resource : resources) {

                        MedicationRequest prescription = (MedicationRequest) resource;
                        MethodOutcome outcome = client.update().resource(resource)
                                .conditionalByUrl("MedicationRequest?identifier=" + prescription.getIdentifier().get(0).getSystem() + "%7C" +prescription.getIdentifier().get(0).getValue())
                                .execute();

                        if (outcome.getId() != null ) {
                            prescription.setId(outcome.getId().getIdPart());
                        }
                    }
                    resources.clear();


                } catch (Exception ex) {
                    ourLog.error(ex.getMessage());
                }
            }
            /* Developer loads - remove for now
            try {
                File file = new File(classLoader.getResource("Examples/observations").getFile());

                for (File fileD : file.listFiles()) {
                    System.out.println(fileD.getName());
                    String contents = IOUtils.toString(new InputStreamReader(new FileInputStream(fileD), "UTF-8"));
                    IBaseResource localProfileResource = ca.uhn.fhir.rest.api.EncodingEnum.detectEncodingNoDefault(contents).newParser(ctx).parseResource(contents);
                    client.create().resource(localProfileResource).execute();
                }
            } catch (Exception ex) {
                ourLog.error(ex.getMessage());

            }
            */

            // Nokia
            if (theCommandLine.hasOption("a") ||theCommandLine.hasOption("phr")) {

                try {

                    // File file = new File(classLoader.getResource("Examples/nokia/weight.csv").getFile());

                    System.out.println("activities.csv");
                    IRecordHandler handler = null;

                    handler = new ActivitiesHandler();
                    processActivitiesCSV(handler, ctx, ',', QuoteMode.NON_NUMERIC, classLoader.getResourceAsStream("Examples/nokia/activities.csv"));
                    for (IBaseResource resource : resources) {
                        Observation observation = (Observation) resource;
                        MethodOutcome outcome = client.update().resource(resource)
                                .conditionalByUrl("Observation?identifier=" + observation.getIdentifier().get(0).getSystem() + "%7C" +observation.getIdentifier().get(0).getValue())
                                .execute();

                        if (outcome.getId() != null ) {
                            observation.setId(outcome.getId().getIdPart());
                        }
                    }
                    resources.clear();

                    System.out.println("weight.csv");


                    handler = new WeightHandler();
                    processWeightCSV(handler, ctx, ',', QuoteMode.NON_NUMERIC, classLoader.getResourceAsStream("Examples/nokia/weight.csv"));
                    for (IBaseResource resource : resources) {
                        Observation observation = (Observation) resource;
                        MethodOutcome outcome = client.update().resource(resource)
                                .conditionalByUrl("Observation?identifier=" + observation.getIdentifier().get(0).getSystem() + "%7C" +observation.getIdentifier().get(0).getValue())
                                .execute();

                        if (outcome.getId() != null ) {
                            observation.setId(outcome.getId().getIdPart());
                        }
                    }
                    resources.clear();

                    // file = new File(classLoader.getResource("Examples/nokia/blood_pressure.csv").getFile());

                    System.out.println("blood_pressure.csv");
                    handler = null;

                    handler = new BloodPressureHandler();
                    processBloodPressureCSV(handler, ctx, ',', QuoteMode.NON_NUMERIC, classLoader.getResourceAsStream("Examples/nokia/blood_pressure.csv"));
                    for (IBaseResource resource : resources) {
                        Observation observation = (Observation) resource;
                        MethodOutcome outcome = client.update().resource(resource)
                                .conditionalByUrl("Observation?identifier=" + observation.getIdentifier().get(0).getSystem() + "%7C" +observation.getIdentifier().get(0).getValue())
                                .execute();

                        if (outcome.getId() != null ) {
                            observation.setId(outcome.getId().getIdPart());
                        }
                    }
                    resources.clear();


                } catch (Exception ex) {
                    ourLog.error(ex.getMessage());
                }
            }

        }

	}



    private List<String> getResourceFiles(String path ) throws IOException {
        List<String> filenames = new ArrayList<>();

        try(
                InputStream in = getResourceAsStream( path );
                BufferedReader br = new BufferedReader( new InputStreamReader( in ) ) ) {
            String resource;

            while( (resource = br.readLine()) != null ) {
                filenames.add( resource );
            }
        }

        return filenames;
    }

    private InputStream getResourceAsStream(String resource ) {
        final InputStream in
                = getContextClassLoader().getResourceAsStream( resource );

        return in == null ? getClass().getResourceAsStream( resource ) : in;
    }

    private ClassLoader getContextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    private void processPatientCSV(IRecordHandler handler, FhirContext ctx, char theDelimiter, QuoteMode theQuoteMode, InputStream file) throws CommandFailureException {

        Boolean found = false;
        try {

            //  ourLog.info("Processing file {}", file.getName());
            found = true;

            Reader reader = null;
            CSVParser parsed = null;
            try {
                reader = new InputStreamReader(file);
                CSVFormat format = CSVFormat
                        .newFormat(theDelimiter)
                        .withAllowMissingColumnNames()
                        .withSkipHeaderRecord(true)
                        .withHeader("PATIENT_ID"
                                ,"RES_DELETED"
                                ,"RES_CREATED"
                                ,"RES_MESSAGE_REF"
                                ,"RES_UPDATED"
                                ,"active"
                                ,"date_of_birth"
                                ,"gender"
                                ,"registration_end"
                                ,"registration_start"
                                ,"NHSverification"
                                ,"ethnic"
                                ,"GP_ID"
                                ,"marital"
                                ,"PRACTICE_ID"
                        );
                if (theQuoteMode != null) {
                    format = format.withQuote('"').withQuoteMode(theQuoteMode);
                }
                parsed = new CSVParser(reader, format);
                Iterator<CSVRecord> iter = parsed.iterator();
                ourLog.debug("Header map: {}", parsed.getHeaderMap());

                int count = 0;

                int nextLoggedCount = 0;
                while (iter.hasNext()) {
                    CSVRecord nextRecord = iter.next();
                    handler.accept(nextRecord);
                    count++;
                    if (count >= nextLoggedCount) {
                        ourLog.info(" * Processed {} records", count);
                    }
                }

            } catch (IOException e) {
                throw new InternalErrorException(e);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void processObsCSV(IRecordHandler handler, FhirContext ctx, char theDelimiter, QuoteMode theQuoteMode, InputStream file) throws CommandFailureException {

        Boolean found = false;
        try {

          //  ourLog.info("Processing file {}", file.getName());
            found = true;

            Reader reader = null;
            CSVParser parsed = null;
            try {
                reader = new InputStreamReader(file);
                CSVFormat format = CSVFormat
                        .newFormat(theDelimiter)
                        .withAllowMissingColumnNames()
                        .withSkipHeaderRecord(true)
                        .withHeader("ObservationID"
                                ,"PatientID"
                                ,"EffectiveDateTime"
                                ,"Code"
                                ,"CodeDisplay"
                                ,"Method"
                                ,"MethodDisplay"
                                ,"BodySite"
                                ,"BodySiteDescription"
                                ,"ValueQuantity"
                                ,"ValueCode"
                                ,"ValueString"
                                ,"ValueUnitOfMeasure"
                                ,"LowRange"
                                ,"HighRange"
                                ,"Interpretation"
                                ,"Age"
                                ,"FHIRCategory"
                                ,"PerformerType"
                                ,"PerformerIdentifier"
                        );
                if (theQuoteMode != null) {
                    format = format.withQuote('"').withQuoteMode(theQuoteMode);
                }
                parsed = new CSVParser(reader, format);
                Iterator<CSVRecord> iter = parsed.iterator();
                ourLog.debug("Header map: {}", parsed.getHeaderMap());

                int count = 0;

                int nextLoggedCount = 0;
                while (iter.hasNext()) {
                    CSVRecord nextRecord = iter.next();
                    handler.accept(nextRecord);
                    count++;
                    if (count >= nextLoggedCount) {
                        ourLog.info(" * Processed {} records", count);
                    }
                }

            } catch (IOException e) {
                throw new InternalErrorException(e);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void processEncounterCSV(IRecordHandler handler, FhirContext ctx, char theDelimiter, QuoteMode theQuoteMode, InputStream file) throws CommandFailureException {

        Boolean found = false;
        try {

            //  ourLog.info("Processing file {}", file.getName());
            found = true;

            Reader reader = null;
            CSVParser parsed = null;
            try {
                reader = new InputStreamReader(file);
                CSVFormat format = CSVFormat
                        .newFormat(theDelimiter)
                        .withAllowMissingColumnNames()
                        .withSkipHeaderRecord(true)
                        .withHeader("encounterID"
                                ,"period.startDate"
                                ,"period.startTime"
                                ,"period.endDate"
                                ,"period.endTime"
                                ,"patientID"
                                ,"participant.type"
                                ,"resource.type"
                                ,"participent.individual"
                                ,"serviceProvider"
                        );
                if (theQuoteMode != null) {
                    format = format.withQuote('"').withQuoteMode(theQuoteMode);
                }
                parsed = new CSVParser(reader, format);
                Iterator<CSVRecord> iter = parsed.iterator();
                ourLog.debug("Header map: {}", parsed.getHeaderMap());

                int count = 0;

                int nextLoggedCount = 0;
                while (iter.hasNext()) {
                    CSVRecord nextRecord = iter.next();
                    handler.accept(nextRecord);
                    count++;
                    if (count >= nextLoggedCount) {
                        ourLog.info(" * Processed {} records", count);
                    }
                }

            } catch (IOException e) {
                throw new InternalErrorException(e);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }



    private void processProcedureCSV(IRecordHandler handler, FhirContext ctx, char theDelimiter, QuoteMode theQuoteMode, InputStream file) throws CommandFailureException {

        Boolean found = false;
        try {

            found = true;

            Reader reader = null;
            CSVParser parsed = null;
            try {
                reader = new InputStreamReader(file);
                CSVFormat format = CSVFormat
                        .newFormat(theDelimiter)
                        .withAllowMissingColumnNames()
                        .withSkipHeaderRecord(true)
                        .withHeader("identifier"
                                ,"subject"
                                ,"status"
                                ,"code.coding.code"
                                ,"code.coding.display"
                                ,"performed date"
                                ,"performed time"
                                ,"category.code.coding.code"
                                ,"category.code.coding.display"
                                ,"notPerformed"
                                ,"Performer Type"
                                ,"performer"
                                ,"encounter"
                                ,"outcome.coding.code"
                                ,"ouctome.coding.display"
                        );
                if (theQuoteMode != null) {
                    format = format.withQuote('"').withQuoteMode(theQuoteMode);
                }
                parsed = new CSVParser(reader, format);
                Iterator<CSVRecord> iter = parsed.iterator();
                ourLog.debug("Header map: {}", parsed.getHeaderMap());

                int count = 0;

                int nextLoggedCount = 0;
                while (iter.hasNext()) {
                    CSVRecord nextRecord = iter.next();
                    handler.accept(nextRecord);
                    count++;
                    if (count >= nextLoggedCount) {
                        ourLog.info(" * Processed {} records", count);
                    }
                }

            } catch (IOException e) {
                throw new InternalErrorException(e);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void processImmunisationCSV(IRecordHandler handler, FhirContext ctx, char theDelimiter, QuoteMode theQuoteMode, InputStream file) throws CommandFailureException {

        Boolean found = false;
        try {

            found = true;

            Reader reader = null;
            CSVParser parsed = null;
            try {
                reader = new InputStreamReader(file);
                CSVFormat format = CSVFormat
                        .newFormat(theDelimiter)
                        .withAllowMissingColumnNames()
                        .withSkipHeaderRecord(true)
                        .withHeader("identifier"
                                ,"patientID"
                                ,"parentPresent"
                                ,"dateRecorded"
                                ,"status"
                                ,"dateAdministered"
                                ,"timeAdministered"
                                ,"vaccineCode.coding"
                                ,"notGiven"
                                ,"encounter"
                                ,"primarySource"
                                ,"reportOrigin"
                                ,"location"
                                ,"lotNumber"
                                ,"expirationDate"
                                ,"vaccineCode.coding.display"
                        );
                if (theQuoteMode != null) {
                    format = format.withQuote('"').withQuoteMode(theQuoteMode);
                }
                parsed = new CSVParser(reader, format);
                Iterator<CSVRecord> iter = parsed.iterator();
                ourLog.debug("Header map: {}", parsed.getHeaderMap());

                int count = 0;

                int nextLoggedCount = 0;
                while (iter.hasNext()) {
                    CSVRecord nextRecord = iter.next();
                    handler.accept(nextRecord);
                    count++;
                    if (count >= nextLoggedCount) {
                        ourLog.info(" * Processed {} records", count);
                    }
                }

            } catch (IOException e) {
                throw new InternalErrorException(e);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void processAllergyCSV(IRecordHandler handler, FhirContext ctx, char theDelimiter, QuoteMode theQuoteMode, InputStream file) throws CommandFailureException {

        Boolean found = false;
        try {

            found = true;

            Reader reader = null;
            CSVParser parsed = null;
            try {
                reader = new InputStreamReader(file);
                CSVFormat format = CSVFormat
                        .newFormat(theDelimiter)
                        .withAllowMissingColumnNames()
                        .withSkipHeaderRecord(true)
                        .withHeader("identifier"
                                ,"encounter"
                                ,"patient"
                                ,"substance.coding.code"
                                ,"substance.coding.display"
                                ,"clinicalStatus"
                                ,"verificationStatus"
                                ,"onset.DateTime"
                                ,"assertedDate"
                                ,"recorder"
                                ,"lastOccurrence"

                        );
                if (theQuoteMode != null) {
                    format = format.withQuote('"').withQuoteMode(theQuoteMode);
                }
                parsed = new CSVParser(reader, format);
                Iterator<CSVRecord> iter = parsed.iterator();
                ourLog.debug("Header map: {}", parsed.getHeaderMap());

                int count = 0;

                int nextLoggedCount = 0;
                while (iter.hasNext()) {
                    CSVRecord nextRecord = iter.next();
                    handler.accept(nextRecord);
                    count++;
                    if (count >= nextLoggedCount) {
                        ourLog.info(" * Processed {} records", count);
                    }
                }

            } catch (IOException e) {
                throw new InternalErrorException(e);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void processAllergyReactionsCSV(IRecordHandler handler, FhirContext ctx, char theDelimiter, QuoteMode theQuoteMode, InputStream file) throws CommandFailureException {

        Boolean found = false;
        try {

            found = true;

            Reader reader = null;
            CSVParser parsed = null;
            try {
                reader = new InputStreamReader(file);
                CSVFormat format = CSVFormat
                        .newFormat(theDelimiter)
                        .withAllowMissingColumnNames()
                        .withSkipHeaderRecord(true)
                        .withHeader("identifier"
                                ,"allergy.identifier"
                                ,"reaction.manifestation"
                                ,"reaction.manifestation.display"
                        );
                if (theQuoteMode != null) {
                    format = format.withQuote('"').withQuoteMode(theQuoteMode);
                }
                parsed = new CSVParser(reader, format);
                Iterator<CSVRecord> iter = parsed.iterator();
                ourLog.debug("Header map: {}", parsed.getHeaderMap());

                int count = 0;

                int nextLoggedCount = 0;
                while (iter.hasNext()) {
                    CSVRecord nextRecord = iter.next();
                    handler.accept(nextRecord);
                    count++;
                    if (count >= nextLoggedCount) {
                        ourLog.info(" * Processed {} records", count);
                    }
                }

            } catch (IOException e) {
                throw new InternalErrorException(e);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void processConditionCSV(IRecordHandler handler, FhirContext ctx, char theDelimiter, QuoteMode theQuoteMode, InputStream file) throws CommandFailureException {

        Boolean found = false;
        try {

            found = true;

            Reader reader = null;
            CSVParser parsed = null;
            try {
                reader = new InputStreamReader(file);
                CSVFormat format = CSVFormat
                        .newFormat(theDelimiter)
                        .withAllowMissingColumnNames()
                        .withSkipHeaderRecord(true)
                        .withHeader("identifier"
                                ,"patient ID"
                                ,"encounter"
                                ,"asserter"
                                ,"dateRecorded"
                                ,"code.coding.code"
                                ,"code.coding.display"
                                ,"category.code"
                                ,"category.description"
                                ,"clinicalStatus"
                                ,"verificationStatus"
                                ,"severity.code.coding.code"
                                ,"serverity.code.coding.description"
                        );
                if (theQuoteMode != null) {
                    format = format.withQuote('"').withQuoteMode(theQuoteMode);
                }
                parsed = new CSVParser(reader, format);
                Iterator<CSVRecord> iter = parsed.iterator();
                ourLog.debug("Header map: {}", parsed.getHeaderMap());

                int count = 0;

                int nextLoggedCount = 0;
                while (iter.hasNext()) {
                    CSVRecord nextRecord = iter.next();
                    handler.accept(nextRecord);
                    count++;
                    if (count >= nextLoggedCount) {
                        ourLog.info(" * Processed {} records", count);
                    }
                }

            } catch (IOException e) {
                throw new InternalErrorException(e);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
    private void processWeightCSV(IRecordHandler handler, FhirContext ctx, char theDelimiter, QuoteMode theQuoteMode, InputStream file) throws CommandFailureException {


        Boolean found = false;
        try {


            found = true;

            Reader reader = null;
            CSVParser parsed = null;
            try {
                reader = new InputStreamReader(file);
                CSVFormat format = CSVFormat
                        .newFormat(theDelimiter)
                        .withAllowMissingColumnNames()
                        .withSkipHeaderRecord(true)
                        .withHeader("Date"
                                ,"Weight"
                                ,"FatMass"
                                ,"BoneMass"
                                ,"MuscleMass"
                                ,"Hydration"
                                ,"Comments"
                        );
                if (theQuoteMode != null) {
                    format = format.withQuote('"').withQuoteMode(theQuoteMode);
                }
                parsed = new CSVParser(reader, format);
                Iterator<CSVRecord> iter = parsed.iterator();
                ourLog.debug("Header map: {}", parsed.getHeaderMap());

                int count = 0;

                int nextLoggedCount = 0;
                while (iter.hasNext()) {
                    CSVRecord nextRecord = iter.next();
                    handler.accept(nextRecord);
                    count++;
                    if (count >= nextLoggedCount) {
                        ourLog.info(" * Processed {} records", count);
                    }
                }

            } catch (IOException e) {
                throw new InternalErrorException(e);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void processPrescriptionCSV(IRecordHandler handler, FhirContext ctx, char theDelimiter, QuoteMode theQuoteMode, InputStream file) throws CommandFailureException {


        Boolean found = false;
        try {


            found = true;

            Reader reader = null;
            CSVParser parsed = null;
            try {
                reader = new InputStreamReader(file);
                CSVFormat format = CSVFormat
                        .newFormat(theDelimiter)
                        .withAllowMissingColumnNames()
                        .withSkipHeaderRecord(true)
                        .withHeader("identifier"
                                ,"subject (patientID)"
                                ,"status"
                                ,"intent"
                                ,"priority"
                                ,"medication"
                                ,"context"
                                ,"authoredOn Date"
                                ,"authoredOn Time"
                                ,"requester.agent type"
                                ,"requester.agent"
                                ,"reasonCode Type"
                                ,"reasonCode"
                                ,"dosage.text"
                                ,"dosage.additionalInstruction"
                                ,"dosage.timing"
                                ,"dosage.asNeeded.asNeededBoolean"
                                ,"dosage.asNeeded.asNeededCodableConcept"
                                ,"dosage.route"
                                ,"dosage.dose.doseRange.low.value"
                                ,"dosage.dose.doseRange.low.units"
                                ,"dosage.dose.doseRange.high.value"
                                ,"dosage.dose.doseRange.high.units"
                                ,"dosage.dose.doseQuantity.value"
                                ,"dosage.dose.doseQuantity.units"
                                ,"dispenseRequest.validityPeriod.start"
                                ,"dispenseRequest.validityPeriod.end"
                                ,"dispenseRequest.numberOfRepeatsAllowed"
                                ,"dispenseRequest.quantity.value"
                                ,"dispenseRequest.quantity.units"
                                ,"dispenseRequest.expectedSupplyDuration.value"
                                ,"dispenseRequest.expectedSupplyDuration.units"
                        );
                if (theQuoteMode != null) {
                    format = format.withQuote('"').withQuoteMode(theQuoteMode);
                }
                parsed = new CSVParser(reader, format);
                Iterator<CSVRecord> iter = parsed.iterator();
                ourLog.debug("Header map: {}", parsed.getHeaderMap());

                int count = 0;

                int nextLoggedCount = 0;
                while (iter.hasNext()) {
                    CSVRecord nextRecord = iter.next();
                    System.out.println("On nextRecord");
                    handler.accept(nextRecord);
                    count++;
                    if (count >= nextLoggedCount) {
                        ourLog.info(" * Processed {} records", count);
                    }
                }

            } catch (IOException e) {
                throw new InternalErrorException(e);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }


    private void processActivitiesCSV(IRecordHandler handler, FhirContext ctx, char theDelimiter, QuoteMode theQuoteMode, InputStream file) throws CommandFailureException {


        Boolean found = false;
        try {


            found = true;

            Reader reader = null;
            CSVParser parsed = null;
            try {
                reader = new InputStreamReader(file);
                CSVFormat format = CSVFormat
                        .newFormat(theDelimiter)
                        .withAllowMissingColumnNames()
                        .withSkipHeaderRecord(true)
                        .withHeader("Date"
                                ,"Steps"
                                ,"Distance (m)"
                                ,"Elevation (m)"
                                ,"Active calories"
                        );
                if (theQuoteMode != null) {
                    format = format.withQuote('"').withQuoteMode(theQuoteMode);
                }
                parsed = new CSVParser(reader, format);
                Iterator<CSVRecord> iter = parsed.iterator();
                ourLog.debug("Header map: {}", parsed.getHeaderMap());

                int count = 0;

                int nextLoggedCount = 0;
                while (iter.hasNext()) {
                    CSVRecord nextRecord = iter.next();
                    handler.accept(nextRecord);
                    count++;
                    if (count >= nextLoggedCount) {
                        ourLog.info(" * Processed {} records", count);
                    }
                }

            } catch (IOException e) {
                throw new InternalErrorException(e);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void processBloodPressureCSV(IRecordHandler handler, FhirContext ctx, char theDelimiter, QuoteMode theQuoteMode, InputStream file) throws CommandFailureException {


        Boolean found = false;
        try {

            //ourLog.info("Processing file {}", file.getName());
            found = true;

            Reader reader = null;
            CSVParser parsed = null;
            try {
                reader = new InputStreamReader(file);
                CSVFormat format = CSVFormat
                        .newFormat(theDelimiter)
                        .withAllowMissingColumnNames()
                        .withSkipHeaderRecord(true)
                        .withHeader("Date"
                                ,"HeartRate",
                                "Systolic"
                                ,"Diastolic"
                                ,"Comments"
                        );
                if (theQuoteMode != null) {
                    format = format.withQuote('"').withQuoteMode(theQuoteMode);
                }
                parsed = new CSVParser(reader, format);
                Iterator<CSVRecord> iter = parsed.iterator();
                ourLog.debug("Header map: {}", parsed.getHeaderMap());

                int count = 0;

                int nextLoggedCount = 0;
                while (iter.hasNext()) {
                    CSVRecord nextRecord = iter.next();
                    handler.accept(nextRecord);
                    count++;
                    if (count >= nextLoggedCount) {
                        ourLog.info(" * Processed {} records", count, file);
                    }
                }

            } catch (IOException e) {
                throw new InternalErrorException(e);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }


    private interface IRecordHandler {
        void accept(CSVRecord theRecord);
    }

    public class ObsHandler implements  IRecordHandler {
        @Override
        public void accept(CSVRecord theRecord) {
           // System.out.println(theRecord.toString());

            Observation observation = newBasicObservation2(theRecord.get("EffectiveDateTime"), theRecord.get("FHIRCategory"), theRecord.get("FHIRCategory"),CareConnectSystem.FHIRObservationCategory);

            observation.addIdentifier()
                    .setSystem("https://fhir.leedsth.nhs.uk/Id/observation")
                    .setValue(theRecord.get("ObservationID"));
            CodeableConcept code = observation.getCode();
            code.addCoding().setSystem(CareConnectSystem.SNOMEDCT).setCode(theRecord.get("Code")).setDisplay(theRecord.get("CodeDisplay"));

            observation.setSubject(new Reference("Patient/"+theRecord.get("PatientID")));
            if (!theRecord.get("ValueQuantity").isEmpty()) {
                Quantity quantity = new Quantity();
                quantity
                        .setValue(new BigDecimal(theRecord.get("ValueQuantity")))
                        .setUnit(theRecord.get("ValueUnitOfMeasure"))
                        .setCode(theRecord.get("ValueUnitOfMeasure"))
                        .setSystem(CareConnectSystem.UnitOfMeasure);
                observation.setValue(quantity);
            }
            if (!theRecord.get("ValueCode").isEmpty()) {
                CodeableConcept valueCode = new CodeableConcept();
                valueCode.addCoding().setDisplay(theRecord.get("ValueString")).setCode(theRecord.get("ValueCode")).setSystem(CareConnectSystem.SNOMEDCT);
                observation.setValue(valueCode);
            }
            if (!theRecord.get("Method").isEmpty()) {
                observation.getMethod().addCoding().setSystem(CareConnectSystem.SNOMEDCT).setCode(theRecord.get("Method")).setDisplay(theRecord.get("MethodDisplay"));
            }
            if (!theRecord.get("BodySite").isEmpty()) {
                observation.getBodySite().addCoding().setSystem(CareConnectSystem.SNOMEDCT).setCode(theRecord.get("BodySite")).setDisplay(theRecord.get("BodySiteDescription"));
            }
            if (!theRecord.get("Interpretation").isEmpty()) {
                observation.getInterpretation().addCoding().setSystem(CareConnectSystem.HL7v2Table0078).setCode(theRecord.get("Interpretation")).setDisplay(theRecord.get("Interpretation"));
            }
            if (!theRecord.get("LowRange").isEmpty() || !theRecord.get("HighRange").isEmpty()) {
                Observation.ObservationReferenceRangeComponent range = observation.addReferenceRange();
                if (!theRecord.get("LowRange").isEmpty()) {
                    SimpleQuantity low = new SimpleQuantity();
                    low.setValue(new BigDecimal(theRecord.get("LowRange")));
                    range.setLow(low);
                }
                if (!theRecord.get("HighRange").isEmpty()) {

                    SimpleQuantity high = new SimpleQuantity();
                    high.setValue(new BigDecimal(theRecord.get("HighRange")));
                    range.setHigh(high);
                }
            }
            if (!theRecord.get("PerformerType").isEmpty()) {
             //   System.out.println(theRecord.get("PerformerType"));
            //    System.out.println(theRecord.get("PerformerIdentifier"));
                switch (theRecord.get("PerformerType")) {
                    case "practitioner" :
                        String practitioner = docMap.get(theRecord.get("PerformerIdentifier"));
                        if (practitioner == null) {
                            Bundle results = client
                                    .search()
                                    .forResource(Practitioner.class)
                                    .where(Practitioner.IDENTIFIER.exactly().code(theRecord.get("PerformerIdentifier")))
                                    .returnBundle(Bundle.class)
                                    .execute();
                         //   System.out.println(results.getEntry().size());
                            if (results.getEntry().size() > 0) {
                                Practitioner prac = (Practitioner) results.getEntry().get(0).getResource();
                                practitioner = prac.getIdElement().getIdPart();
                                docMap.put(theRecord.get("PerformerIdentifier"), practitioner);
                            }
                        }
                        if (practitioner != null) {
                            observation.addPerformer(new Reference("Practitioner/" + practitioner));
                        }

                        break;

                    case "Organization" :

                        String organization= orgMap.get(theRecord.get("PerformerIdentifier"));
                        if (organization == null) {
                            Bundle results = client
                                    .search()
                                    .forResource(Organization.class)
                                    .where(Organization.IDENTIFIER.exactly().code(theRecord.get("PerformerIdentifier")))
                                    .returnBundle(Bundle.class)
                                    .execute();
                         //   System.out.println(results.getEntry().size());
                            if (results.getEntry().size() > 0) {
                                Organization org = (Organization) results.getEntry().get(0).getResource();
                                organization = org.getIdElement().getIdPart();
                                orgMap.put(theRecord.get("PerformerIdentifier"), organization);
                            }
                        }
                        if (organization != null) {
                            observation.addPerformer(new Reference("Organization/" + organization));
                        }
                        break;

                }
            }

            //System.out.println(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(observation));
           resources.add(observation);
        }
    }

    public class EncounterHandler implements  IRecordHandler {
        @Override
        public void accept(CSVRecord theRecord) {
            Encounter encounter = new Encounter();


            encounter.setMeta(new Meta().addProfile(CareConnectProfile.Encounter_1));

            encounter.addIdentifier()
                    .setSystem("https://fhir.leedsth.nhs.uk/Id/encounter")
                    .setValue(theRecord.get("encounterID"));

            Period period = new Period();

            String dateString = theRecord.get("period.startDate");
            if (!dateString.isEmpty()) {
                if (!theRecord.get("period.startTime").isEmpty())
                    dateString = dateString + " " + theRecord.get("period.startTime");
                try {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    format.setTimeZone(TimeZone.getTimeZone("UTC"));
                    // turn off set linient
                    period.setStart(format.parse(dateString));
                } catch (Exception e) {
                    try {
                        //  System.out.println(dateString);
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                        format.setTimeZone(TimeZone.getTimeZone("UTC"));
                        period.setStart(format.parse(dateString));
                    } catch (Exception e2) {
                        e.printStackTrace();
                    }
                }
            }

            dateString = theRecord.get("period.endDate");
            if (!dateString.isEmpty()) {
                if (!theRecord.get("period.endTime").isEmpty())
                    dateString = dateString + " " + theRecord.get("period.endTime");
                try {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    format.setTimeZone(TimeZone.getTimeZone("UTC"));
                    // turn off set linient
                    period.setEnd(format.parse(dateString));
                } catch (Exception e) {
                    try {
                        //  System.out.println(dateString);
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                        format.setTimeZone(TimeZone.getTimeZone("UTC"));
                        period.setEnd(format.parse(dateString));
                    } catch (Exception e2) {
                        e.printStackTrace();
                    }
                }
            }
            encounter.setPeriod(period);
            encounter.setSubject(new Reference("Patient/"+theRecord.get("patientID")));



            if (!theRecord.get("resource.type").isEmpty() && !theRecord.get("participent.individual").isEmpty()) {


                if (theRecord.get("resource.type").equals("Practitioner")) {
                    String practitioner = docMap.get(theRecord.get("participent.individual"));
                    if (practitioner == null) {
                        Bundle results = client
                                .search()
                                .forResource(Practitioner.class)
                                .where(Practitioner.IDENTIFIER.exactly().code(theRecord.get("participent.individual")))
                                .returnBundle(Bundle.class)
                                .execute();
                        //   System.out.println(results.getEntry().size());
                        if (results.getEntry().size() > 0) {
                            Practitioner prac = (Practitioner) results.getEntry().get(0).getResource();
                            practitioner = prac.getIdElement().getIdPart();
                            docMap.put(theRecord.get("participent.individual"), practitioner);
                        }
                    }
                    if (practitioner != null) {
                        Encounter.EncounterParticipantComponent participant = encounter.addParticipant()
                                .setIndividual(new Reference("Practitioner/" + practitioner));
                        if (!theRecord.get("participant.type").isEmpty()) {
                            CodeableConcept type = new CodeableConcept();
                            switch (theRecord.get("participant.type")) {
                                case "PRF":
                                    type.addCoding().setSystem("http://hl7.org/fhir/ValueSet/encounter-participant-type").setCode("PPRF");
                                    break;
                                default:
                                    System.out.println("participant.type=" + theRecord.get("participant.type"));

                            }
                            if (type.getCoding().size()>0) {
                                participant.addType(type);
                            }

                        }
                    }
                }
            }
/*
            System.out.println("participant.type=" + theRecord.get("participant.type"));
            System.out.println("resource.type="+theRecord.get("resource.type"));
            System.out.println("participent.individual="+theRecord.get("participent.individual"));
            System.out.println("serviceProvider="+theRecord.get("serviceProvider"));
*/
            if (!theRecord.get("serviceProvider").isEmpty()) {
                String organization= orgMap.get(theRecord.get("serviceProvider"));
                if (organization == null) {
                    Bundle results = client
                            .search()
                            .forResource(Organization.class)
                            .where(Organization.IDENTIFIER.exactly().code(theRecord.get("serviceProvider")))
                            .returnBundle(Bundle.class)
                            .execute();
                    //   System.out.println(results.getEntry().size());
                    if (results.getEntry().size() > 0) {
                        Organization org = (Organization) results.getEntry().get(0).getResource();
                        organization = org.getIdElement().getIdPart();
                        orgMap.put(theRecord.get("serviceProvider"), organization);
                    }
                }
                if (organization != null) {
                    encounter
                            .setServiceProvider(new Reference("Organization/"+organization));

                }
            }

            //System.out.println(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(encounter));
            resources.add(encounter);
        }
    }

    public class ConditionHandler implements  IRecordHandler {
        @Override
        public void accept(CSVRecord theRecord) {
           Condition condition = new Condition();

           condition.addIdentifier()
                   .setSystem("https://fhir.leedsth.nhs.uk/Id/condition")
                   .setValue(theRecord.get("identifier"));

           condition.setSubject(new Reference("Patient/" + theRecord.get("patient ID")));

            if (!theRecord.get("encounter").isEmpty()) {
                Bundle results = client
                        .search()
                        .forResource(Encounter.class)
                        .where(Encounter.IDENTIFIER.exactly().code(theRecord.get("encounter")))
                        .returnBundle(Bundle.class)
                        .execute();
                //   System.out.println(results.getEntry().size());
                if (results.getEntry().size() > 0) {
                    Encounter encounter = (Encounter) results.getEntry().get(0).getResource();

                    condition.setContext(new Reference("Encounter/" + encounter.getIdElement().getIdPart()));
                }
            }
            if (!theRecord.get("asserter").isEmpty()) {
                Bundle results = client
                        .search()
                        .forResource(Practitioner.class)
                        .where(Practitioner.IDENTIFIER.exactly().code(theRecord.get("asserter")))
                        .returnBundle(Bundle.class)
                        .execute();
                //   System.out.println(results.getEntry().size());
                if (results.getEntry().size() > 0) {
                    Practitioner practitioner = (Practitioner) results.getEntry().get(0).getResource();

                    condition.setAsserter(new Reference("Practitioner/" + practitioner.getIdElement().getIdPart()));
                }
            }

            String dateString = theRecord.get("dateRecorded");

            if (!dateString.isEmpty()) {

                try {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    // turn off set linient
                    condition.setAssertedDate(format.parse(dateString));
                } catch (Exception e) {
                    try {
                        //  System.out.println(dateString);
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                        format.setTimeZone(TimeZone.getTimeZone("UTC"));
                        //System.out.println(format.parse(dateString).toString());
                        condition.setAssertedDate(format.parse(dateString));
                    } catch (Exception e2) {
                        e.printStackTrace();
                    }
                }
            }
            if (!theRecord.get("code.coding.code").isEmpty()) {
                condition.getCode().addCoding()
                        .setCode(theRecord.get("code.coding.code"))
                        .setSystem(CareConnectSystem.SNOMEDCT)
                        .setDisplay(theRecord.get("code.coding.display"));
            }

            if (!theRecord.get("category.code").isEmpty()) {
                switch (theRecord.get("category.code")) {
                    case "diagnosis":
                        condition.addCategory().addCoding()
                            .setSystem("http://hl7.org/fhir/condition-category")
                                .setCode("encounter-diagnosis")
                                .setDisplay("Encounter Diagnosis");
                        break;

                }
            }
            if (!theRecord.get("clinicalStatus").isEmpty()) {
                switch (theRecord.get("clinicalStatus")) {
                    case "Resolved":
                        condition.setClinicalStatus(Condition.ConditionClinicalStatus.RESOLVED);
                        break;
                    case "Active":
                        condition.setClinicalStatus(Condition.ConditionClinicalStatus.ACTIVE);
                        break;
                    case "Remission":
                        condition.setClinicalStatus(Condition.ConditionClinicalStatus.REMISSION);
                        break;
                    case "Relapse":
                        condition.setClinicalStatus(Condition.ConditionClinicalStatus.RECURRENCE);
                        break;
                    case "Inactive":
                        condition.setClinicalStatus(Condition.ConditionClinicalStatus.INACTIVE);
                        break;
                        default:
                            System.out.println("****** = "+theRecord.get("clinicalStatus"));

                }
            }
            if (!theRecord.get("verificationStatus").isEmpty()) {
                switch (theRecord.get("verificationStatus")) {
                    case "Confirmed" :
                        condition.setVerificationStatus(Condition.ConditionVerificationStatus.CONFIRMED);
                        break;
                    case "Provisional" :
                        condition.setVerificationStatus(Condition.ConditionVerificationStatus.PROVISIONAL);
                        break;
                    case "Entered-In-Error" :
                        condition.setVerificationStatus(Condition.ConditionVerificationStatus.ENTEREDINERROR);
                        break;
                    default:
                        System.out.println("****** verificationStatus = " + theRecord.get("verificationStatus"));

                }

            }
            if (!theRecord.get("severity.code.coding.code").isEmpty()) {
                condition.getSeverity().addCoding()
                        .setSystem(CareConnectSystem.SNOMEDCT)
                        .setCode(theRecord.get("severity.code.coding.code"))
                        .setDisplay(theRecord.get("serverity.code.coding.description"));
                //System.out.println("****** severity.code.coding.code = " + theRecord.get("severity.code.coding.code"));
            }

            //System.out.println(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(condition));
            resources.add(condition);
        }
    }

    public class AllergyHandler implements  IRecordHandler {
        @Override
        public void accept(CSVRecord theRecord) {
            AllergyIntolerance allergy = new AllergyIntolerance();

            allergy.addIdentifier()
                    .setSystem("https://fhir.leedsth.nhs.uk/Id/allergy")
                    .setValue(theRecord.get("identifier"));

            allergy.setPatient(new Reference("Patient/" + theRecord.get("patient")));

          //  AllergyIntolerance.AllergyIntoleranceReactionComponent reaction = allergy.addReaction();
            if (!theRecord.get("substance.coding.code").isEmpty()) {
                allergy.getCode().addCoding()
                        .setCode(theRecord.get("substance.coding.code"))
                        .setDisplay(theRecord.get("substance.coding.display"))
                        .setSystem(CareConnectSystem.SNOMEDCT);
            }

            if (!theRecord.get("clinicalStatus").isEmpty()) {
                switch (theRecord.get("clinicalStatus")) {
                    case "Active":
                        allergy.setClinicalStatus(AllergyIntolerance.AllergyIntoleranceClinicalStatus.ACTIVE);
                        break;
                    case "Inactive" :
                        allergy.setClinicalStatus(AllergyIntolerance.AllergyIntoleranceClinicalStatus.INACTIVE);
                        break;
                    case "Resolved" :
                        allergy.setClinicalStatus(AllergyIntolerance.AllergyIntoleranceClinicalStatus.RESOLVED);
                        break;
                    default :
                        System.out.println("***** clinicalStatus = "+theRecord.get("clinicalStatus"));

                }
            }

            if (!theRecord.get("verificationStatus").isEmpty()) {
                switch (theRecord.get("verificationStatus")) {
                    case "Unconfirmed":
                        allergy.setVerificationStatus(AllergyIntolerance.AllergyIntoleranceVerificationStatus.UNCONFIRMED);
                        break;
                    case "Confirmed" :
                        allergy.setVerificationStatus(AllergyIntolerance.AllergyIntoleranceVerificationStatus.CONFIRMED);
                        break;
                    case "Refuted" :
                        allergy.setVerificationStatus(AllergyIntolerance.AllergyIntoleranceVerificationStatus.REFUTED);
                        break;
                    case "Entered-In-Error" :
                        allergy.setVerificationStatus(AllergyIntolerance.AllergyIntoleranceVerificationStatus.ENTEREDINERROR);
                        break;
                    default :
                        System.out.println("***** verificationStatus = "+theRecord.get("verificationStatus"));

                }
            }

            if (!theRecord.get("onset.DateTime").isEmpty()) {
                try {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    format.setTimeZone(TimeZone.getTimeZone("UTC"));

                    allergy.setOnset(new DateTimeType(format.parse(theRecord.get("onset.DateTime"))));
                } catch (Exception e) {
                    try {
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
                        format.setTimeZone(TimeZone.getTimeZone("UTC"));

                        allergy.setOnset(new DateTimeType(format.parse(theRecord.get("onset.DateTime"))));
                    } catch (Exception e1) {
                        try {
                            SimpleDateFormat format = new SimpleDateFormat("yyyy");
                            format.setTimeZone(TimeZone.getTimeZone("UTC"));

                            allergy.setOnset(new DateTimeType(format.parse(theRecord.get("onset.DateTime"))));
                        } catch (Exception e2) {

                        }
                    }

                }
            }

            if (!theRecord.get("assertedDate").isEmpty()) {
                try {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    format.setTimeZone(TimeZone.getTimeZone("UTC"));

                    allergy.setAssertedDate(format.parse(theRecord.get("assertedDate")));
                } catch (Exception e) {


                }
            }

            if (!theRecord.get("lastOccurrence").isEmpty()) {
                try {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    format.setTimeZone(TimeZone.getTimeZone("UTC"));

                    allergy.setLastOccurrence(format.parse(theRecord.get("lastOccurrence")));
                } catch (Exception e) {
                    try {
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
                        format.setTimeZone(TimeZone.getTimeZone("UTC"));

                        allergy.setLastOccurrence(format.parse(theRecord.get("lastOccurrence")));
                    } catch (Exception e1) {
                        try {
                            SimpleDateFormat format = new SimpleDateFormat("yyyy");
                            format.setTimeZone(TimeZone.getTimeZone("UTC"));

                            allergy.setLastOccurrence(format.parse(theRecord.get("lastOccurrence")));
                        } catch (Exception e2) {

                        }
                    }
                }
            }
            if (!theRecord.get("recorder").isEmpty()) {
                Bundle results = client
                        .search()
                        .forResource(Practitioner.class)
                        .where(Practitioner.IDENTIFIER.exactly().code(theRecord.get("recorder")))
                        .returnBundle(Bundle.class)
                        .execute();
                //   System.out.println(results.getEntry().size());
                if (results.getEntry().size() > 0) {
                    Practitioner practitioner = (Practitioner) results.getEntry().get(0).getResource();

                    allergy.setAsserter(new Reference("Practitioner/" + practitioner.getIdElement().getIdPart()));
                }
            }

         //   System.out.println(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(allergy));
            resources.add(allergy);
        }
    }

    public class ProcedureHandler implements  IRecordHandler {
        @Override
        public void accept(CSVRecord theRecord) {
            Procedure procedure = new Procedure();

            procedure.addIdentifier()
                    .setSystem("https://fhir.leedsth.nhs.uk/Id/procedure")
                    .setValue(theRecord.get("identifier"));

            procedure.setSubject(new Reference("Patient/" + theRecord.get("subject")));
            if (!theRecord.get("status").isEmpty()) {
                switch (theRecord.get("status")) {
                    case "Completed" :
                        procedure.setStatus(Procedure.ProcedureStatus.COMPLETED);
                        break;
                }
            }
            if (!theRecord.get("code.coding.code").isEmpty()) {
                procedure.getCode().addCoding()
                        .setCode(theRecord.get("code.coding.code"))
                        .setSystem(CareConnectSystem.SNOMEDCT)
                        .setDisplay(theRecord.get("code.coding.display"));
            }
            if (!theRecord.get("category.code.coding.code").isEmpty()) {
                procedure.getCategory().addCoding()
                        .setCode(theRecord.get("category.code.coding.code"))
                        .setSystem(CareConnectSystem.SNOMEDCT)
                        .setDisplay(theRecord.get("category.code.coding.display"));
            }

            String dateString = theRecord.get("performed date");
            if (!theRecord.get("performed time").isEmpty()) dateString = dateString + " " +"performed time";

            if (!dateString.isEmpty()) {

                try {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    format.setTimeZone(TimeZone.getTimeZone("UTC"));
                    // turn off set linient
                    procedure.setPerformed(new DateTimeType(format.parse(dateString)));
                } catch (Exception e) {
                    try {
                        //  System.out.println(dateString);
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                        format.setTimeZone(TimeZone.getTimeZone("UTC"));
                        procedure.setPerformed(new DateTimeType(format.parse(dateString)));
                    } catch (Exception e2) {
                        e.printStackTrace();
                    }
                }
            }
            if (!theRecord.get("notPerformed").isEmpty()) {
                switch (theRecord.get("notPerformed")) {
                    case "FALSE" :
                        procedure.setNotDone(false);
                        break;
                    case "TRUE" :
                        procedure.setNotDone(true);
                        break;
                }
            }
            if (!theRecord.get("Performer Type").isEmpty()) {
                switch (theRecord.get("Performer Type")) {
                    case "Practitioner":
                        Bundle results = client
                                .search()
                                .forResource(Practitioner.class)
                                .where(Practitioner.IDENTIFIER.exactly().code(theRecord.get("performer")))
                                .returnBundle(Bundle.class)
                                .execute();
                        //   System.out.println(results.getEntry().size());
                        if (results.getEntry().size() > 0) {
                            Practitioner practitioner = (Practitioner) results.getEntry().get(0).getResource();

                            Procedure.ProcedurePerformerComponent performer = procedure.addPerformer();
                            performer.setActor(new Reference("Practitioner/" + practitioner.getIdElement().getIdPart()));
                        }
                        break;
                    case "Organization":
                        results = client
                                .search()
                                .forResource(Organization.class)
                                .where(Organization.IDENTIFIER.exactly().code(theRecord.get("performer")))
                                .returnBundle(Bundle.class)
                                .execute();
                        //   System.out.println(results.getEntry().size());
                        if (results.getEntry().size() > 0) {
                            Organization organization = (Organization) results.getEntry().get(0).getResource();

                            Procedure.ProcedurePerformerComponent performer = procedure.addPerformer();
                            performer.setActor(new Reference("Organization/" + organization.getIdElement().getIdPart()));
                        }
                }
            }
            if (!theRecord.get("encounter").isEmpty()) {
                Bundle results = client
                        .search()
                        .forResource(Encounter.class)
                        .where(Encounter.IDENTIFIER.exactly().code(theRecord.get("encounter")))
                        .returnBundle(Bundle.class)
                        .execute();
                //   System.out.println(results.getEntry().size());
                if (results.getEntry().size() > 0) {
                    Encounter encounter = (Encounter) results.getEntry().get(0).getResource();
              procedure.setContext(new Reference("Encounter/" + encounter.getIdElement().getIdPart()));
                }
            }
            if (!theRecord.get("outcome.coding.code").isEmpty()) {
                procedure.getOutcome().addCoding()
                        .setCode(theRecord.get("outcome.coding.code"))
                        .setSystem(CareConnectSystem.SNOMEDCT)
                        .setDisplay(theRecord.get("ouctome.coding.display"));
            }

            System.out.println(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(procedure));
            resources.add(procedure);
        }
    }

    public class AllergyReactionsHandler implements  IRecordHandler {
        @Override
        public void accept(CSVRecord theRecord) {

            for (IBaseResource resource : resources) {
                if (resource instanceof AllergyIntolerance) {
                    AllergyIntolerance allergy = (AllergyIntolerance) resource;
                    if (allergy.getIdentifier().size()>0 && theRecord.get("allergy.identifier").equals(allergy.getIdentifier().get(0).getValue())) {
                        AllergyIntolerance.AllergyIntoleranceReactionComponent reaction = allergy.addReaction();
                        reaction.addManifestation().addCoding()
                                .setSystem(CareConnectSystem.SNOMEDCT)
                                .setDisplay(theRecord.get("reaction.manifestation.display"))
                                .setCode(theRecord.get("reaction.manifestation"));
                        System.out.println(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(allergy));
                    }
                }
            }

        }
    }


    public class ImmunisationHandler implements  IRecordHandler {
        @Override
        public void accept(CSVRecord theRecord) {
            Immunization immunisation = new Immunization();

            immunisation.addIdentifier()
                    .setSystem("https://fhir.leedsth.nhs.uk/Id/immunisation")
                    .setValue(theRecord.get("identifier"));

            immunisation.setPatient(new Reference("Patient/" + theRecord.get("patientID")));

            if (!theRecord.get("dateRecorded").isEmpty()) {
                try {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    format.setTimeZone(TimeZone.getTimeZone("UTC"));

                   immunisation.addExtension()
                        .setUrl(CareConnectExtension.UrlImmunizationDateRecorded)
                        .setValue(new DateTimeType(format.parse(theRecord.get("dateRecorded"))));
                } catch (Exception e) {

                }
            }
            if (!theRecord.get("parentPresent").isEmpty()) {

                switch (theRecord.get("parentPresent")) {
                    case "FALSE":
                        immunisation.addExtension()
                                .setUrl(CareConnectExtension.UrlImmunizationParentPresent)
                                .setValue(new BooleanType(false));
                        break;
                    case "TRUE":
                        immunisation.addExtension()
                                .setUrl(CareConnectExtension.UrlImmunizationParentPresent)
                                .setValue(new BooleanType(true));
                        break;
                }
            }
            if (!theRecord.get("status").isEmpty()) {
                switch (theRecord.get("status")) {
                    case "Completed":
                        immunisation.setStatus(Immunization.ImmunizationStatus.COMPLETED);
                        break;
                }
            }

            String dateString = theRecord.get("dateAdministered");
            if (!dateString.isEmpty() && !theRecord.get("timeAdministered").isEmpty()) {
                dateString = dateString + " " +theRecord.get("timeAdministered");
            }

            if (!dateString.isEmpty()) {

                try {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    format.setTimeZone(TimeZone.getTimeZone("UTC"));
                    // turn off set linient
                    immunisation.setDate(format.parse(dateString));
                } catch (Exception e) {
                    try {
                        //  System.out.println(dateString);
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                        format.setTimeZone(TimeZone.getTimeZone("UTC"));
                        immunisation.setDate(format.parse(dateString));
                    } catch (Exception e2) {
                        e.printStackTrace();
                    }
                }
            }
            if (!theRecord.get("vaccineCode.coding").isEmpty()) {
                immunisation.getVaccineCode().addCoding()
                        .setSystem(CareConnectSystem.SNOMEDCT)
                        .setCode(theRecord.get("vaccineCode.coding"))
                        .setDisplay(theRecord.get("vaccineCode.coding.display"));

            }
            if (!theRecord.get("notGiven").isEmpty()) {

                switch (theRecord.get("notGiven")) {
                    case "FALSE":
                        immunisation.setNotGiven(false);
                        break;
                    case "TRUE":
                        immunisation.setNotGiven(true);
                        break;
                }
            }

            if (!theRecord.get("encounter").isEmpty()) {
                Bundle results = client
                        .search()
                        .forResource(Encounter.class)
                        .where(Encounter.IDENTIFIER.exactly().code(theRecord.get("encounter")))
                        .returnBundle(Bundle.class)
                        .execute();
                System.out.println("***** Encounter ID = "+theRecord.get("encounter")+ " Results = "+results.getEntry().size());
                if (results.getEntry().size() > 0) {
                    Encounter encounter = (Encounter) results.getEntry().get(0).getResource();

                    immunisation.setEncounter(new Reference("Encounter/" + encounter.getIdElement().getIdPart()));
                }
            }


            if (!theRecord.get("primarySource").isEmpty()) {

                switch (theRecord.get("primarySource")) {
                    case "FALSE":
                        immunisation.setPrimarySource(false);
                        break;
                    case "TRUE":
                        immunisation.setPrimarySource(true);
                        break;
                }
            }

            if (!theRecord.get("reportOrigin").isEmpty()) {

                switch (theRecord.get("reportOrigin")) {
                    case "provider":
                        immunisation.getReportOrigin().addCoding()
                                .setCode("provider")
                                .setSystem("http://hl7.org/fhir/immunization-origin")
                                .setDisplay("Provider");
                        break;

                }
            }

            if (!theRecord.get("location").isEmpty()) {
                Bundle results = client
                        .search()
                        .forResource(Location.class)
                        .where(Location.IDENTIFIER.exactly().code(theRecord.get("location")))
                        .returnBundle(Bundle.class)
                        .execute();
                if (results.getEntry().size() > 0) {
                    Location location = (Location) results.getEntry().get(0).getResource();

                    immunisation.setLocation(new Reference("Location" +
                            "/" + location.getIdElement().getIdPart()));
                }
            }
            if (!theRecord.get("lotNumber").isEmpty()) {
                   immunisation.setLotNumber(theRecord.get("lotNumber"));
                }


            if (!theRecord.get("expirationDate").isEmpty()) {
            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                format.setTimeZone(TimeZone.getTimeZone("UTC"));

                immunisation.setExpirationDate(format.parse(theRecord.get("expirationDate")));
            } catch (Exception e) {

            }
        }
            // TODO


            System.out.println(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(immunisation));
            resources.add(immunisation);
        }
    }

    public class PatientHandler implements  IRecordHandler {

        FhirContext ctx;
        IGenericClient client;

	    PatientHandler(FhirContext ctx,IGenericClient client) {
	        this.ctx = ctx;
	        this.client = client;
        }
         @Override
        public void accept(CSVRecord theRecord) {

	        Patient patient = (Patient) client.read().resource(Patient.class).withId(theRecord.get("PATIENT_ID")).execute();

	        if (theRecord.get("PRACTICE_ID") != null && !theRecord.get("PRACTICE_ID").isEmpty()) {
                Bundle bundle = client.search().forResource(Organization.class)
                        .where(Organization.IDENTIFIER.exactly().code(theRecord.get("PRACTICE_ID")))
                        .returnBundle(Bundle.class).execute();

                if (bundle.getEntry().size()>0) {
                    Organization org = (Organization) bundle.getEntry().get(0).getResource();
                    patient.setManagingOrganization(new Reference( "Organization/"+org.getIdElement().getIdPart() ));
                }

            }
            if (theRecord.get("GP_ID") != null && !theRecord.get("GP_ID").isEmpty()) {
                 Bundle bundle = client.search().forResource(Practitioner.class)
                         .where(Practitioner.IDENTIFIER.exactly().code(theRecord.get("GP_ID")))
                         .returnBundle(Bundle.class).execute();

                 if (bundle.getEntry().size()>0) {
                     Practitioner gp = (Practitioner) bundle.getEntry().get(0).getResource();
                     patient.addGeneralPractitioner(new Reference( "Practitioner/"+gp.getIdElement().getIdPart() ));
                 }

             }

             Boolean found = false;
	        for (Identifier identifier : patient.getIdentifier()) {
	            if (identifier.getSystem().contains("https://fhir.leedsth.nhs.uk/Id/PPMIdentifier")) {
	                found = true;
                }
            }
            if (!found) {
	            patient.addIdentifier()
                        .setSystem("https://fhir.leedsth.nhs.uk/Id/PPMIdentifier")
                        .setValue(theRecord.get("PATIENT_ID"));
            }
	        //System.out.println(ctx.newJsonParser().encodeResourceToString(patient));

	        resources.add(patient);

        }
    }

    public class PrescriptionHandler implements  IRecordHandler {



        @Override
        public void accept(CSVRecord theRecord) {


            MedicationRequest prescription = new MedicationRequest();

            if (!theRecord.get("identifier").isEmpty()) {

                prescription.addIdentifier()
                        .setSystem("https://fhir.leedsth.nhs.uk/Id/prescription")
                        .setValue(theRecord.get("identifier"));
            }
            if (!theRecord.get("subject (patientID)").isEmpty()) {

                prescription.setSubject(new Reference("Patient/" + theRecord.get("subject (patientID)")));
            }
            if (!theRecord.get("status").isEmpty()) {
                switch (theRecord.get("status")) {
                    case "completed" :
                        prescription.setStatus(MedicationRequest.MedicationRequestStatus.COMPLETED);
                }

            }
            if (!theRecord.get("intent").isEmpty()) {
                switch (theRecord.get("intent")) {
                    case "order" :
                        prescription.setIntent(MedicationRequest.MedicationRequestIntent.ORDER);
                }

            }
            if (!theRecord.get("priority").isEmpty()) {
                switch (theRecord.get("priority")) {
                    case "routine" :
                        prescription.setPriority(MedicationRequest.MedicationRequestPriority.ROUTINE);
                }

            }
            if (!theRecord.get("medication").isEmpty()) {
                CodeableConcept med = new CodeableConcept();
                med.addCoding().setCode(theRecord.get("medication")).setSystem(CareConnectSystem.SNOMEDCT);

                prescription.setMedication(med);
            }
            if (!theRecord.get("context").isEmpty()) {
                Bundle results = client
                        .search()
                        .forResource(Encounter.class)
                        .where(Encounter.IDENTIFIER.exactly().code(theRecord.get("context")))
                        .returnBundle(Bundle.class)
                        .execute();

                if (results.getEntry().size() > 0) {
                    Encounter encounter = (Encounter) results.getEntry().get(0).getResource();

                    prescription.setContext(new Reference("Encounter/" + encounter.getIdElement().getIdPart()));
                }
            }

            String dateString = theRecord.get("authoredOn Date");
            if (!theRecord.get("authoredOn Time").isEmpty()) dateString = dateString + " " +"authoredOn Time";

            if (!dateString.isEmpty()) {

                try {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    format.setTimeZone(TimeZone.getTimeZone("UTC"));
                    // turn off set linient
                    prescription.setAuthoredOn(format.parse(dateString));
                } catch (Exception e) {
                    try {
                        //  System.out.println(dateString);
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                        format.setTimeZone(TimeZone.getTimeZone("UTC"));
                        prescription.setAuthoredOn(format.parse(dateString));
                    } catch (Exception e2) {
                        e.printStackTrace();
                    }
                }
            }
            if (!theRecord.get("requester.agent type").isEmpty() && !theRecord.get("requester.agent").isEmpty()) {
                switch (theRecord.get("requester.agent type")) {
                    case "Practitioner":
                        Bundle results = client
                                .search()
                                .forResource(Practitioner.class)
                                .where(Practitioner.IDENTIFIER.exactly().code(theRecord.get("requester.agent")))
                                .returnBundle(Bundle.class)
                                .execute();

                        if (results.getEntry().size() > 0) {
                            Practitioner practitioner = (Practitioner) results.getEntry().get(0).getResource();

                            prescription.getRequester().setAgent(new Reference("Practitioner/" + practitioner.getIdElement().getIdPart()));
                        }
                        break;

                    case "Organization":
                        results = client
                                .search()
                                .forResource(Organization.class)
                                .where(Organization.IDENTIFIER.exactly().code(theRecord.get("requester.agent")))
                                .returnBundle(Bundle.class)
                                .execute();

                        if (results.getEntry().size() > 0) {
                            Organization organization = (Organization) results.getEntry().get(0).getResource();

                            prescription.getRequester().setAgent(new Reference("Organization/" + organization.getIdElement().getIdPart()));
                        }
                        break;

                }
            }
            if (!theRecord.get("reasonCode Type").isEmpty() && !theRecord.get("reasonCode").isEmpty()) {
                switch (theRecord.get("reasonCode Type")) {
                    case "Condition":
                        Bundle results = client
                                .search()
                                .forResource(Condition.class)
                                .where(Condition.IDENTIFIER.exactly().code(theRecord.get("reasonCode")))
                                .returnBundle(Bundle.class)
                                .execute();

                        if (results.getEntry().size() > 0) {
                            Condition condition = (Condition) results.getEntry().get(0).getResource();

                            prescription.addReasonReference(new Reference("Condition/" + condition.getIdElement().getIdPart()));
                        }
                        break;
                }
            }
            Dosage dosage= prescription.addDosageInstruction();

            if (!theRecord.get("dosage.text").isEmpty()) {
                dosage.setText(theRecord.get("dosage.text"));
            }
            if (!theRecord.get("dosage.additionalInstruction").isEmpty()) {
                CodeableConcept additional  = new CodeableConcept();
                additional.addCoding().setSystem(CareConnectSystem.SNOMEDCT).setCode(theRecord.get("dosage.additionalInstruction"));
                dosage.getAdditionalInstruction().add(additional);
            }

            if (!theRecord.get("dosage.asNeeded.asNeededBoolean").isEmpty()) {
                switch (theRecord.get("dosage.asNeeded.asNeededBoolean")) {
                    case "FALSE":
                        dosage.setAsNeeded(new BooleanType(false));
                        break;
                    case "TRUE":
                        dosage.setAsNeeded(new BooleanType(true));
                        break;
                }
            }
            if (!theRecord.get("dosage.asNeeded.asNeededCodableConcept").isEmpty()) {
                CodeableConcept additional  = new CodeableConcept();
                additional.addCoding().setSystem(CareConnectSystem.SNOMEDCT).setCode(theRecord.get("dosage.asNeeded.asNeededCodableConcept"));
                dosage.setAsNeeded(additional);
            }
            if (!theRecord.get("dosage.route").isEmpty()) {
                CodeableConcept additional  = new CodeableConcept();
                additional.addCoding().setSystem(CareConnectSystem.SNOMEDCT).setCode(theRecord.get("dosage.route"));
                dosage.setRoute(additional);
            }
            Range range = new Range();
            dosage.setDose(range);

            if (!theRecord.get("dosage.dose.doseRange.low.value").isEmpty()) {
                SimpleQuantity qty = new SimpleQuantity();
                qty.setValue(new BigDecimal(theRecord.get("dosage.dose.doseRange.low.value")));
                qty.setCode(theRecord.get("dosage.dose.doseRange.low.units"));
                qty.setSystem(CareConnectSystem.SNOMEDCT);
                range.setLow(qty);
            }

            if (!theRecord.get("dosage.dose.doseRange.high.value").isEmpty()) {
                SimpleQuantity qty = new SimpleQuantity();
                qty.setValue(new BigDecimal(theRecord.get("dosage.dose.doseRange.high.value")));
                qty.setCode(theRecord.get("dosage.dose.doseRange.high.units"));
                qty.setSystem(CareConnectSystem.SNOMEDCT);
                range.setHigh(qty);
            }
            if (!theRecord.get("dosage.dose.doseQuantity.value").isEmpty() && !theRecord.get("dosage.dose.doseQuantity.units").isEmpty()) {
                SimpleQuantity qty = new SimpleQuantity();
                qty.setValue(new BigDecimal(theRecord.get("dosage.dose.doseQuantity.value")));
                qty.setCode(theRecord.get("dosage.dose.doseQuantity.units"));
                qty.setSystem(CareConnectSystem.SNOMEDCT);
                dosage.setDose(qty);
            }
            MedicationRequest.MedicationRequestDispenseRequestComponent dispense =  prescription.getDispenseRequest();

            Period period = dispense.getValidityPeriod();

            dateString = theRecord.get("dispenseRequest.validityPeriod.start");
            if (!dateString.isEmpty()) {
                try {
                    //  System.out.println(dateString);
                    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                    format.setTimeZone(TimeZone.getTimeZone("UTC"));
                    period.setStart(format.parse(dateString));
                } catch (Exception e2) {
                    e2.printStackTrace();
                }

            }
            dateString = theRecord.get("dispenseRequest.validityPeriod.end");
            if (!dateString.isEmpty()) {
                try {
                    //  System.out.println(dateString);
                    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                    format.setTimeZone(TimeZone.getTimeZone("UTC"));
                    period.setEnd(format.parse(dateString));
                } catch (Exception e2) {
                    e2.printStackTrace();
                }

            }
            if (!theRecord.get("dispenseRequest.numberOfRepeatsAllowed").isEmpty() ) {

                dispense.setNumberOfRepeatsAllowed(Integer.parseInt(theRecord.get("dispenseRequest.numberOfRepeatsAllowed")));
            }
            if (!theRecord.get("dispenseRequest.quantity.value").isEmpty() && !theRecord.get("dispenseRequest.quantity.units").isEmpty()) {
                SimpleQuantity qty = new SimpleQuantity();
                qty.setValue(new BigDecimal(theRecord.get("dispenseRequest.quantity.value")));
                qty.setCode(theRecord.get("dispenseRequest.quantity.units"));
                qty.setSystem(CareConnectSystem.SNOMEDCT);
                dispense.setQuantity(qty);
            }
            if (!theRecord.get("dispenseRequest.expectedSupplyDuration.value").isEmpty() && !theRecord.get("dispenseRequest.expectedSupplyDuration.units").isEmpty()) {
                Duration duration = new Duration();
                duration.setValue(new BigDecimal(theRecord.get("dispenseRequest.expectedSupplyDuration.value")));
                duration.setCode(theRecord.get("dispenseRequest.expectedSupplyDuration.units"));
                duration.setSystem(CareConnectSystem.UnitOfMeasure);
                dispense.setExpectedSupplyDuration(duration);
            }
            /*
    TODO                              ,"dosage.timing"
             */

            System.out.println(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(prescription));

            resources.add(prescription);

        }
    }

    public class BloodPressureHandler implements IRecordHandler {
        @Override
        public void accept(CSVRecord theRecord) {

            if (!theRecord.get("HeartRate").isEmpty()) {
                try {

                    Observation observation = newBasicObservation(theRecord.get("Date"), "364066008", "Cardiovascular observable",CareConnectSystem.SNOMEDCT);
                    observation.addIdentifier()
                            .setSystem(nokiaObs)
                            .setValue("CD"+stripChar(theRecord.get("Date")));
                    CodeableConcept code = observation.getCode();
                    code.addCoding().setSystem(CareConnectSystem.SNOMEDCT).setCode("364075005").setDisplay("Heart rate");

                    Quantity quantity = new Quantity();
                    quantity
                            .setValue(new BigDecimal(theRecord.get("HeartRate")))
                            .setUnit("{beats}/min")
                            .setCode("{beats}/min")
                            .setSystem(CareConnectSystem.UnitOfMeasure);
                    observation.setValue(quantity);

                    resources.add(observation);


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (!theRecord.get("Systolic").isEmpty() && !theRecord.get("Diastolic").isEmpty()) {
                try {

                    Observation observation = newBasicObservation(theRecord.get("Date"), "364066008", "Cardiovascular observable",CareConnectSystem.SNOMEDCT);
                    observation.addIdentifier()
                            .setSystem(nokiaObs)
                            .setValue("BP"+stripChar(theRecord.get("Date")));
                    CodeableConcept code = observation.getCode();
                    code.addCoding().setSystem(CareConnectSystem.SNOMEDCT).setCode("75367002").setDisplay("Blood pressure");

                    Observation.ObservationComponentComponent systolic = observation.addComponent();
                    systolic.getCode().addCoding().setSystem(CareConnectSystem.SNOMEDCT).setCode("271649006").setDisplay("Systolic blood pressure");

                    Quantity quantity = new Quantity();
                    quantity
                            .setValue(new BigDecimal(theRecord.get("Systolic")))
                            .setUnit("mm[Hg]")
                            .setCode("mm[Hg]")
                            .setSystem(CareConnectSystem.UnitOfMeasure);
                    systolic.setValue(quantity);

                    Observation.ObservationComponentComponent diastolic = observation.addComponent();
                    diastolic.getCode().addCoding().setSystem(CareConnectSystem.SNOMEDCT).setCode("271650006").setDisplay("Diastolic blood pressure");

                    Quantity diaQuantity = new Quantity();
                    diaQuantity
                            .setValue(new BigDecimal(theRecord.get("Diastolic")))
                            .setUnit("mm[Hg]")
                            .setCode("mm[Hg]")
                            .setSystem(CareConnectSystem.UnitOfMeasure);
                    diastolic.setValue(diaQuantity);

                    resources.add(observation);


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public class WeightHandler implements IRecordHandler {
        @Override
        public void accept(CSVRecord theRecord) {
            if (!theRecord.get("Weight").isEmpty()) {

                Observation observation = newBasicObservation(theRecord.get("Date"), "248326004", "Body measure",CareConnectSystem.SNOMEDCT);
                observation.addIdentifier()
                        .setSystem(nokiaObs)
                        .setValue("WT"+stripChar(theRecord.get("Date")));
                CodeableConcept code = observation.getCode();
                code.addCoding().setSystem(CareConnectSystem.SNOMEDCT).setCode("27113001").setDisplay("Body Weight");

                Quantity quantity = new Quantity();
                quantity
                        .setValue(new BigDecimal(theRecord.get("Weight")))
                        .setUnit("kg")
                        .setCode("kg")
                        .setSystem(CareConnectSystem.UnitOfMeasure);
                observation.setValue(quantity);

                resources.add(observation);

            }
            if (!theRecord.get("FatMass").isEmpty()) {
                Observation  fatMassobservation = newBasicObservation(theRecord.get("Date"),"365605003","Body measurement finding",CareConnectSystem.SNOMEDCT);

                fatMassobservation.addIdentifier()
                        .setSystem(nokiaObs)
                        .setValue("LD"+stripChar(theRecord.get("Date")));
                CodeableConcept code =fatMassobservation.getCode();

                code.addCoding().setSystem(CareConnectSystem.SNOMEDCT).setCode("248363008").setDisplay("Fat-free mass");

                Quantity quantity= new Quantity();
                quantity
                        .setValue(new BigDecimal(theRecord.get("FatMass")))
                        .setUnit("kg")
                        .setCode("kg")
                        .setSystem(CareConnectSystem.UnitOfMeasure);
                fatMassobservation.setValue(quantity);

                resources.add(fatMassobservation);
            }

        }

    }

    public class ActivitiesHandler implements IRecordHandler {
        @Override
        public void accept(CSVRecord theRecord) {
            if (!theRecord.get("Active calories").isEmpty()) {

                Observation observation = newBasicObservation(theRecord.get("Date"), "therapy","Therapy","http://hl7.org/fhir/observation-category");
                observation.addIdentifier()
                        .setSystem(nokiaObs)
                        .setValue("AT"+stripChar(theRecord.get("Date")));
                CodeableConcept code = observation.getCode();
                code.addCoding().setSystem(CareConnectSystem.LOINC).setCode("41981-2").setDisplay("Calories burned");

                Quantity quantity = new Quantity();
                quantity
                        .setValue(new BigDecimal(theRecord.get("Active calories")))
                        .setUnit("cal")
                        .setCode("cal")
                        .setSystem(CareConnectSystem.UnitOfMeasure);
                observation.setValue(quantity);
               // System.out.println(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(observation));
                resources.add(observation);

            }
            if (!theRecord.get("Steps").isEmpty()) {
                Observation stepobservation = newBasicObservation(theRecord.get("Date"), "therapy","Therapy","http://hl7.org/fhir/observation-category");

                stepobservation.addIdentifier()
                        .setSystem(nokiaObs)
                        .setValue("SP"+stripChar(theRecord.get("Date")));
                CodeableConcept code =stepobservation.getCode();

                code.addCoding().setSystem(CareConnectSystem.LOINC).setCode("41950-7").setDisplay("Number of steps in 24 hour Measured");

                Quantity quantity= new Quantity();
                quantity
                        .setValue(new BigDecimal(theRecord.get("Steps")))
                        .setUnit("/d")
                        .setCode("/d")
                        .setSystem(CareConnectSystem.UnitOfMeasure);
                stepobservation.setValue(quantity);
             //   System.out.println(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(stepobservation));
                resources.add(stepobservation);
            }

        }

    }


    private Observation newBasicObservation2(String dateString, String categoryCode, String categoryDesc, String categorySystem) {
        Observation observation = null;
        Date date = null;
        try {
            SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss a");
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            // turn off set linient
            date = format.parse(dateString);
        } catch (Exception e) {
            try {
              //  System.out.println(dateString);
                SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
                format.setTimeZone(TimeZone.getTimeZone("UTC"));
                date = format.parse(dateString);
            } catch (Exception e2) {
                e.printStackTrace();
            }
        }

        observation = new Observation();
        observation.setMeta(new Meta().addProfile(CareConnectProfile.Observation_1));
        observation.setStatus(Observation.ObservationStatus.FINAL);
        CodeableConcept category = observation.addCategory();
        category.addCoding().setSystem(categorySystem).setCode(categoryCode).setDisplay(categoryDesc);

        observation.setEffective(new DateTimeType(date));


        return observation;

    }

    private Observation newBasicObservation(String dateString, String categoryCode, String categoryDesc, String categorySystem) {
        Observation observation = null;

        observation = new Observation();
        Date date = null;
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            date = format.parse(dateString);
        }
        catch (Exception e) {
                try {
                    //  System.out.println(dateString);
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    format.setTimeZone(TimeZone.getTimeZone("UTC"));
                    date = format.parse(dateString);
                } catch (Exception e2) {
                    e.printStackTrace();
                }
            }


        observation.setMeta(new Meta().addProfile(CareConnectProfile.Observation_1));
        observation.setStatus(Observation.ObservationStatus.FINAL);
        CodeableConcept category = observation.addCategory();
        category.addCoding().setSystem(categorySystem).setCode(categoryCode).setDisplay(categoryDesc);

        observation.setSubject(new Reference("Patient/2"));

        observation.setEffective(new DateTimeType(date));

        observation.getPerformer().add(new Reference("Patient/2"));

        return observation;

    }



}

