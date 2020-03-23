package com.l2jhellas.tools.ngl;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LocaleCodes
{
	Map<String, Locale> _locales = new HashMap<>();
	
	public static LocaleCodes getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected LocaleCodes()
	{
		loadCodes();
	}
	
	private void loadCodes()
	{
		for (Locale locale : Locale.getAvailableLocales())
		{
			String language = locale.getLanguage();
			// String script = locale.getScript();
			String country = locale.getCountry();
			String variant = locale.getVariant();
			
			if (language.isEmpty() && country.isEmpty() && variant.isEmpty())
			{
				continue;
			}
			
			StringBuilder lang = new StringBuilder();
			lang.append(language);
			if (!country.isEmpty())
			{
				lang.append(country);
			}
			if (!variant.isEmpty())
			{
				lang.append('_' + variant);
			}
			
			_locales.put(lang.toString(), locale);
		}
	}
	
	public Locale getLanguage(String lang)
	{
		return _locales.get(lang);
	}
	
	private static class SingletonHolder
	{
		protected static final LocaleCodes _instance = new LocaleCodes();
	}
}