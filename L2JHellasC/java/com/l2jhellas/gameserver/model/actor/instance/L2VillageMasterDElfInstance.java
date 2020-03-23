package com.l2jhellas.gameserver.model.actor.instance;

import com.l2jhellas.gameserver.enums.player.ClassId;
import com.l2jhellas.gameserver.enums.player.ClassRace;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;

public final class L2VillageMasterDElfInstance extends L2VillageMasterInstance
{
	public L2VillageMasterDElfInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	protected final boolean checkVillageMasterRace(ClassId pclass)
	{
		if (pclass == null)
			return false;
		
		return pclass.getRace() == ClassRace.DARK_ELF;
	}
}