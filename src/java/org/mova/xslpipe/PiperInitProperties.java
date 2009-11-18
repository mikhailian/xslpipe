package org.mova.xslpipe;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * This Properties file has only one entry -- "xslpath"
 * which is the absolute path to the xsl stylesheets 
 * TransformerImpl name may appear here in the future. */
public class PiperInitProperties extends Properties
{

	private PiperInitProperties() throws PiperException
	{
		stream = getClass().getResourceAsStream(PIPERINIT_PROPERTIES);
		try
		{
			this.load(stream);
		}
		catch (IOException ioe)
		{
			PiperException pe = new PiperException();
			pe.initCause(ioe);
			throw pe;
		}
	}
	

	public String getXSLPath()
	{
		return this.getProperty("xslpath", "");
	}
	
	public boolean skipDtd()
	{
		return new Boolean(this.getProperty("skipdtd")).booleanValue();
	}

	public boolean skipXsd()
	{
		return new Boolean(this.getProperty("skipxsd")).booleanValue();
	}

	public static PiperInitProperties getInstance() throws PiperException
	{
		if (instance == null)
		{
			synchronized (PiperInitProperties.class)
			{
				if (instance == null)
				{
					instance = new PiperInitProperties();
				}
			}
		}
		return instance;
	}
	private static final long serialVersionUID = 4163283938856696162L;
	private static final String PIPERINIT_PROPERTIES = "/piperinit.properties";
	private static PiperInitProperties instance;
	private InputStream stream;
	
}


