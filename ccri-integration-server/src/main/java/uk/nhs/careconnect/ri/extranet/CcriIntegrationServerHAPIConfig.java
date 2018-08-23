package uk.nhs.careconnect.ri.extranet;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.server.FifoMemoryPagingProvider;
import ca.uhn.fhir.rest.server.HardcodedServerAddressStrategy;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.IServerInterceptor;
import ca.uhn.fhir.rest.server.interceptor.ResponseHighlighterInterceptor;
import ca.uhn.fhir.util.VersionUtil;

import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.Location;
import org.hl7.fhir.dstu3.model.Organization;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import uk.nhs.careconnect.ri.extranet.providers.*;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.TimeZone;


public class CcriIntegrationServerHAPIConfig extends RestfulServer {

	private static final long serialVersionUID = 1L;
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CcriIntegrationServerHAPIConfig.class);

	private ApplicationContext applicationContext;

	CcriIntegrationServerHAPIConfig(ApplicationContext context) {
		this.applicationContext = context;
	}

	@Value("http://127.0.0.1/STU3")
	private String serverBase;

    @Value("${fhir.resource.serverName}")
    private String serverName;

    @Value("${fhir.resource.serverVersion}")
    private String serverVersion;


    @Override
	public void addHeadersToResponse(HttpServletResponse theHttpResponse) {
		theHttpResponse.addHeader("X-Powered-By", "HAPI FHIR " + VersionUtil.getVersion() + " RESTful Server (INTEROPen Care Connect STU3)");
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void initialize() throws ServletException {
		super.initialize();
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));


		FhirVersionEnum fhirVersion = FhirVersionEnum.DSTU3;
		setFhirContext(new FhirContext(fhirVersion));

	     if (serverBase != null && !serverBase.isEmpty()) {
            setServerAddressStrategy(new HardcodedServerAddressStrategy(serverBase));
        }

        if (applicationContext == null ) log.info("Context is null");

		setResourceProviders(Arrays.asList(
				applicationContext.getBean(EncounterExtranetProvider.class)
				,applicationContext.getBean(BundleExtranetProvider.class)
				,applicationContext.getBean(PatientExtranetProvider.class)
				,applicationContext.getBean(PractitionerExtranetProvider.class)
				,applicationContext.getBean(OrganizationExtranetProvider.class)
				,applicationContext.getBean(AllergyIntoleranceExtranetProvider.class)
				,applicationContext.getBean(CompositionExtranetProvider.class)
				,applicationContext.getBean(ConditionExtranetProvider.class)
				,applicationContext.getBean(DocumentReferenceExtranetProvider.class)
				,applicationContext.getBean(ImmunizationExtranetProvider.class)
				,applicationContext.getBean(LocationExtranetProvider.class)
				,applicationContext.getBean(MedicationExtranetProvider.class)
				,applicationContext.getBean(MedicationRequestExtranetProvider.class)
				,applicationContext.getBean(MedicationStatementExtranetProvider.class)
				,applicationContext.getBean(ObservationExtranetProvider.class)
				,applicationContext.getBean(ProcedureExtranetProvider.class)
				,applicationContext.getBean(CarePlanExtranetProvider.class)
		));

		// Replace built in conformance provider (CapabilityStatement)
		setServerConformanceProvider(new ConformanceExtranetProvider(applicationContext ));

        setServerName(serverName);
        setServerVersion(serverVersion);

		FifoMemoryPagingProvider pp = new FifoMemoryPagingProvider(10);
		pp.setDefaultPageSize(10);
		pp.setMaximumPageSize(100);
		setPagingProvider(pp);

		setDefaultPrettyPrint(true);
		setDefaultResponseEncoding(EncodingEnum.JSON);

		FhirContext ctx = getFhirContext();
		// Remove as believe due to issues on docker ctx.setNarrativeGenerator(new DefaultThymeleafNarrativeGenerator());
	}

	/**
	 * This interceptor adds some pretty syntax highlighting in responses when a browser is detected
	 */
	@Bean(autowire = Autowire.BY_TYPE)
	public IServerInterceptor responseHighlighterInterceptor() {
		ResponseHighlighterInterceptor retVal = new ResponseHighlighterInterceptor();
		return retVal;
	}



}
