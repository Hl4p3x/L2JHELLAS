package com.l2jhellas.gameserver.templates;

import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractVarSet extends ConcurrentHashMap<String, String>
{
	private static final long serialVersionUID = 2L;

	protected abstract void onSet(String key, String value);
	
	protected abstract void onUnset(String key);
	
	public AbstractVarSet()
	{
		super();
	}
	
	public AbstractVarSet(final int size)
	{
		super(size);
	}
	
	public void set(final String key, final boolean value)
	{
		set(key, String.valueOf(value));
	}
	
	public void set(final String key, final int value)
	{
		set(key, String.valueOf(value));
	}
	
	public void set(final String key, final long value)
	{
		set(key, String.valueOf(value));
	}
	
	public void set(final String key, final double value)
	{
		set(key, String.valueOf(value));
	}
	
	public void set(final String key, final Enum<?> value)
	{
		set(key, String.valueOf(value));
	}
		
	public final void set(final String key, final String value)
	{
		onSet(key, value);
		
		put(key, value);
	}
	
	public final void unset(String key)
	{
		onUnset(key);
		
		remove(key);
	}
	
	public boolean getBool(final String key)
	{
		final String val = get(key);
		if (val != null)
			return Boolean.parseBoolean(val);
		
		throw new IllegalArgumentException("AbstractVarSet : Boolean value required, but found: " + val + " for key: " + key + ".");
	}
	
	public boolean getBool(final String key, final boolean defaultValue)
	{
		final String val = get(key);
		if (val != null)
			return Boolean.parseBoolean(val);
		
		return defaultValue;
	}
	
	public int getInteger(final String key)
	{
		final String val = get(key);
		if (val != null)
			return Integer.parseInt(val);
		
		throw new IllegalArgumentException("AbstractVarSet : Integer value required, but found: " + val + " for key: " + key + ".");
	}
	
	public int getInteger(final String key, final int defaultValue)
	{
		final String val = get(key);
		if (val != null)
			return Integer.parseInt(val);
		
		return defaultValue;
	}
	
	public long getLong(final String key)
	{
		final String val = get(key);
		if (val != null)
			return Long.parseLong(val);
		
		throw new IllegalArgumentException("AbstractVarSet : Long value required, but found: " + val + " for key: " + key + ".");
	}
	
	public long getLong(final String key, final long defaultValue)
	{
		final String val = get(key);
		if (val != null)
			return Long.parseLong(val);
		
		return defaultValue;
	}
	
	public double getDouble(final String key)
	{
		final String val = get(key);
		if (val != null)
			return Double.parseDouble(val);
		
		throw new IllegalArgumentException("AbstractVarSet : Double value required, but found: " + val + " for key: " + key + ".");
	}
	
	public double getDouble(final String key, final double defaultValue)
	{
		final String val = get(key);
		if (val != null)
			return Double.parseDouble(val);
		
		return defaultValue;
	}
	
	public String getString(String key)
	{
		final Object val = get(key);
		if (val == null)
		{
			throw new IllegalArgumentException("AbstractVarSet : String value required, but not specified");
		}
		return String.valueOf(val);
	}
	
	public String getString(String key, String defaultValue)
	{
		final Object val = get(key);
		if (val == null)
		{
			return defaultValue;
		}
		return String.valueOf(val);
	}

	public <E extends Enum<E>> E getEnum(final String name, final Class<E> enumClass)
	{
		final String val = get(name);
		
		if (val != null)
			return Enum.valueOf(enumClass, val);
		
		throw new IllegalArgumentException("Enum value of type " + enumClass.getName() + " required, but found: " + val + ".");
	}
	
	public <E extends Enum<E>> E getEnum(final String name, final Class<E> enumClass, final E defaultValue)
	{
		final String val = get(name);
		
		if (val != null)
			return Enum.valueOf(enumClass, val);
		
		return defaultValue;
	}
}