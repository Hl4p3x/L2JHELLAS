package com.l2jhellas.gameserver.instancemanager;

public class Manager
{
	public static void reloadAll()
	{
		OlympiadStadiaManager.getInstance().clearStadium();
		GrandBossManager.getInstance().reload();
		ZoneManager.getInstance().reload();
		ItemsOnGroundManager.getInstance().reload();
		AuctionManager.getInstance().reload();
		BoatManager.getInstance();
		CastleManager.getInstance().reload();
		ClanHallManager.getInstance().reload();
		CoupleManager.getInstance().reload();
		CursedWeaponsManager.reload();
		DimensionalRiftManager.getInstance().reload();
		DuelManager.getInstance();
		FourSepulchersManager.getInstance();
		MercTicketManager.getInstance().reload();
		PetitionManager.getInstance();
		RaidBossSpawnManager.getInstance().reloadBosses();
		SiegeManager.getInstance();		
	}
}