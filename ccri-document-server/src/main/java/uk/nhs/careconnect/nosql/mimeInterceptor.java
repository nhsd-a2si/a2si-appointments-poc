package uk.nhs.careconnect.nosql;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.interceptor.InterceptorAdapter;

import org.apache.commons.io.IOUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.xhtmlrenderer.pdf.ITextRenderer;



import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class mimeInterceptor extends InterceptorAdapter {


    FhirContext ctx = FhirContext.forDstu3();
    private static final Logger log = LoggerFactory.getLogger(mimeInterceptor.class);

    @Override
    public boolean incomingRequestPostProcessed(RequestDetails theRequestDetails, HttpServletRequest theRequest,
                                                HttpServletResponse theResponse) {

        log.info("iR Content-Type = "+theRequestDetails.getHeader("Content-Type"));
        return true;
        }

    @Override
    public boolean outgoingResponse(RequestDetails theRequestDetails, IBaseResource resource, HttpServletRequest theServletRequest, HttpServletResponse response) {

        log.info("oR Content-Type = "+theRequestDetails.getHeader("Content-Type"));
        String contentType = theRequestDetails.getHeader("Content-Type");
        if (contentType != null) {
            if (contentType.equals("text/html")) {
                try {

                    response.setStatus(200);
                    response.setContentType("text/html");

                    performTransform(response.getOutputStream(), resource, "XML/DocumentToHTML.xslt");

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return false;
            } else if (contentType.equals("application/pdf")) {
                try {

                    response.setStatus(200);
                    response.setContentType("application/pdf");

                    // TODO Remove file and convert to plain streams

                    File fileHtml = File.createTempFile("pdf", ".tmp");
                    FileOutputStream fos = new FileOutputStream(fileHtml);
                    performTransform(fos, resource, "XML/DocumentToHTML.xslt");


                    String processedHtml = org.apache.commons.io.IOUtils.toString(new InputStreamReader(new FileInputStream(fileHtml), "UTF-8"));

                    ITextRenderer renderer = new ITextRenderer();
                    renderer.setDocumentFromString(processedHtml);
                    renderer.layout();
                    renderer.createPDF(response.getOutputStream(), false);
                    renderer.finishPDF();
                    fos.flush();

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return false;
            }
        }
        return true;
    }
    private ClassLoader getContextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }


    private void performTransform(OutputStream os, IBaseResource resource, String styleSheet) {

        // Input xml data file
        ClassLoader classLoader = getContextClassLoader();

        // Input xsl (stylesheet) file
        String xslInput = classLoader.getResource(styleSheet).getFile();

        // Set the property to use xalan processor
        System.setProperty("javax.xml.transform.TransformerFactory",
                "org.apache.xalan.processor.TransformerFactoryImpl");

        // try with resources
        try {
            InputStream xml = new ByteArrayInputStream(ctx.newXmlParser().encodeResourceToString(resource).getBytes(StandardCharsets.UTF_8));


            FileInputStream xsl = new FileInputStream(xslInput);

            // Instantiate a transformer factory
            TransformerFactory tFactory = TransformerFactory.newInstance();

            // Use the TransformerFactory to process the stylesheet source and produce a Transformer
            StreamSource styleSource = new StreamSource(xsl);
            Transformer transformer = tFactory.newTransformer(styleSource);

            // Use the transformer and perform the transformation
            StreamSource xmlSource = new StreamSource(xml);
            StreamResult result = new StreamResult(os);
            transformer.transform(xmlSource, result);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

}
