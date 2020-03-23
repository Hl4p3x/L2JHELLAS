package com.l2jhellas.gameserver.datatables.xml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.PackRoot;
import com.l2jhellas.gameserver.controllers.RecipeController;
import com.l2jhellas.gameserver.model.L2RecipeInstance;
import com.l2jhellas.gameserver.model.L2RecipeList;

public class RecipeData extends RecipeController
{
	protected static final Logger _log = Logger.getLogger(RecipeData.class.getName());
	
	private final Map<Integer, L2RecipeList> _lists = new HashMap<>();
	
	private static RecipeData instance;
	
	public static RecipeData getInstance()
	{
		if (instance == null)
		{
			instance = new RecipeData();
		}
		
		return instance;
	}
	
	private RecipeData()
	{
		try
		{
			ParseXML();
		}
		catch (SAXException | IOException | ParserConfigurationException e)
		{
			e.printStackTrace();
		}
		_log.info("RecipeTable: Loaded " + _lists.size() + " recipes.");
	}
	
	private void ParseXML() throws SAXException, IOException, ParserConfigurationException
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);
		File file = new File(PackRoot.DATAPACK_ROOT, "data/xml/recipes.xml");
		if (file.exists())
		{
			Document doc = factory.newDocumentBuilder().parse(file);
			List<L2RecipeInstance> recipePartList = new ArrayList<>();
			
			for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
			{
				if ("list".equalsIgnoreCase(n.getNodeName()))
				{
					String recipeName;
					int id;
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						if ("item".equalsIgnoreCase(d.getNodeName()))
						{
							recipePartList.clear();
							NamedNodeMap attrs = d.getAttributes();
							Node att;
							att = attrs.getNamedItem("id");
							if (att == null)
							{
								_log.warning(RecipeData.class.getName() + ": Missing id for recipe item, skipping");
								continue;
							}
							id = Integer.parseInt(att.getNodeValue());
							
							att = attrs.getNamedItem("name");
							if (att == null)
							{
								_log.warning(RecipeData.class.getName() + ": Missing name for recipe item id: " + id + ", skipping");
								continue;
							}
							recipeName = att.getNodeValue();
							
							int recipeId = -1;
							int level = -1;
							boolean isDwarvenRecipe = true;
							int mpCost = -1;
							int successRate = -1;
							int prodId = -1;
							int count = -1;
							for (Node c = d.getFirstChild(); c != null; c = c.getNextSibling())
							{
								if ("recipe".equalsIgnoreCase(c.getNodeName()))
								{
									NamedNodeMap atts = c.getAttributes();
									
									recipeId = Integer.parseInt(atts.getNamedItem("id").getNodeValue());
									level = Integer.parseInt(atts.getNamedItem("level").getNodeValue());
									isDwarvenRecipe = atts.getNamedItem("type").getNodeValue().equalsIgnoreCase("dwarven");
								}
								else if ("mpCost".equalsIgnoreCase(c.getNodeName()))
								{
									mpCost = Integer.parseInt(c.getTextContent());
								}
								else if ("successRate".equalsIgnoreCase(c.getNodeName()))
								{
									successRate = Integer.parseInt(c.getTextContent());
								}
								else if ("ingredient".equalsIgnoreCase(c.getNodeName()))
								{
									int ingId = Integer.parseInt(c.getAttributes().getNamedItem("id").getNodeValue());
									int ingCount = Integer.parseInt(c.getAttributes().getNamedItem("count").getNodeValue());
									recipePartList.add(new L2RecipeInstance(ingId, ingCount));
								}
								else if ("production".equalsIgnoreCase(c.getNodeName()))
								{
									prodId = Integer.parseInt(c.getAttributes().getNamedItem("id").getNodeValue());
									count = Integer.parseInt(c.getAttributes().getNamedItem("count").getNodeValue());
								}
							}
							L2RecipeList recipeList = new L2RecipeList(id, level, recipeId, recipeName, successRate, mpCost, prodId, count, isDwarvenRecipe);
							for (L2RecipeInstance recipePart : recipePartList)
								recipeList.addRecipe(recipePart);
							
							_lists.put(_lists.size(), recipeList);
						}
					}
				}
			}
		}
		else
			_log.warning(RecipeData.class.getName() + ": recipes.xml is missing in data folder.");
	}
	
	public int getRecipesCount()
	{
		return _lists.size();
	}
	
	public L2RecipeList getRecipeList(int listId)
	{
		return _lists.get(listId);
	}
	
	@Override
	public L2RecipeList getRecipeByItemId(int itemId)
	{
		return _lists.values().stream().filter(r -> r.getRecipeId() == itemId).findAny().orElse(null);
	}
	
	public L2RecipeList getRecipeById(int recId)
	{
		return _lists.values().stream().filter(r -> r.getId() == recId).findAny().orElse(null);
	}
	
}