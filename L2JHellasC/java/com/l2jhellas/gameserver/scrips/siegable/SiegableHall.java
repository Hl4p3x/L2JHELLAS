package com.l2jhellas.gameserver.scrips.siegable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Calendar;
import java.util.logging.Level;

import com.l2jhellas.gameserver.model.L2Clan;
import com.l2jhellas.gameserver.model.L2SiegeClan;
import com.l2jhellas.gameserver.model.L2SiegeClan.SiegeClanType;
import com.l2jhellas.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.entity.ClanHall;
import com.l2jhellas.gameserver.model.zone.type.L2SiegeZone;
import com.l2jhellas.gameserver.network.serverpackets.SiegeInfo;
import com.l2jhellas.gameserver.templates.StatsSet;
import com.l2jhellas.util.database.L2DatabaseFactory;

public final class SiegableHall extends ClanHall
{	
	private static final String SQL_SAVE = "UPDATE siegable_clanhall SET ownerId=?, nextSiege=? WHERE clanHallId=?";
	
	private Calendar _nextSiege;
	
	private final long _siegeLength;
	
	private final int[] _scheduleConfig = {
		7,
		0,
		0,
		12,
		0
	};
	
	private SiegeStatus _status = SiegeStatus.REGISTERING;
	
	private L2SiegeZone _siegeZone;
	
	private ClanHallSiegeEngine _siege;
	
	public SiegableHall(StatsSet set)
	{
		super(set);
		_siegeLength = set.getLong("siegeLength");
		String[] rawSchConfig = set.getString("scheduleConfig").split(";");
		if (rawSchConfig.length == 5) {
			for (int i = 0; i < 5; i++) {
				try {
					_scheduleConfig[i] = Integer.parseInt(rawSchConfig[i]);
				} catch (Exception e) {
					_log.warning("SiegableHall - " + getName() + ": Wrong schedule_config parameters!");
				}
			}
		} else {
			_log.warning(getName() + ": Wrong schedule_config value in siegable_halls table, using default (7 days)");
		}
		
		_nextSiege = Calendar.getInstance();
		long nextSiege = set.getLong("nextSiege");
		if ((nextSiege - System.currentTimeMillis()) < 0) {
			updateNextSiege();
		} else {
			_nextSiege.setTimeInMillis(nextSiege);
		}
		
		if (getOwnerId() != 0)
		{
			_isFree = false;
			_paid = true;
			loadFunctions();
		}
	}
	
	public void spawnDoor() {
		spawnDoor(false);
	}
	
	public void spawnDoor(boolean isDoorWeak) {
		for (L2DoorInstance door : getDoors()) {
			if (door.isDead()) {
				door.doRevive();
				if (isDoorWeak) {
					door.setCurrentHp(door.getMaxHp() / 2.0);
				} else {
					door.setCurrentHp(door.getMaxHp());
				}
			}
			
			if (door.getOpen()) {
				door.closeMe();
			}
		}
	}
	
	@Override
	public void updateDb() {
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement ps = con.prepareStatement(SQL_SAVE)) {
			ps.setInt(1, getOwnerId());
			ps.setLong(2, getNextSiegeTime());
			ps.setInt(3, getId());
			ps.execute();
		} catch (Exception e) {
			_log.log(Level.WARNING, "Exception: SiegableHall.updateDb(): " + e.getMessage(), e);
		}
	}
	
	public void setSiege(final ClanHallSiegeEngine siegable) {
		_siege = siegable;
		//_siegeZone.setSiegeInstance(siegable);
	}
	
	public ClanHallSiegeEngine getSiege() {
		return _siege;
	}
	
	public Calendar getSiegeDate() {
		return _nextSiege;
	}
	
	public long getNextSiegeTime() {
		return _nextSiege.getTimeInMillis();
	}
	
	public long getSiegeLength() {
		return _siegeLength;
	}
	
	public void setNextSiegeDate(long date) {
		_nextSiege.setTimeInMillis(date);
	}
	
	public void setNextSiegeDate(final Calendar c) {
		_nextSiege = c;
	}
	
	public void updateNextSiege() {
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DAY_OF_YEAR, _scheduleConfig[0]);
		c.add(Calendar.MONTH, _scheduleConfig[1]);
		c.add(Calendar.YEAR, _scheduleConfig[2]);
		c.set(Calendar.HOUR_OF_DAY, _scheduleConfig[3]);
		c.set(Calendar.MINUTE, _scheduleConfig[4]);
		c.set(Calendar.SECOND, 0);
		setNextSiegeDate(c);
		updateDb();
	}
	
	public void addAttacker(final L2Clan clan) {
		if (getSiege() != null) {
			getSiege().getAttackers().put(clan.getClanId(), new L2SiegeClan(clan.getClanId(), SiegeClanType.ATTACKER));
		}
	}
	
	public void removeAttacker(final L2Clan clan) {
		if (getSiege() != null) {
			getSiege().getAttackers().remove(clan.getClanId());
		}
	}
	
	public boolean isRegistered(L2Clan clan) {
		if (getSiege() == null) {
			return false;
		}
		
		return getSiege().checkIsAttacker(clan);
	}
	
	public SiegeStatus getSiegeStatus() {
		return _status;
	}
	
	public boolean isRegistering() {
		return _status == SiegeStatus.REGISTERING;
	}
	
	public boolean isInSiege() {
		return _status == SiegeStatus.RUNNING;
	}
	
	public boolean isWaitingBattle() {
		return _status == SiegeStatus.WAITING_BATTLE;
	}
	
	public void updateSiegeStatus(SiegeStatus status) {
		_status = status;
	}
	
	public L2SiegeZone getSiegeZone() {
		return _siegeZone;
	}
	
	public void setSiegeZone(L2SiegeZone zone) {
		_siegeZone = zone;
	}
	
	public void updateSiegeZone(boolean active) {
		_siegeZone.setIsActive(active);
	}
	
	public void banishForeignerss()
	{
		_siegeZone.banishForeignerss(getOwnerId());
	}
	
	public void showSiegeInfo(L2PcInstance player) 
	{
		player.sendPacket(new SiegeInfo(this));
	}
	
	@Override
	public boolean isSiegableHall() 
	{
		return true;
	}
	
	@Override
	public void free()
	{
        super.free();
	}
}
