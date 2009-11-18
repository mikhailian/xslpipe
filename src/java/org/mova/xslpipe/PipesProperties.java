package org.mova.xslpipe;

import java.io.IOException;
import java.io.InputStream;

/** 
 * This class configures the pipes available for the application in
 * the form "pipename = file1.xsl file2.xsl file3.xsl".
 * TODO hot properties http://www.javaworld.com/javaworld/javatips/jw-javatip125.html */
public class PipesProperties extends java.util.Properties
{
	public PipesProperties() throws PiperException
	{
		stream = getClass().getResourceAsStream(PIPE_PROPERTIES);
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
	
	public String[] getXSLsForPipe(String pipename)
	{
		String [] strings = ((String) this.getProperty(pipename, "")).split("\\s+");
		
		if (strings.length == 1 && strings[0].equals(""))
			return new String[0]; //
		else 
			return strings;
	}
	
	public static PipesProperties getInstance() throws PiperException
	{
		if (instance == null)
		{
			synchronized (PipesProperties.class)
			{
				if (instance == null)
				{
					instance = new PipesProperties();
				}
			}
		}
		return instance;
	}
	
	private static final long serialVersionUID = -7359318853406979458L; /* auto-generated from eclipse */
	private static final String PIPE_PROPERTIES = "/pipes.properties";
	private static PipesProperties instance;
	private InputStream stream;
}
