package com.l2jhellas.gameserver.model;

import com.l2jhellas.gameserver.datatables.xml.AdminData;
import com.l2jhellas.gameserver.templates.StatsSet;

public class L2AccessLevel
{
	
	private int _accessLevel = 0;
	
	private String _name = null;
	
	L2AccessLevel _childsAccessLevel = null;
	
	private int _child = 0;
	
	private int _nameColor = 0;
	
	private int _titleColor = 0;
	
	private boolean _isGm = false;
	
	private boolean _allowPeaceAttack = false;
	
	private boolean _allowFixedRes = false;
	
	private boolean _allowTransaction = false;
	
	private boolean _allowAltG = false;
	
	private boolean _giveDamage = false;
	
	private boolean _takeAggro = false;
	
	private boolean _gainExp = false;
	
	public L2AccessLevel(StatsSet set)
	{
		_accessLevel = set.getInteger("level");
		_name = set.getString("name");
		_nameColor = Integer.decode("0x" + set.getString("nameColor", "FFFFFF"));
		_titleColor = Integer.decode("0x" + set.getString("titleColor", "FFFFFF"));
		_child = set.getInteger("childAccess", 0);
		_isGm = set.getBool("isGM", false);
		_allowPeaceAttack = set.getBool("allowPeaceAttack", false);
		_allowFixedRes = set.getBool("allowFixedRes", false);
		_allowTransaction = set.getBool("allowTransaction", true);
		_allowAltG = set.getBool("allowAltg", false);
		_giveDamage = set.getBool("giveDamage", true);
		_takeAggro = set.getBool("takeAggro", true);
		_gainExp = set.getBool("gainExp", true);
	}
	
	public L2AccessLevel()
	{
		_accessLevel = 0;
		_name = "User";
		_nameColor = Integer.decode("0xFFFFFF");
		_titleColor = Integer.decode("0xFFFFFF");
		_child = 0;
		_isGm = false;
		_allowPeaceAttack = false;
		_allowFixedRes = false;
		_allowTransaction = true;
		_allowAltG = false;
		_giveDamage = true;
		_takeAggro = true;
		_gainExp = true;
	}
	
	public int getLevel()
	{
		return _accessLevel;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public int getNameColor()
	{
		return _nameColor;
	}
	
	public int getTitleColor()
	{
		return _titleColor;
	}
	
	public boolean isGm()
	{
		return _isGm;
	}
	
	public boolean allowPeaceAttack()
	{
		return _allowPeaceAttack;
	}
	
	public boolean allowFixedRes()
	{
		return _allowFixedRes;
	}
	
	public boolean allowTransaction()
	{
		return _allowTransaction;
	}
	
	public boolean allowAltG()
	{
		return _allowAltG;
	}
	
	public boolean canGiveDamage()
	{
		return _giveDamage;
	}
	
	public boolean canTakeAggro()
	{
		return _takeAggro;
	}
	
	public boolean canGainExp()
	{
		return _gainExp;
	}
	
	public boolean hasChildAccess(L2AccessLevel accessLevel)
	{
		if (_childsAccessLevel == null)
		{
			if (_child <= 0)
				return false;
			
			_childsAccessLevel = AdminData.getInstance().getAccessLevel(_child);
		}
		return (_childsAccessLevel.getLevel() == accessLevel.getLevel() || _childsAccessLevel.hasChildAccess(accessLevel));
	}
}