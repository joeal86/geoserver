/* (c) 2017 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.catalog.rest;

import java.io.IOException;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Component;

@Component
public class SimpleFeatureCollectionConverter extends FeatureCollectionConverter<SimpleFeatureCollection> {

    public SimpleFeatureCollectionConverter(){
        super(MediaType.APPLICATION_JSON, CatalogController.MEDIATYPE_TEXT_JSON,
                MediaType.APPLICATION_XML);
    }
    
    /**
     * Access features, unwrapping if necessary.
     * @param o
     * @return features
     */
    protected SimpleFeatureCollection getFeatures(SimpleFeatureCollection content){
        return content;
    }

    @Override
    protected void writeInternal(SimpleFeatureCollection content, HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {
        MediaType mediaType = outputMessage.getHeaders().getContentType();
        if (MediaType.APPLICATION_JSON.includes(mediaType)
                || CatalogController.MEDIATYPE_TEXT_JSON.includes(mediaType)) {
            writeGeoJsonl(content, outputMessage);
        }
        else if (MediaType.APPLICATION_XML.includes(mediaType)) {
            writeGML(content, outputMessage);
        } 
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return SimpleFeatureCollection.class.isAssignableFrom(clazz);
    }

}
