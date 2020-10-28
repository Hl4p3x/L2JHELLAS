package com.l2jhellas.gameserver.model.actor.instance;

import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.ai.L2CharacterAI;
import com.l2jhellas.gameserver.ai.L2DoorAI;
import com.l2jhellas.gameserver.geodata.GeoControl;
import com.l2jhellas.gameserver.geodata.GeoEngine;
import com.l2jhellas.gameserver.geometry.Polygon;
import com.l2jhellas.gameserver.instancemanager.CastleManager;
import com.l2jhellas.gameserver.instancemanager.ZoneManager;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.model.actor.stat.DoorStat;
import com.l2jhellas.gameserver.model.actor.status.DoorStatus;
import com.l2jhellas.gameserver.model.entity.Castle;
import com.l2jhellas.gameserver.model.entity.ClanHall;
import com.l2jhellas.gameserver.model.zone.type.L2TownZone;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.ConfirmDlg;
import com.l2jhellas.gameserver.network.serverpackets.DoorInfo;
import com.l2jhellas.gameserver.network.serverpackets.DoorStatusUpdate;
import com.l2jhellas.gameserver.network.serverpackets.MyTargetSelected;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jhellas.gameserver.templates.L2CharTemplate;
import com.l2jhellas.gameserver.templates.L2Weapon;

public class L2DoorInstance extends L2Character implements GeoControl
{
	protected static final Logger _log = Logger.getLogger(L2DoorInstance.class.getName());
	
	private int _mapRegion = -1;
	
	// when door is closed, the dimensions are
	private int _rangeXMin = 0;
	private int _rangeYMin = 0;
	private int _rangeZMin = 0;
	private int _rangeXMax = 0;
	private int _rangeYMax = 0;
	private int _rangeZMax = 0;
	
	private int _A = 0;
	
	private int _B = 0;
	
	private int _C = 0;
	
	private int _D = 0;
	protected final int _doorId;
	protected final String _name;
	
	private boolean _open = false;
	private final boolean _unlockable;	
	public boolean geoOpen;
	private boolean _geodata = true;
	private ClanHall _clanHall;
	
	protected int _autoActionDelay = -1;
	private ScheduledFuture<?> _autoActionTask;
	
	public L2CharacterAI getAI()
	{
		L2CharacterAI ai = _ai;
		if (ai == null)
		{
			synchronized (this)
			{
				ai = _ai;
				if (ai == null)
					_ai = ai = new L2DoorAI(this);
			}
		}
		return ai;
	}
	
	@Override
	public boolean hasAI()
	{
		return (_ai != null);
	}
	
