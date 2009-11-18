package org.mova.xslpipe;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;

import org.apache.log4j.Logger;
import org.apache.xml.serializer.OutputPropertiesFactory;
import org.apache.xml.serializer.Serializer;
import org.apache.xml.serializer.SerializerFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.sun.java.browser.dom.DOMUnsupportedException;

/**
 * This is basically the same as Piper but uses XMLFilter and SAXTransformerFactory, 
 * so it can not set custom parameters to the XSL. 
 * @see Piper
*/
public class PiperFilter
{
	/** You probably need the singleton rather than the constructor 
	 * @see PiperFilter#getInstance() */
	
	TransformerFactory tFactory;
	
	public PiperFilter() throws PiperException, DOMUnsupportedException
	{
		logger = Logger.getLogger(this.getClass());
		piperInit = PiperInitProperties.getInstance();
		pipes = PipesProperties.getInstance();
		chain = new Vector();
    
		// Instantiate  a TransformerFactory.
	  tFactory = TransformerFactory.newInstance();
    
	  // Determine whether the TransformerFactory supports The use uf SAXSource and SAXResult		
    if (!(tFactory.getFeature(SAXSource.FEATURE) && tFactory.getFeature(SAXResult.FEATURE)))
    	throw new PiperException("The factory in use does not support SAXSource.FEATURE");

    // Cast the TransformerFactory to SAXTransformerFactory.
    saxTFactory = ((SAXTransformerFactory) tFactory);    
   
    
	}
	/** This is used to launch a named xslt chain. Since the factory can be
	 * a caching one, the xslts are only read once, so do not worry about 
	 * multiple filter instantiations in the code 
	 * @param input input stream
	 * @param output output stream
	 * @param pipename the name of the pipe as defined in 
	 * @see org.mova.xslpipe.PipesProperties */
	public void process(InputStream input,OutputStream output,String pipename)
	throws PiperException
	{
		process(input,output,pipename,null,null);
	}

	
	/** This is used to launch a named xslt chain. Since the factory can be
	 * a caching one, the xslts are only read once, so do not worry about 
	 * multiple filter instantiations in the code 
	 * @param input input stream
	 * @param output output stream
	 * @param pipename the name of the pipe as defined in 
	 * @param entityResolver custom EntityResolver
	 * @see org.mova.xslpipe.PipesProperties */
	@SuppressWarnings("unchecked")
	public void process(InputStream input,OutputStream output,String pipename,
			EntityResolver entityResolver,URIResolver uriResolver) 
	throws PiperException
	{		
		chain.removeAllElements();		
		try
		{
	    // Create an XMLReader.
			XMLReader reader = XMLReaderFactory.createXMLReader();
			// not sure if we can pass one instance of the resolver everywhere or recreate
			// it for every DocumentReader, so going the safe way here.
			if (entityResolver != null)
				reader.setEntityResolver(entityResolver); 
			if (uriResolver != null)
				saxTFactory.setURIResolver(uriResolver);
	    Serializer serializer = 
	    	SerializerFactory.getSerializer(
	    			OutputPropertiesFactory.getDefaultMethodProperties("xml"));        
	    serializer.setOutputStream(output);

			// read in xslts
		String[] xslts;
		xslts = pipes.getXSLsForPipe(pipename);

		if (xslts.length == 0)
			logger.warn("no XSLs for the pipe " + pipename);
		for (int i = 0; i < xslts.length; i++)
		{
			Source source = uriResolver.resolve( piperInit.getXSLPath() + xslts[i],null);
			XMLFilter xmlFilter = 
				saxTFactory.newXMLFilter(source);
			if (entityResolver != null)
				xmlFilter.setEntityResolver(entityResolver); 
			chain.add(xmlFilter);
		}
		
			/* assign every next filter reads from the previous one,
			the first reader reads from the XMLReader */
		Iterator it = chain.iterator();
		while (it.hasNext())
		{
			XMLFilter xmlFilter = (XMLFilter)it.next();
			xmlFilter.setParent(reader);
			reader = xmlFilter;
		}

			/* assign a content handler to the last filter in the chain */
			XMLFilter xmlFilter = (XMLFilter)chain.lastElement();
			xmlFilter.setContentHandler(serializer.asContentHandler());

			logger.debug("processing the pipe" + pipename + "...");			
			xmlFilter.parse(new InputSource(input));
			logger.debug("finished.");
		}
		catch (Exception tce)
		{
			PiperException pe = new PiperException();
			pe.initCause(tce);
			throw pe;
		}
	}

