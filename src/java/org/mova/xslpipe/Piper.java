package org.mova.xslpipe;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;

import org.apache.log4j.Logger;
import org.apache.xml.serializer.OutputPropertiesFactory;
import org.apache.xml.serializer.Serializer;
import org.apache.xml.serializer.SerializerFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.sun.java.browser.dom.DOMUnsupportedException;

/**
 * This is the main class that manages xslpipe in the applicaition.
 * 
 * To configure the application, use piperinit.properties in the classpath. To
 * configure the pipes, use pipes.properties in the classpath. See for the
 * examples in the $PROJECT_ROOT/etc directory
 * 
 * To use, just instantiate it via the Piper#getInstance() and run
 * Piper#process() with it. Caching is implemented <a
 * href="http://www.javaworld.com/javaworld/jw-05-2003/jw-0502-xsl.html"> as
 * suggested by Alexey Valikov </a>
 * 
 * Compiling transformers are implemented on top of <a
 * href="http://xml.apache.org/xalan-j/xsltc_usage.html#api">Xalan-J/XSLTC </a>
 * 
 * Currently, there are three implementations:
 * <ul>
 * <li>org.mova.xslpipe.CachingTransformerFactory extends the
 * interpretive xalan processor</li>
 * <li>org.mova.xslpipe.CachingSmartTransformerFactory switches
 * between interpretive and compiled xalan to maintain the maximum compatibility
 * </li>
 * <li>org.mova.xslpipe.CachingCompiledTransformerFactory extends
 * compiled xalan processor</li>
 * </ul>
 * 
 * To configure an alternative TransformerFactory in your application, add the
 * class name of the custom TransformerFactory implementation to the file
 * META-INF/services/javax.xml.transform.TransformerFactory
 * 
 * If you get a runtime exception like
 * 
 * <code>java.lang.IllegalAccessError: class org.apache.xml.dtm.ref.sax2dtm.SAX2DTM2$AncestorIterator cannot access its superclass org.apache.xml.dtm.ref.DTMDefaultBaseIterators$InternalAxisIteratorBase</code>
 *
 * then you need to upgrade your xalan. Use
 * 
 * <code>-Djava.endorsed.dirs=$XSLPIPE_HOME\lib</code> or some other means to preload
 *      the new version of xalan before the one used by jdk.
 */
public class Piper
{
	/**
	 * You probably need the singleton rather than the constructor
	 * 
	 * @see Piper#getInstance()
	 */
	public Piper() throws PiperException, DOMUnsupportedException
	{
		
		logger = Logger.getLogger(this.getClass());
		piperInit = PiperInitProperties.getInstance();
		pipes = PipesProperties.getInstance();
		// Instantiate a TransformerFactory.
		TransformerFactory tFactory = TransformerFactory.newInstance();
		// Determine whether the TransformerFactory supports The use uf SAXSource
		// and SAXResult
		if (!(tFactory.getFeature(SAXSource.FEATURE) && tFactory.getFeature(SAXResult.FEATURE)))
			throw new PiperException("The factory in use does not support SAXSource.FEATURE");
		// Cast the TransformerFactory to SAXTransformerFactory.
		factory = ((SAXTransformerFactory) tFactory);

		if (logger.getEffectiveLevel() == org.apache.log4j.Level.DEBUG)
			System.getProperties().list(System.err);
		
	}

	/**
	 * This is used to launch a named xslt chain. 
	 * @param input
	 *          input stream
	 * @param output
	 *          output stream
	 * @param pipename
	 *          the name of the pipe as defined in
	 * @see org.mova.xslpipe.PipesProperties
	 */
	public void process(InputStream input, OutputStream output, String pipename) throws PiperException
	{
		process(input, output, pipename, null,null,null,null);
	}

	
	/**
	 * This is used to launch a named xslt chain. 
	 * @param input
	 *          input stream
	 * @param output
	 *          output stream
	 * @param pipename
	 *          the name of the pipe as defined in
	 * @param resolver
	 *          custom EntityResolver
	 * @see org.mova.xslpipe.PipesProperties
	 */
	public void process(InputStream input, OutputStream output, String pipename, EntityResolver resolver)
			throws PiperException
			{
				process(input,output,pipename,resolver,null,null,null);
			}

    /**
     * This is used to launch a named xslt chain. 
     * @param input
     *          input stream
     * @param output
     *          output stream
     * @param pipename
     *          the name of the pipe as defined in
     * @param resolver
     *          custom EntityResolver
     * @see org.mova.xslpipe.PipesProperties
     */
    public void process(InputStream input, OutputStream output, String pipename, EntityResolver resolver,URIResolver uriresolver)
            throws PiperException
            {
                process(input,output,pipename,resolver,null,null,uriresolver);
            }
    
