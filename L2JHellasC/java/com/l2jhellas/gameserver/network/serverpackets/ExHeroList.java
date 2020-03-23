package com.l2jhellas.gameserver.network.serverpackets;

import java.util.Map;

import com.l2jhellas.gameserver.model.entity.Hero;
import com.l2jhellas.gameserver.model.entity.olympiad.Olympiad;
import com.l2jhellas.gameserver.templates.StatsSet;

public class ExHeroList extends L2GameServerPacket
{
	private static final String _S__FE_23_EXHEROLIST = "[S] FE:23 ExHeroList";
	private final Map<Integer, StatsSet> _heroList;
	
	public ExHeroList()
	{
		_heroList = Hero.getInstance().getHeroes();
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x23);
		writeD(_heroList.size());
		
		for (Integer heroId : _heroList.keySet())
		{
			StatsSet hero = _heroList.get(heroId);
			writeS(hero.getString(Olympiad.CHAR_NAME));
			writeD(hero.getInteger(Olympiad.CLASS_ID));
			writeS(hero.getString(Hero.CLAN_NAME, ""));
			writeD(hero.getInteger(Hero.CLAN_CREST, 0));
			writeS(hero.getString(Hero.ALLY_NAME, ""));
			writeD(hero.getInteger(Hero.ALLY_CREST, 0));
			writeD(hero.getInteger(Hero.COUNT));
		}
	}
	
	@Override
	public String getType()
	{
		return _S__FE_23_EXHEROLIST;
	}
}