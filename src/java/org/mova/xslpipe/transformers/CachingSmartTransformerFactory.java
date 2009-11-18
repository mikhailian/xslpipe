/**
 * $Id: CachingSmartTransformerFactory.java,v 1.2 2005/01/12 17:18:56 mik Exp $
 */
package org.mova.xslpipe.transformers;

import java.io.File;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;

import org.apache.xalan.xsltc.trax.SmartTransformerFactoryImpl;
import org.xml.sax.XMLFilter;

/**
 * Caching implementation of JAXP transformer factory. This class extends
 * org.apache.xalan.xsltc.trax.SmartTransformerFactoryImpl according to
 * http://xml.apache.org/xalan-j/xsltc_usage.html#api The caching
 * implementation is delegated to TransformerCache
 * org.mova.xslpipe.transformers.TransformerCache
 */
public class CachingSmartTransformerFactory extends SmartTransformerFactoryImpl
{
	private TransformerCache transformerCache;

	public CachingSmartTransformerFactory()
	{
		super();
		transformerCache = new TransformerCache(this);
	}

	protected XMLFilter newXMLFilter(final File file) throws TransformerConfigurationException
	{
		return transformerCache.newXMLFilter(file);
	}

	public XMLFilter newXMLFilter(Source source) throws TransformerConfigurationException
	{
		return transformerCache.newXMLFilter(source);
	}

	public Transformer newTransformer(Source source) throws TransformerConfigurationException
	{
		return transformerCache.newTransformer(source);
	}
	
	public Transformer newTransformer(final File file) throws TransformerConfigurationException
	{
		return transformerCache.newTransformer(file);
	}
	
}