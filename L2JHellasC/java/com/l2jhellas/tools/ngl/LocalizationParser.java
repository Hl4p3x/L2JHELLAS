package com.l2jhellas.tools.ngl;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class LocalizationParser
{
	private String LANGUAGES_DIRECTORY = "../languages/";
	private final Map<String, String> _msgMap = new HashMap<>();
	private static final Logger _log = Logger.getLogger(LocalizationParser.class.getName());
	private final String _baseName;
	
	public LocalizationParser(String dir, String baseName, Locale locale)
	{
		LANGUAGES_DIRECTORY += dir + "/";
		_baseName = baseName;
		
		String language = locale.getLanguage();
		// String script = locale.getScript();
		String country = locale.getCountry();
		String variant = locale.getVariant();
		
		StringBuilder sb = new StringBuilder();
		sb.append(language);
		if (!country.isEmpty())
		{
			sb.append(country);
		}
		if (!variant.isEmpty())
		{
			sb.append('_' + variant);
			// Java 7 Function
			
		}
		
		File xml = getTranslationFile(sb.toString());
		parseXml(xml);
	}
	
	public LocalizationParser(String dir, String baseName, String locale)
	{
		LANGUAGES_DIRECTORY += dir + "/";
		_baseName = baseName;
		File xml = getTranslationFile(locale);
		parseXml(xml);
	}
	
	private void parseXml(File xml)
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);
		Document doc = null;
		
		if (xml.exists())
		{
			try
			{
				doc = factory.newDocumentBuilder().parse(xml);
			}
			catch (Exception e)
			{
				_log.warning(LocalizationParser.class.getSimpleName() + ": Could not load localization file");
				return;
			}
			
			Node n = doc.getFirstChild();
			NamedNodeMap docAttr = n.getAttributes();
			if (docAttr.getNamedItem("extends") != null)
			{
				String baseLang = docAttr.getNamedItem("extends").getNodeValue();
				parseXml(getTranslationFile(baseLang));
			}
			for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
			{
				if (d.getNodeName().equals("message"))
				{
					NamedNodeMap attrs = d.getAttributes();
					String id = attrs.getNamedItem("id").getNodeValue();
					String text = attrs.getNamedItem("text").getNodeValue();
					_msgMap.put(id, text);
				}
			}
		}
	}
	
	private File getTranslationFile(String language)
	{
		File xml = null;
		if (language.length() > 0)
		{
			xml = new File(LANGUAGES_DIRECTORY + _baseName + '_' + language + ".xml");
		}
		
		if ((language.length() > 2) && ((xml == null) || !xml.exists()))
		{
			xml = new File(LANGUAGES_DIRECTORY + _baseName + '_' + language.substring(0, 2) + ".xml");
		}
		
		if ((xml == null) || !xml.exists())
		{
			xml = new File(LANGUAGES_DIRECTORY + _baseName + ".xml");
		}
		return xml;
	}
	
	protected String getStringFromId(String id)
	{
		return _msgMap.get(id);
	}
}