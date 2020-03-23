package com.l2jhellas.gameserver.engines;

import java.io.File;
import java.io.FileFilter;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

import com.PackRoot;
import com.l2jhellas.util.filters.file.XMLFilter;

public interface DocumentParser
{
	static final Logger LOG = Logger.getLogger(DocumentParser.class.getName());
	
	static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
	static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
	
	static final XMLFilter XML_FILTER = new XMLFilter();
	
	public void load();
	
	default void parseDatapackFile(String path)
	{
		parseFile(new File(PackRoot.DATAPACK_ROOT, path));
	}
	
	default void parseFile(File f)
	{
		if (!getCurrentFileFilter().accept(f))
		{
			LOG.warning(getClass().getSimpleName() + ": Could not parse " + f.getName() + " is not a file or it doesn't exist!");
			return;
		}
		
		final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		dbf.setIgnoringComments(true);
		try
		{
			dbf.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
			final DocumentBuilder db = dbf.newDocumentBuilder();
			db.setErrorHandler(new XMLErrorHandler());
			parseDocument(db.parse(f), f);
		}
		catch (SAXParseException e)
		{
			LOG.warning(getClass().getSimpleName() + ": Could not parse file " + f.getName() + " at line " + e.getLineNumber() + ", column " + e.getColumnNumber());
			e.printStackTrace();
			return;
		}
		catch (Exception e)
		{
			LOG.warning(getClass().getSimpleName() + ": Could not parse file " + f.getName());
			e.printStackTrace();
			return;
		}
	}
	
	default boolean parseDirectory(File file)
	{
		return parseDirectory(file, false);
	}
	
	default boolean parseDirectory(String path)
	{
		return parseDirectory(new File(path), false);
	}
	
	default boolean parseDirectory(String path, boolean recursive)
	{
		return parseDirectory(new File(path), recursive);
	}
	
	default boolean parseDirectory(File dir, boolean recursive)
	{
		if (!dir.exists())
		{
			LOG.warning(getClass().getSimpleName() + ": Folder " + dir.getAbsolutePath() + " doesn't exist!");
			return false;
		}
		
		final File[] files = dir.listFiles();
		if (files != null)
		{
			for (File f : files)
			{
				if (recursive && f.isDirectory())
				{
					parseDirectory(f, recursive);
				}
				else if (getCurrentFileFilter().accept(f))
				{
					parseFile(f);
				}
			}
		}
		return true;
	}
	
	default boolean parseDatapackDirectory(String path, boolean recursive)
	{
		return parseDirectory(new File(PackRoot.DATAPACK_ROOT, path), recursive);
	}
	
	default void parseDocument(Document doc, File f)
	{
		parseDocument(doc);
	}
	
	default void parseDocument(Document doc)
	{
		LOG.severe(getClass().getSimpleName() + ": Parser not implemented!");
	}
	
	default Boolean parseBoolean(Node node, Boolean defaultValue)
	{
		return node != null ? Boolean.valueOf(node.getNodeValue()) : defaultValue;
	}
	
	default Boolean parseBoolean(Node node)
	{
		return parseBoolean(node, null);
	}
	
	default Boolean parseBoolean(NamedNodeMap attrs, String name)
	{
		return parseBoolean(attrs.getNamedItem(name));
	}
	
	default Boolean parseBoolean(NamedNodeMap attrs, String name, Boolean defaultValue)
	{
		return parseBoolean(attrs.getNamedItem(name), defaultValue);
	}
	
	default Byte parseByte(Node node, Byte defaultValue)
	{
		return node != null ? Byte.valueOf(node.getNodeValue()) : defaultValue;
	}
	
	default Byte parseByte(Node node)
	{
		return parseByte(node, null);
	}
	
	default Byte parseByte(NamedNodeMap attrs, String name)
	{
		return parseByte(attrs.getNamedItem(name));
	}
	
	default Byte parseByte(NamedNodeMap attrs, String name, Byte defaultValue)
	{
		return parseByte(attrs.getNamedItem(name), defaultValue);
	}
	
	default Short parseShort(Node node, Short defaultValue)
	{
		return node != null ? Short.valueOf(node.getNodeValue()) : defaultValue;
	}
	
	default Short parseShort(Node node)
	{
		return parseShort(node, null);
	}
	
	default Short parseShort(NamedNodeMap attrs, String name)
	{
		return parseShort(attrs.getNamedItem(name));
	}
	
	default Short parseShort(NamedNodeMap attrs, String name, Short defaultValue)
	{
		return parseShort(attrs.getNamedItem(name), defaultValue);
	}
	
