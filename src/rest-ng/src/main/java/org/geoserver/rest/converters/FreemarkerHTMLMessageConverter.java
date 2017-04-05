package org.geoserver.rest.converters;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.geoserver.platform.ExtensionPriority;
import org.geoserver.rest.RequestInfo;
import org.geoserver.rest.wrapper.RestWrapper;
import org.geotools.util.logging.Logging;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * Message converter for Freemarker-generated HTML output
 */
public class FreemarkerHTMLMessageConverter extends BaseMessageConverter<RestWrapper<?>> {

    private static final Logger LOGGER = Logging.getLogger("org.geoserver.restng.converters");

    /**
     * Encoding (null for default)
     */
    protected String encoding;

    public FreemarkerHTMLMessageConverter() {
        super(MediaType.TEXT_HTML);
    }

    public FreemarkerHTMLMessageConverter(String encoding) {
        this();
        this.encoding = encoding;
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return RestWrapper.class.isAssignableFrom(clazz);
    }
    
    @Override
    protected boolean canRead(MediaType mediaType) {
        return false; // reading not supported
    }

    @Override
    protected RestWrapper<?> readInternal(Class<? extends RestWrapper<?>> clazz,
            HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Write an given object to the given output message as HTML, invoked from {@link #write}.
     * @param wrapper The wrapped object write to the output message
     * @param outputMessage the HTTP output message to write to
     * @throws IOException in case of I/O errors
     * @throws HttpMessageNotWritableException in case of conversion errors
     */
    @Override
    protected void writeInternal(RestWrapper<?> wrapper, HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {
        MediaType contentType = outputMessage.getHeaders().getContentType();
        
        Writer templateWriter = null;
        wrapper.configureFreemarker(this);
        try {
            Object object = wrapper.getObject();
            Template template = wrapper.getTemplate();
            OutputStream outputStream = outputMessage.getBody();
            Charset charSet = contentType.getCharset();
            
            if (charSet != null) {
                templateWriter = new BufferedWriter(
                        new OutputStreamWriter(outputStream, charSet.name()));
            } else {
                templateWriter = new BufferedWriter(
                        new OutputStreamWriter(outputStream, template.getEncoding()));
            }

            template.process(object, templateWriter);
            templateWriter.flush();
        } catch (TemplateException te) {
            throw new IOException("Template processing error " + te.getMessage());
        } finally {
            if (templateWriter != null) {
                templateWriter.close();
            }
        }
    }

    public int getPriority() {
        // If no extension or content-type provided, return HTML;
        return ExtensionPriority.LOWEST - 1;
    }

    public List<URL> createCollectionLink(String link) {
        // TODO Auto-generated method stub
        try {
            String href = href(link);
            URL url2 = new URL(href);
            return (List<URL>) Collections.singletonList(url2);
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return new ArrayList<URL>();
    }

    protected String href(String link) {

        final RequestInfo pg = RequestInfo.get();
        String ext = "html";

        if (ext != null && ext.length() > 0)
            link = link + "." + ext;

        // encode as relative or absolute depending on the link type
        if (link.startsWith("/")) {
            // absolute, encode from "root"
            return pg.servletURI(link);
        } else {
            // encode as relative
            return pg.pageURI(link);
        }
    }
}
