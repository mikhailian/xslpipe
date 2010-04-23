package org.mova.xslpipe;


public class PiperException extends Exception 
{

	public PiperException(String msg)
	{
		super(msg);
	}

	public PiperException(String msg, Throwable ex)
	{
		super(msg,ex);
	}
	
	public PiperException(Throwable arg0) {
		super(arg0);
	}

	public PiperException()
	{
		super();
	}
    
 

	private static final long serialVersionUID = 6004415884502170384L;
}

