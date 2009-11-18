/**
 * Writes the OutputStream to /dev/null
 */
package org.mova.xslpipe.utils;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author sagbmik
 */
public class NullOutputStream extends OutputStream
{
	
	public void close() throws IOException
	{
	}
	public void flush() throws IOException
	{
	}
	public void write(byte[] arg0, int arg1, int arg2) throws IOException
	{
	}
	public void write(byte[] arg0) throws IOException
	{
	}
	public void write(int arg0) throws IOException
	{
	}
}
