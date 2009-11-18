package org.mova.xslpipe.utils;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author sagbmik
 * This class throws an exception on a error as well as on fatalError.
 * The behaviour of DefaultHandler is to throw nothing on error and
 * an exception or fatalError
 * */
public class StrictErrorHandler extends DefaultHandler
{
	
	public void error(SAXParseException e) throws SAXException
	{
		throw e;
	}
}
