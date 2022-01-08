package com.l2jhellas.gameserver.datatables.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.l2jhellas.Config;
import com.l2jhellas.util.database.L2DatabaseFactory;

public class PetNameTable
{
	private static Logger _log = Logger.getLogger(PetNameTable.class.getName());
	
	static String FIND_PETNAME = "SELECT name FROM pets WHERE name=?";

	private static PetNameTable _instance;
	
	public static PetNameTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new PetNameTable();
		}
		return _instance;
	}
	
	public boolean doesPetNameExist(String name)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
		PreparedStatement ps = con.prepareStatement(FIND_PETNAME)) 
		{
			ps.setString(1, name);
			try (ResultSet rs = ps.executeQuery()) 
			{
				return rs.next();
			}
		} catch (Exception ex) 
		{
			_log.warning(PetNameTable.class.getName() + ": could not check existing petname:");
		}
		return false;
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