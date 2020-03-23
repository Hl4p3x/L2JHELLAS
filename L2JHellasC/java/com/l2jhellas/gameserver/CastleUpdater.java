package com.l2jhellas.gameserver;

import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.instancemanager.CastleManager;
import com.l2jhellas.gameserver.model.L2Clan;
import com.l2jhellas.gameserver.model.actor.item.ItemContainer;
import com.l2jhellas.gameserver.model.entity.Castle;

public class CastleUpdater implements Runnable
{
	protected static Logger _log = Logger.getLogger(CastleUpdater.class.getName());
	private final L2Clan _clan;
	private int _runCount = 0;
	
	public CastleUpdater(L2Clan clan, int runCount)
	{
		_clan = clan;
		_runCount = runCount;
	}
	
	@Override
	public void run()
	{
		try
		{
			// Move current castle treasury to clan warehouse every 2 hour
			ItemContainer warehouse = _clan.getWarehouse();
			if ((warehouse != null) && (_clan.hasCastle() > 0))
			{
				Castle castle = CastleManager.getInstance().getCastleById(_clan.hasCastle());
				if (!Config.ALT_MANOR_SAVE_ALL_ACTIONS)
				{
					if (_runCount % Config.ALT_MANOR_SAVE_PERIOD_RATE == 0)
					{
						castle.saveSeedData();
						castle.saveCropData();
						_log.info(CastleUpdater.class.getName() + ": all data for " + castle.getName() + " saved.");
					}
				}
				_runCount++;
				CastleUpdater cu = new CastleUpdater(_clan, _runCount);
				ThreadPoolManager.getInstance().scheduleGeneral(cu, 3600000);
			}
		}
		catch (Throwable e)
		{
			_log.warning(CastleUpdater.class.getName() + ": could not save data.");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
}