	default int parseInt(Node node, Integer defaultValue)
	{
		return node != null ? Integer.parseInt(node.getNodeValue()) : defaultValue;
	}
	
	default int parseInt(Node node)
	{
		return parseInt(node, -1);
	}
	
	default Integer parseInteger(Node node, Integer defaultValue)
	{
		return node != null ? Integer.valueOf(node.getNodeValue()) : defaultValue;
	}
	
	default Integer parseInteger(Node node)
	{
		return parseInteger(node, null);
	}
	
	default Integer parseInteger(NamedNodeMap attrs, String name)
	{
		return parseInteger(attrs.getNamedItem(name));
	}
	
	default Integer parseInteger(NamedNodeMap attrs, String name, Integer defaultValue)
	{
		return parseInteger(attrs.getNamedItem(name), defaultValue);
	}
	
	default Long parseLong(Node node, Long defaultValue)
	{
		return node != null ? Long.valueOf(node.getNodeValue()) : defaultValue;
	}
	
	default Long parseLong(Node node)
	{
		return parseLong(node, null);
	}
	
	default Long parseLong(NamedNodeMap attrs, String name)
	{
		return parseLong(attrs.getNamedItem(name));
	}
	
	default Long parseLong(NamedNodeMap attrs, String name, Long defaultValue)
	{
		return parseLong(attrs.getNamedItem(name), defaultValue);
	}
	
	default Float parseFloat(Node node, Float defaultValue)
	{
		return node != null ? Float.valueOf(node.getNodeValue()) : defaultValue;
	}
	
	default Float parseFloat(Node node)
	{
		return parseFloat(node, null);
	}
	
	default Float parseFloat(NamedNodeMap attrs, String name)
	{
		return parseFloat(attrs.getNamedItem(name));
	}
	
	default Float parseFloat(NamedNodeMap attrs, String name, Float defaultValue)
	{
		return parseFloat(attrs.getNamedItem(name), defaultValue);
	}
	
	default Double parseDouble(Node node, Double defaultValue)
	{
		return node != null ? Double.valueOf(node.getNodeValue()) : defaultValue;
	}
	
	default Double parseDouble(Node node)
	{
		return parseDouble(node, null);
	}
	
	default Double parseDouble(NamedNodeMap attrs, String name)
	{
		return parseDouble(attrs.getNamedItem(name));
	}
	
	default Double parseDouble(NamedNodeMap attrs, String name, Double defaultValue)
	{
		return parseDouble(attrs.getNamedItem(name), defaultValue);
	}
	
	default String parseString(Node node, String defaultValue)
	{
		return node != null ? node.getNodeValue() : defaultValue;
	}
	
	default String parseString(Node node)
	{
		return parseString(node, null);
	}
	
	default String parseString(NamedNodeMap attrs, String name)
	{
		return parseString(attrs.getNamedItem(name));
	}
	
	default String parseString(NamedNodeMap attrs, String name, String defaultValue)
	{
		return parseString(attrs.getNamedItem(name), defaultValue);
	}
	
	default <T extends Enum<T>> T parseEnum(Node node, Class<T> clazz, T defaultValue)
	{
		if (node == null)
		{
			return defaultValue;
		}
		
		try
		{
			return Enum.valueOf(clazz, node.getNodeValue());
		}
		catch (IllegalArgumentException e)
		{
			LOG.warning("Invalid value specified for node: " + node.getNodeName() + " specified value: " + node.getNodeValue() + " should be enum value of " + clazz.getSimpleName() + " using default value: " + defaultValue);
			return defaultValue;
		}
	}
	
	default <T extends Enum<T>> T parseEnum(Node node, Class<T> clazz)
	{
		return parseEnum(node, clazz, null);
	}
	
	default <T extends Enum<T>> T parseEnum(NamedNodeMap attrs, Class<T> clazz, String name)
	{
		return parseEnum(attrs.getNamedItem(name), clazz);
	}
	
	default <T extends Enum<T>> T parseEnum(NamedNodeMap attrs, Class<T> clazz, String name, T defaultValue)
	{
		return parseEnum(attrs.getNamedItem(name), clazz, defaultValue);
	}
	
	default FileFilter getCurrentFileFilter()
	{
		return XML_FILTER;
	}
	
	static class XMLErrorHandler implements ErrorHandler
	{
		@Override
		public void warning(SAXParseException e) throws SAXParseException
		{
			throw e;
		}
		
		@Override
		public void error(SAXParseException e) throws SAXParseException
		{
			throw e;
		}
		
		@Override
		public void fatalError(SAXParseException e) throws SAXParseException
		{
			throw e;
		}
	}
}
