package com.l2jhellas.gameserver.model.actor.appearance;

import com.l2jhellas.gameserver.enums.Sex;

public class PcAppearance
{
	private byte _face;
	private byte _hairColor;
	private byte _hairStyle;
	
	private Sex _sex;
	
	private boolean _visible = true;
	
	private int _nameColor = 0xFFFFFF;	
	private int _titleColor = 0xFFFF77;
	
	public PcAppearance(byte Face, byte HColor, byte HStyle, Sex sex)
	{
		_face = Face;
		_hairColor = HColor;
		_hairStyle = HStyle;
		_sex = sex;
	}
	
	public final byte getFace()
	{
		return _face;
	}
	
	public final void setFace(int value)
	{
		_face = (byte) value;
	}
	
	public final byte getHairColor()
	{
		return _hairColor;
	}
	
	public final void setHairColor(int value)
	{
		_hairColor = (byte) value;
	}
	
	public final byte getHairStyle()
	{
		return _hairStyle;
	}
	
	public final void setHairStyle(int value)
	{
		_hairStyle = (byte) value;
	}
	
	public Sex getSex()
	{
		return _sex;
	}
	
	public void setSex(Sex sex)
	{
		_sex = sex;
	}
	
	public void setIsVisible(boolean vis)
	{
		_visible = vis;
	}

	public boolean isVisible()
	{
		return _visible;
	}
	
	public int getNameColor()
	{
		return _nameColor;
	}
	
	public void setNameColor(int nameColor)
	{
		_nameColor = nameColor;
	}
	
	public void setNameColor(int red, int green, int blue)
	{
		_nameColor = (red & 0xFF) + ((green & 0xFF) << 8) + ((blue & 0xFF) << 16);
	}
	
	public int getTitleColor()
	{
		return _titleColor;
	}
	
	public void setTitleColor(int titleColor)
	{
		_titleColor = titleColor;
	}
	
	public void setTitleColor(int red, int green, int blue)
	{
		_titleColor = (red & 0xFF) + ((green & 0xFF) << 8) + ((blue & 0xFF) << 16);
	}
}