	/**
	 * This is used to launch a named xslt chain. 
	 * @param input
	 *          input stream
	 * @param output
	 *          output stream
	 * @param pipename
	 *          the name of the pipe as defined in
	 * @param params
	 * 					contans String/Object mappings that will be used as xslt params
	 */	
	public void process(InputStream input,OutputStream output, String pipename, XSLParameters params)
			throws PiperException
			{
				process(input,output,pipename,null,params, null,null);
			}
	
	/**
	 * This is used to launch a named xslt chain. 
	 * @param input
	 *          input stream
	 * @param output
	 *          output stream
	 * @param pipename
	 *          the name of the pipe as defined in
	 * @param resolver
	 *          custom EntityResolver
	 * @param params
	 * 					contans String/Object mappings that will be used as xslt params
	 */	
	public void process(InputStream input,OutputStream output, String pipename, 
			EntityResolver resolver, XSLParameters params)
			throws PiperException
			{
				process(input,output,pipename,resolver,params, null,null);
			}

	/**
	 * This is used to launch a named xslt chain. 
	 * @param input
	 *          input stream
	 * @param output
	 *          output stream
	 * @param pipename
	 *          the name of the pipe as defined in
	 * @param resolver
	 *          custom EntityResolver
	 * @param reader 
	 *         	contains a custom XMLReader for validation configuration
	 */	
	public void process(InputStream input,OutputStream output, String pipename, 
			EntityResolver resolver, XMLReader reader)
			throws PiperException
			{
				process(input,output,pipename,resolver,null, reader,null);
			}
	
	/**
	 * This is used to launch a named xslt chain.
	 * @param input
	 *          input stream
	 * @param output
	 *          output stream
	 * @param pipename
	 *          the name of the pipe as defined in
	 * @param reader 
	 *         	contains String/Object mappings that will be used to configure the XMLReader
	 * 
	 * @see org.mova.xslpipe.PipesProperties
	 */
	public void process(InputStream input,OutputStream output, String pipename, 
			XMLReader reader)
			throws PiperException
			{
				process(input,output,pipename,null,null, reader,null);		
			}

	/**
	 * This is used to launch a named xslt chain. Since the factory can be a
	 * caching one, the xslts are only read once, so do not worry about multiple
	 * filter instantiations in the code
	 * 
	 * @param input
	 *          input stream
	 * @param output
	 *          output stream
	 * @param pipename
	 *          the name of the pipe as defined in
	 * @param entityResolver
	 *          custom EntityResolver
	 * @param params
	 * 					contans String/Object mappings that will be used as xslt params
	 * @param reader 
	 *         	contains a custom XMLReader for validation configuration
	 * 
	 * @see org.mova.xslpipe.PipesProperties
	 */
	public void process(InputStream input,OutputStream output, String pipename, 
			EntityResolver entityResolver, XSLParameters params, XMLReader reader)
			throws PiperException
	{
		process(input,output,pipename,null,null, reader,null);
	}
	