	class CloseTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				onClose();
			}
			catch (Throwable e)
			{
				_log.severe(CloseTask.class.getName() + ": Throwable: run");
				if (Config.DEVELOPER)
					e.printStackTrace();
			}
		}
	}
	
	class AutoOpenClose implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				String doorAction;
				
				if (!getOpen())
				{
					doorAction = "opened";
					openMe();
				}
				else
				{
					doorAction = "closed";
					closeMe();
				}
				
				if (Config.DEBUG)
				{
					_log.config("Auto " + doorAction + " door ID " + _doorId + " (" + _name + ") for " + (_autoActionDelay / 60000) + " minute(s).");
				}
			}
			catch (Exception e)
			{
				_log.warning(L2DoorInstance.class.getName() + ": Could not auto open/close door ID " + _doorId + " (" + _name + ")");
			}
		}
	}
	
	public L2DoorInstance(int objectId, L2CharTemplate template, int doorId, String name, boolean unlockable)
	{
		super(objectId, template);
		getStat(); // init stats
		getStatus(); // init status
		_doorId = doorId;
		_name = name;
		_unlockable = unlockable;
		geoOpen = true;
	}
	
	@Override
	public final DoorStat getStat()
	{
		if ((super.getStat() == null) || !(super.getStat() instanceof DoorStat))
		{
			setStat(new DoorStat(this));
		}
		return (DoorStat) super.getStat();
	}
	
	@Override
	public final DoorStatus getStatus()
	{
		if ((super.getStatus() == null) || !(super.getStatus() instanceof DoorStatus))
		{
			setStatus(new DoorStatus(this));
		}
		return (DoorStatus) super.getStatus();
	}
	
	public final boolean isUnlockable()
	{
		return _unlockable;
	}
	
	@Override
	public final int getLevel()
	{
		return 1;
	}
	
	public int getDoorId()
	{
		return _doorId;
	}
	
	public boolean getOpen()
	{
		return _open;
	}
	
	public void setOpen(boolean open)
	{
		_open = open;
	}
	
	public void setAutoActionDelay(int actionDelay)
	{
		if (_autoActionDelay == actionDelay)
			return;
		
		if (actionDelay > -1)
		{
			AutoOpenClose ao = new AutoOpenClose();
			ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(ao, actionDelay, actionDelay);
		}
		else
		{
			if (_autoActionTask != null)
			{
				_autoActionTask.cancel(false);
			}
		}
		
		_autoActionDelay = actionDelay;
	}
	
	public final int getDamage()
	{
		return Math.max(0, Math.min(6, 6 - (int) Math.ceil(getCurrentHp() / getMaxHp() * 6)));
	}
	
	public final Castle getCastle()
	{
		L2TownZone town = ZoneManager.getInstance().getClosestZone(this, L2TownZone.class);
		return town != null ? CastleManager.getInstance().getCastleById(town.getTaxById()) : null;
	}

	public void setClanHall(ClanHall clanhall)
	{
		_clanHall = clanhall;
	}
	
	public ClanHall getClanHall()
	{
		return _clanHall;
	}
	
	public boolean isEnemyOf(L2Character cha)
	{
		return true;
	}
	
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		if (isUnlockable())
			return true;
		
		// Attackable during siege by attacker only
		return (attacker != null && attacker instanceof L2PcInstance && getCastle() != null && getCastle().getCastleId() > 0 && getCastle().getSiege().getIsInProgress() && getCastle().getSiege().checkIsAttacker(((L2PcInstance) attacker).getClan()));
	}
	
	public boolean isAttackable(L2Character attacker)
	{
		return (attacker != null && attacker instanceof L2PcInstance && getCastle() != null && getCastle().getCastleId() > 0 && getCastle().getSiege().getIsInProgress() && getCastle().getSiege().checkIsAttacker(((L2PcInstance) attacker).getClan()));
	}
	
	@Override
	public void updateAbnormalEffect()
	{
	}
	
	@Override
	public L2ItemInstance getActiveWeaponInstance()
	{
		return null;
	}
	
	@Override
	public L2Weapon getActiveWeaponItem()
	{
		return null;
	}
	
	@Override
	public L2ItemInstance getSecondaryWeaponInstance()
	{
		return null;
	}
	
	@Override
	public L2Weapon getSecondaryWeaponItem()
	{
		return null;
	}
	
	@Override
	public void onAction(L2PcInstance player)
	{
		if (player == null)
			return;
		
		if (player.getTarget() != this)
		{
			player.setTarget(this);
			player.sendPacket(new DoorStatusUpdate(this));
		}
		else
		{
			if (isAutoAttackable(player))
			{
				if (Math.abs(player.getZ() - getZ()) < 400) 
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
			}
			else if (!isInsideRadius(player, L2Npc.INTERACTION_DISTANCE, false, false))
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
			else if (player.getClan() != null && _clanHall != null && player.getClanId() == _clanHall.getOwnerId())
			{
				player.gatesRequest(this);
				player.sendPacket(new ConfirmDlg((!getOpen()) ? 1140 : 1141));
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
			else
				player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);		
	}
	
	@Override
	public void onActionShift(L2PcInstance player)
	{
		if (player == null)
			return;
		
		if (player.getAccessLevel().isGm())
		{
			player.setTarget(this);
			MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel());
			player.sendPacket(my);
			
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			StringBuilder html1 = new StringBuilder("<html><title>Door Edit</title><body><table border=0>");
			html1.append("<tr><td>Door Stats:</td></tr>");
			html1.append("<tr><td>HP: " + (int) getCurrentHp() + "/" + getMaxHp() + "</td></tr>");
			
			html1.append("<tr><td>Object ID: " + getObjectId() + "</td></tr>");
			html1.append("<tr><td>Door ID:" + getDoorId() + "</td></tr>");
			html1.append("<tr><td></td></tr>");
			
			html1.append("<tr><td>Max X " + getXMax() + "</td></tr>");
			html1.append("<tr><td>Max Y " + getYMax() + "</td></tr>");
			html1.append("<tr><td>Max Z " + getZMax() + "</td></tr>");
			html1.append("<tr><td>Min X " + getXMin() + "</td></tr>");
			html1.append("<tr><td>Min Y " + getYMin() + "</td></tr>");
			html1.append("<tr><td>Min Z " + getZMin() + "</td></tr>");
			html1.append("<tr><td></td></tr>");
			
			html1.append("<tr><td>Class: " + getClass().getSimpleName() + "</td></tr>");
			html1.append("<tr><td></td></tr>");
			html1.append("</table>");
			
			html1.append("<br><center><table><tr>");
			html1.append("<td><button value=\"Open\" action=\"bypass -h admin_open " + getDoorId() + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
			html1.append("<td><button value=\"Close\" action=\"bypass -h admin_close " + getDoorId() + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
			html1.append("<td><button value=\"Kill\" action=\"bypass -h admin_kill\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
			html1.append("<td><button value=\"Delete\" action=\"bypass -h admin_delete\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
			html1.append("</tr></table></center></body></html>");
			
			html.setHtml(html1.toString());
			player.sendPacket(html);
		}
		
		if (player.getTarget() != this)
		{
			player.setTarget(this);
			
			if (isAutoAttackable(player))
				player.sendPacket(new DoorStatusUpdate(this));
		}
		else
			player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public void broadcastStatusUpdate()
	{
		broadcastPacket(new DoorStatusUpdate(this));
	}
	
	public void onOpen()
	{
		ThreadPoolManager.getInstance().scheduleGeneral(new CloseTask(), 60000);
	}
	
	public void onClose()
	{
		closeMe();
	}
	
	public final void closeMe()
	{
		if(!getOpen() || isDead())
			return;

		setOpen(false);

		setGeoOpen(false);
		broadcastStatusUpdate();
	}
	
	public final void openMe()
	{
		if(getOpen() || isDead())
			return;

		setOpen(true);

		setGeoOpen(true);
		broadcastStatusUpdate();
	}

    public boolean setGeoOpen(final boolean open) 
    {
		if(!Config.ALLOW_DOORS)
			return false;
		
        if (geoOpen == open)
            return false;
        
        geoOpen = open;
        
        if (Config.GEODATA) 
		{
            if (open) 
				GeoEngine.returnGeoAtControl(this);
            else 
				GeoEngine.applyControl(this);
        }
        return true;
    }
	
	@Override
	public String toString()
	{
		return "door " + _doorId;
	}
	
	public String getDoorName()
	{
		return _name;
	}
	
	public int getXMin()
	{
		return _rangeXMin;
	}
	
	public int getYMin()
	{
		return _rangeYMin;
	}
	
	public int getZMin()
	{
		return _rangeZMin;
	}
	
	public int getXMax()
	{
		return _rangeXMax;
	}
	
	public int getYMax()
	{
		return _rangeYMax;
	}
	
	public int getZMax()
	{
		return _rangeZMax;
	}
	
	public void setRange(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax)
	{
		_rangeXMin = xMin;
		_rangeYMin = yMin;
		_rangeZMin = zMin;
		
		_rangeXMax = xMax;
		_rangeYMax = yMax;
		_rangeZMax = zMax;
		
		_A = _rangeYMax * (_rangeZMax - _rangeZMin) + _rangeYMin * (_rangeZMin - _rangeZMax);
		_B = _rangeZMin * (_rangeXMax - _rangeXMin) + _rangeZMax * (_rangeXMin - _rangeXMax);
		_C = _rangeXMin * (_rangeYMax - _rangeYMin) + _rangeXMin * (_rangeYMin - _rangeYMax);
		_D = -1 * (_rangeXMin * (_rangeYMax * _rangeZMax - _rangeYMin * _rangeZMax) + _rangeXMax * (_rangeYMin * _rangeZMin - _rangeYMin * _rangeZMax) + _rangeXMin * (_rangeYMin * _rangeZMax - _rangeYMax * _rangeZMin));
		
        final Polygon shape = new Polygon();
        shape.add(_rangeXMin, _rangeYMin);
        shape.add(_rangeXMax, _rangeYMax);
        shape.setZmin(_rangeZMin);
        shape.setZmax(_rangeZMax);
        setGeoPos(shape);
	}
	
	public int getMapRegion()
	{
		return _mapRegion;
	}
	
	public void setMapRegion(int region)
	{
		_mapRegion = region;
	}
	
	public int getA()
	{
		return _A;
	}
	
	public int getB()
	{
		return _B;
	}
	
	public int getC()
	{
		return _C;
	}
	
	public int getD()
	{
		return _D;
	}
	
	@Override
	public void onForcedAttack(L2PcInstance player)
	{
		onAction(player);
	}
	
	@Override
	public void sendInfo(L2PcInstance activeChar)
	{
		activeChar.sendPacket(new DoorInfo(this));
		activeChar.sendPacket(new DoorStatusUpdate(this));
	}
	
	private Polygon geoPos;
	private byte[][] geoAround;

	public Polygon getGeoPos()
	{
		return geoPos;
	}

	public void setGeoPos(Polygon value)
	{
		geoPos = value;
	}

	public byte[][] getGeoAround()
	{
		return geoAround;
	}

	public void setGeoAround(byte[][] value)
	{
		geoAround = value;
	}
	
	public void setGeodata(boolean value)
	{
		_geodata = value;
	}

	public boolean getGeodata()
	{
		return _geodata;
	}

	@Override
	public boolean isGeoCloser()
	{
		return true;
	}
}