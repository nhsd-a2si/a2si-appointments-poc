package uk.nhs.a2si.poc.fhirfacade;



import ca.uhn.fhir.context.FhirContext;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import uk.nhs.a2si.poc.fhirfacade.camel.interceptor.GatewayPostProcessor;
import uk.nhs.a2si.poc.fhirfacade.camel.interceptor.GatewayPreProcessor;

import java.io.InputStream;

@Component
public class CamelRoute extends RouteBuilder {

	@Autowired
	protected Environment env;

	@Value("${fhir.restserver.serverBase}")
	private String eprBase;


	@Value("${fhir.resource.serverBase}")
    private String hapiBase;

	
    @Override
    public void configure() 
    {

		GatewayPreProcessor camelProcessor = new GatewayPreProcessor();

		GatewayPostProcessor camelPostProcessor = new GatewayPostProcessor();

		FhirContext ctx = FhirContext.forDstu3();



		from("direct:FHIRValidate")
				.routeId("FHIR Validation")
				.process(camelProcessor) // Add in correlation Id if not present
				.to("direct:TIEServer");


		// Complex processing

		from("direct:FHIRBundleCollection")
				.routeId("Bundle Collection Queue")
				.process(camelProcessor) // Add in correlation Id if not present
				.wireTap("seda:FHIRBundleCollection");



	// Integration Server (TIE)

		from("direct:FHIREncounterDocument")
				.routeId("TIE Encounter")
				.to("direct:TIEServer");

		from("direct:FHIRCarePlanDocument")
				.routeId("TIE CarePlan")
				.to("direct:TIEServer");

		from("direct:FHIRPatientOperation")
				.routeId("TIE PatientOperation")
				.to("direct:TIEServer");


		// EPR Server

		// Simple processing - low level resource operations
		from("direct:FHIRPatient")
			.routeId("Gateway Patient")
				.to("direct:HAPIServer");

		from("direct:FHIRPractitioner")
				.routeId("Gateway Practitioner")
				.to("direct:HAPIServer");

        from("direct:FHIRPractitionerRole")
                .routeId("Gateway PractitionerRole")
                .to("direct:HAPIServer");

        from("direct:FHIROrganisation")
                .routeId("Gateway Organisation")
                .to("direct:HAPIServer");

        from("direct:FHIRLocation")
                .routeId("Gateway Location")
                .to("direct:HAPIServer");

		from("direct:FHIRObservation")
			.routeId("Gateway Observation")
			.to("direct:HAPIServer");

		from("direct:FHIREncounter")
				.routeId("Gateway Encounter")
				.to("direct:HAPIServer");

		from("direct:FHIRCondition")
				.routeId("Gateway Condition")
				.to("direct:HAPIServer");

		from("direct:FHIRProcedure")
				.routeId("Gateway Procedure")
				.to("direct:HAPIServer");

		from("direct:FHIRMedicationRequest")
				.routeId("Gateway MedicationRequest")
				.to("direct:HAPIServer");

		from("direct:FHIRMedication")
				.routeId("Gateway Medication")
				.to("direct:HAPIServer");

		from("direct:FHIRMedicationStatement")
				.routeId("Gateway MedicationStatement")
				.to("direct:HAPIServer");

		from("direct:FHIRImmunization")
				.routeId("Gateway Immunization")
				.to("direct:HAPIServer");

		from("direct:FHIREpisodeOfCare")
				.routeId("Gateway EpisodeOfCare")
				.to("direct:HAPIServer");

		from("direct:FHIRAllergyIntolerance")
				.routeId("Gateway AllergyIntolerance")
				.to("direct:HAPIServer");

		from("direct:FHIRCapabilityStatement")
				.routeId("Gateway CapabilityStatement")
				.to("direct:HAPIServer");


		from("direct:FHIRComposition")
				.routeId("Gateway Composition")
				.to("direct:HAPIServer");

		from("direct:FHIRDiagnosticReport")
				.routeId("Gateway DiagnosticReport")
				.to("direct:HAPIServer");

		from("direct:FHIRCarePlan")
				.routeId("Gateway CarePlan")
				.to("direct:HAPIServer");

		from("direct:FHIRDocumentReference")
				.routeId("Gateway DocumentReference")
				.to("direct:HAPIServer");

		from("direct:FHIRBinary")
			.routeId("Gateway Binary")
			.to("direct:EDMSServer");

		from("direct:FHIRReferralRequest")
				.routeId("Gateway ReferralRequest")
				.to("direct:HAPIServer");

		from("direct:FHIRHealthcareService")
				.routeId("Gateway HealthcareService")
				.to("direct:HAPIServer");

		from("direct:FHIREndpoint")
				.routeId("Gateway Endpoint")
				.to("direct:HAPIServer");

		from("direct:FHIRQuestionnaire")
				.routeId("Gateway Questionnaire")
				.to("direct:HAPIServer");

		from("direct:FHIRQuestionnaireResponse")
				.routeId("Gateway QuestionnaireResponse")
				.to("direct:HAPIServer");

		from("direct:FHIRList")
				.routeId("Gateway List")
				.to("direct:HAPIServer");

		from("direct:FHIRRelatedPerson")
				.routeId("Gateway RelatedPerson")
				.to("direct:HAPIServer");

		from("direct:FHIRCareTeam")
				.routeId("Gateway CareTeam")
				.to("direct:HAPIServer");

		from("direct:FHIRMedicationDispense")
				.routeId("Gateway MedicationDispense")
				.to("direct:HAPIServer");

		from("direct:FHIRGoal")
				.routeId("Gateway Goal")
				.to("direct:HAPIServer");

		from("direct:FHIRRiskAssessment")
				.routeId("Gateway RiskAssessment")
				.to("direct:HAPIServer");

		from("direct:FHIRClinicalImpression")
				.routeId("Gateway ClinicalImpression")
				.to("direct:HAPIServer");

		from("direct:FHIRConsent")
				.routeId("Gateway Consent")
				.to("direct:HAPIServer");

		from("direct:FHIRSchedule")
				.routeId("Gateway Schedule")
				.to("direct:HAPIServer");

		from("direct:FHIRSlot")
				.routeId("Gateway Slot")
				.to("direct:HAPIServer");

		from("direct:FHIRAppointment")
				.routeId("Gateway Appointment")
				.to("direct:HAPIServer");



		from("direct:HAPIServer")
            .routeId("EPR FHIR Server")
				.process(camelProcessor)
				.to("log:uk.nhs.careconnect.FHIRGateway.start?level=INFO&showHeaders=true&showExchangeId=true")
                .to(eprBase)
				.process(camelPostProcessor)
                .to("log:uk.nhs.careconnect.FHIRGateway.complete?level=INFO&showHeaders=true&showExchangeId=true")
				.convertBodyTo(InputStream.class);

    }
}