	/**
	 * This is used to launch a named xslt chain. Since the factory can be a
	 * caching one, the xslts are only read once, so do not worry about multiple
	 * filter instantiations in the code
	 * 
	 * @param input
	 *          input stream
	 * @param output
	 *          output stream
	 * @param pipename
	 *          the name of the pipe as defined in
	 * @param entityResolver
	 *          custom EntityResolver
	 * @param params
	 * 					contans String/Object mappings that will be used as xslt params
	 * @param reader 
	 *         	contains a custom XMLReader for validation configuration
     *
     * @param uriResolver 
     *          contains a custom URIResolver for accessing files in war, jars, ear, whatever...
	 * 
	 * @see org.mova.xslpipe.PipesProperties
	 */
	@SuppressWarnings("unchecked")
	public void process(InputStream input,OutputStream output, String pipename, 
			EntityResolver entityResolver, XSLParameters params, XMLReader reader,URIResolver uriResolver)
			throws PiperException
	{		
		Vector chain = new Vector();
		try
		{			
            /* setting a custom factory URI resolver */
            if (uriResolver != null)
               factory.setURIResolver(uriResolver);
			// Create an XMLReader if not created yet
			if (reader == null)
				reader = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
			if (entityResolver != null)
				reader.setEntityResolver(entityResolver);
			Serializer serializer = SerializerFactory
					.getSerializer(OutputPropertiesFactory.getDefaultMethodProperties("xml"));
			serializer.setOutputStream(output);
			// read in xslts
			String[] xslts;
			xslts = pipes.getXSLsForPipe(pipename);
			if (xslts != null && xslts.length == 0)
				logger.warn("no XSLs for the pipe " + pipename);
            

            
			for (int i = 0; i < xslts.length; i++)
			{
				Source source = uriResolver.resolve( piperInit.getXSLPath() + xslts[i],null);
				TransformerHandler th = factory.newTransformerHandler(source);
				if (params != null)
				{
					/* setting XSL params*/
					for (Iterator it=params.entrySet().iterator(); it.hasNext();)
					{
						Map.Entry entry = (Map.Entry)it.next();
						th.getTransformer().setParameter((String)entry.getKey(),entry.getValue());
					}
					/* setting a custom URI resolver */
					if (uriResolver != null)
						th.getTransformer().setURIResolver(uriResolver);
				}
				chain.add(th);
			}
			/*
			 * assign every next filter reads from the previous one, the first reader
			 * reads from the XMLReader
			 */
			Iterator it = chain.iterator();
			if (it.hasNext()) // get first
			{
				TransformerHandler th = (TransformerHandler) it.next();
				reader.setContentHandler(th);
				reader.setProperty("http://xml.org/sax/properties/lexical-handler", th);
				while (it.hasNext()) // get next
				{
					TransformerHandler thnext = (TransformerHandler) it.next();
					th.setResult(new SAXResult(thnext));
					th = thnext;					
				}
				/* assign a content handler to the last filter in the chain */
				th.setResult(new SAXResult(serializer.asContentHandler()));				
			}
			else
			{
				TransformerHandler th = factory.newTransformerHandler();
				reader.setContentHandler(th);
				reader.setProperty("http://xml.org/sax/properties/lexical-handler", th);
				th.setResult(new SAXResult(serializer.asContentHandler()));
			}
			logger.debug("processing the pipe " + pipename + "...");
            reader.parse(new InputSource(input));			
			logger.debug("finished.");
		}
		catch (Exception tce)
		{
			PiperException pe = new PiperException(tce.getMessage(), tce);			
			throw pe;
		}
	}

	/** This is the preferred method of accessing the Piper */
	public static Piper getInstance() throws PiperException, DOMUnsupportedException
	{
		if (instance == null)
		{
			synchronized (PiperInitProperties.class)
			{
				if (instance == null)
				{
					instance = new Piper();
				}
			}
		}
		return instance;
	}

	public static void main(String[] args)
	{
		try
		{
			if (args.length != 2)
			{
				System.err.println("Usage: Piper inputfile outputfile");
				System.exit(1);
			}
			/* this for a single run */
			Piper piper = new Piper();
			piper.process(new FileInputStream(new File(args[0])), new FileOutputStream(new File(args[1])), "createDSS");
			/*
			 * Launch each test at once, dynamically changing the TransformerFactory
			 * does not always work well
			 */
			speedtest(args,"org.apache.xalan.processor.TransformerFactoryImpl");
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

	/**
	 * The speedtest function allows to run the same file many times throough the
	 * cached stylesheets
	 */
	private static void speedtest(String[] args, String factory) throws PiperException, FileNotFoundException,
			DOMUnsupportedException
	{
		System.setProperty("javax.xml.transform.TransformerFactory", factory);
		Piper piper = Piper.getInstance();
		logger.info("using " + piper.factory.getClass().getName());
		Date d1 = new Date();
		for (int i = 0; i < 30; i++)
		{
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
			piper.process(new FileInputStream(new File(args[0])), new FileOutputStream(new File(args[1])), "speedtest");
		}
		Date d2 = new Date();
		long min = (d2.getTime() - d1.getTime()) / (1000 * 60);
		long sec = ((d2.getTime() - d1.getTime()) / (1000)) % 60;
		logger.info("passed: " + min + " minutes " + sec + " seconds");
	}

	protected PipesProperties pipes;

	protected PiperInitProperties piperInit;

	protected SAXTransformerFactory factory;

	//private Vector chain;

	protected static Logger logger = Logger.getLogger(Piper.class);

	private static Piper instance;

  public static final String VALIDATION = 
  	"http://xml.org/sax/features/validation";
  public static final String VALIDATION_DYNAMIC =
  	"http://apache.org/xml/features/validation/dynamic";
  public static final String VALIDATION_SCHEMA =
  	"http://apache.org/xml/features/validation/schema";

  public static final String VALIDATION_NAMESPACES =
  	"http://xml.org/sax/features/namespaces";

  public static final String NO_NS_SCHEMA_LOCATION = 
  	"http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation";
  public static final String NS_SCHEMA_LOCATION =
  	"http://apache.org/xml/properties/schema/external-schemaLocation";
  public static final String JAXP_SCHEMA_SOURCE =
     "http://java.sun.com/xml/jaxp/properties/schemaSource"; 
  }
