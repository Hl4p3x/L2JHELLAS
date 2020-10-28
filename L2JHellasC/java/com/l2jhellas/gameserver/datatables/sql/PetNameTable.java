package com.l2jhellas.gameserver.datatables.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.datatables.xml.PetData;
import com.l2jhellas.util.database.L2DatabaseFactory;

public class PetNameTable
{
	private static Logger _log = Logger.getLogger(PetNameTable.class.getName());
	
	private static PetNameTable _instance;
	
	public static PetNameTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new PetNameTable();
		}
		return _instance;
	}
	
	public boolean doesPetNameExist(String name, int petNpcId)
	{
		boolean result = true;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
		PreparedStatement statement = con.prepareStatement("SELECT name FROM pets p, items i WHERE p.item_obj_id = i.object_id AND name=? AND i.item_id IN (?)"))
		{
			statement.setString(1, name);
			
			StringBuilder cond = new StringBuilder();
			
			if (!cond.toString().isEmpty())
				cond.append(", ");
			
			cond.append(PetData.getPetItemsAsNpc(petNpcId));
	
			statement.setString(2, cond.toString());
			
			try(ResultSet rset = statement.executeQuery())
			{
				result = rset.next();
			}
		}
		catch (SQLException e)
		{
			_log.warning(PetNameTable.class.getName() + ": could not check existing petname:");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
		return result;
	}
	
	public boolean isValidPetName(String name)
	{
		boolean result = true;
		
		if (!isAlphaNumeric(name))
			return result;
		
		Pattern pattern;
		try
		{
			pattern = Pattern.compile(Config.PET_NAME_TEMPLATE);
		}
		catch (PatternSyntaxException e) // case of illegal pattern
		{
			_log.warning(PetNameTable.class.getName() + ": Error Pet name pattern of config is wrong!");
			if (Config.DEVELOPER)
				e.printStackTrace();
			pattern = Pattern.compile(".*");
		}
		Matcher regexp = pattern.matcher(name);
		if (!regexp.matches())
		{
			result = false;
		}
		return result;
	}
	
	private static boolean isAlphaNumeric(String text)
	{
		boolean result = true;
		char[] chars = text.toCharArray();
		for (int i = 0; i < chars.length; i++)
		{
			if (!Character.isLetterOrDigit(chars[i]))
			{
				result = false;
				break;
			}
		}
		return result;
	}
}