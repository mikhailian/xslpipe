package org.mova.xslpipe.utils;

import java.io.IOException;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class WarURIResolver extends org.xml.sax.helpers.DefaultHandler implements URIResolver, EntityResolver {

    public Source resolve(String href, String base) throws TransformerException {
        StreamSource stream = new StreamSource(this.getClass().getClassLoader().getResourceAsStream(href));
        if (stream == null || stream.getInputStream() == null)
            throw new TransformerException("can not resolve the file '" + href + "'");
        return stream;
    }

    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        InputSource source;
        try {
            source = localResolveEntity(publicId, systemId);
        } catch (SAXException saxe) {
            source = super.resolveEntity(publicId, systemId);
        } catch (IOException ioe) {
            source = super.resolveEntity(publicId, systemId);
        }
        return source;
    }

    protected InputSource localResolveEntity(String publicId, String systemId) throws SAXException, IOException {
        InputSource source = new InputSource(this.getClass().getClassLoader().getResourceAsStream(systemId));
        if (source == null || source.getByteStream() == null)
            throw new IOException("can not resolve the file '" + systemId + "'");
        return source;
    }
}