	/** This is the preferred method of accessing the Piper */
	public static PiperFilter getInstance()
	throws PiperException, DOMUnsupportedException 
	{
		if (instance == null)
		{
			synchronized (PiperInitProperties.class)
			{
				if (instance == null)
				{
					instance = new PiperFilter();
				}
			}
		}
		return instance;
	}
	
	public static void main(String[] args)
	{
		try
		{
      if (args.length != 2) {
        System.err.println("Usage: Piper inputfile outputfile");
        System.exit(1);
      }
      /* this for a single run */
			PiperFilter piper = new PiperFilter();
					piper.process(
							new FileInputStream(new File(args[0])), 
							new FileOutputStream(new File(args[1])), "createDSS");
      
    	/* Launch each test at once, dynamically changing the TransformerFactory 
    	 * does not always work well*/	
    //speedtest(args,"org.apache.xalan.processor.TransformerFactoryImpl");
  	//speedtest(args,"org.mova.xslpipe.transformers.CachingTransformerFactory");
  	//speedtest(args,"org.mova.xslpipe.transformers.CachingSmartTransformerFactory");
    //speedtest(args,"org.mova.xslpipe.transformers.CachingCompiledTransformerFactory");

		}
		catch (Exception e)
		{
			System.err.println("---message:-------------");
			System.err.println(e.getMessage());
			System.err.println("---stacktrace:----------");
			e.printStackTrace(System.err);
		}
		
	}
	
	/** The speedtest function allows to run the same file many times
	 * throough the cached stylesheets */	
	@SuppressWarnings("unused")
	private static void speedtest(String[] args, String factory)
	throws PiperException, FileNotFoundException, DOMUnsupportedException
	{
		System.setProperty("javax.xml.transform.TransformerFactory", factory);
		PiperFilter piper = PiperFilter.getInstance();
		logger.info("using " + piper.saxTFactory.getClass().getName());
		Date d1 = new Date();
		for (int i = 0; i < 30; i++)
		{
			/* This is useful if we want to test what happens at nth iteration */
			/*Date ind1;
			Date ind2;
			if (i == 0)
			{
				ind1 = new Date();
				piper.process(
					new FileInputStream(new File(args[0])), 
					new FileOutputStream(new File(args[1])), "speedtest");
					logger.info("first iteration took: " + ((new Date()).getTime() - ind1.getTime()) + "ms");
			}
			else if (i == 9)
			{
				ind1 = new Date();
				piper.process(
					new FileInputStream(new File(args[0])), 
					new FileOutputStream(new File(args[1])), "speedtest");
					logger.info("10th iteration took " + ((new Date()).getTime() - ind1.getTime()) + "ms");
			}
			else if (i == 19)
			{
				ind1 = new Date();
				piper.process(
					new FileInputStream(new File(args[0])), 
					new FileOutputStream(new File(args[1])), "speedtest");
					logger.info("20th iteration took " + ((new Date()).getTime() - ind1.getTime()) + "ms");
			}
			else if (i == 29)
			{
				ind1 = new Date();
				piper.process(
					new FileInputStream(new File(args[0])), 
					new FileOutputStream(new File(args[1])), "speedtest");
					logger.info("20th iteration took " + ((new Date()).getTime() - ind1.getTime()) + "ms");
			}
			else*/
				piper.process(
						new FileInputStream(new File(args[0])), 
						new FileOutputStream(new File(args[1])), "speedtest");

		}
		Date d2 = new Date();
		long min = (d2.getTime() - d1.getTime()) / (1000 * 60);
		long sec = ((d2.getTime() - d1.getTime()) / (1000)) % 60;
		logger.info("passed: " + min + " minutes " + sec + " seconds");
		
	}
		
	protected  PipesProperties pipes;
	protected  PiperInitProperties piperInit;
	protected  SAXTransformerFactory saxTFactory;
	private    Vector chain;
	protected static Logger logger = Logger.getLogger(PiperFilter.class);
	private static   PiperFilter instance;
	
}
