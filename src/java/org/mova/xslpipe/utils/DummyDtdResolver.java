/**
 * allows to ignore DTD and XSD declarations in an XML document
 */
package org.mova.xslpipe.utils;

import java.io.StringReader;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * Allows to ignore DTD declarations in an XML document. Very useful in
 * conjunction with Piper. Use like 
 * <code>piper.process(baisProtocol,baos,"getDSS", new DummyDtdResolver()); </code>
 * @author sagbmik */
public class DummyDtdResolver implements EntityResolver
{

	public InputSource resolveEntity(String publicId, String systemId)
	{
		if (systemId != null && (systemId.endsWith(".dtd") || systemId.endsWith(".DTD")))
		{
			return new InputSource(new StringReader(""));
		}
		else
			return null;
	}
}