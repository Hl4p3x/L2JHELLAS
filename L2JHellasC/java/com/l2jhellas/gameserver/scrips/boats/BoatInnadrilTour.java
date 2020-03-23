package com.l2jhellas.gameserver.scrips.boats;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.enums.player.ChatType;
import com.l2jhellas.gameserver.enums.sound.Sound;
import com.l2jhellas.gameserver.instancemanager.BoatManager;
import com.l2jhellas.gameserver.model.VehiclePathPoint;
import com.l2jhellas.gameserver.model.actor.L2Vehicle;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.CreatureSay;

public class BoatInnadrilTour implements Runnable
{
	private static final Logger _log = Logger.getLogger(BoatInnadrilTour.class.getName());

	private static final VehiclePathPoint[] TOUR =
	{
		new VehiclePathPoint(105129, 226240, -3610, 150, 800),
		new VehiclePathPoint(90604, 238797, -3610, 150, 800),
		new VehiclePathPoint(74853, 237943, -3610, 150, 800),
		new VehiclePathPoint(68207, 235399, -3610, 150, 800),
		new VehiclePathPoint(63226, 230487, -3610, 150, 800),
		new VehiclePathPoint(61843, 224797, -3610, 150, 800),
		new VehiclePathPoint(61822, 203066, -3610, 150, 800),
		new VehiclePathPoint(59051, 197685, -3610, 150, 800),
		new VehiclePathPoint(54048, 195298, -3610, 150, 800),
		new VehiclePathPoint(41609, 195687, -3610, 150, 800),
		new VehiclePathPoint(35821, 200284, -3610, 150, 800),
		new VehiclePathPoint(35567, 205265, -3610, 150, 800),
		new VehiclePathPoint(35617, 222471, -3610, 150, 800),
		new VehiclePathPoint(37932, 226588, -3610, 150, 800),
		new VehiclePathPoint(42932, 229394, -3610, 150, 800),
		new VehiclePathPoint(74324, 245231, -3610, 150, 800),
		new VehiclePathPoint(81872, 250314, -3610, 150, 800),
		new VehiclePathPoint(101692, 249882, -3610, 150, 800),
		new VehiclePathPoint(107907, 256073, -3610, 150, 800),
		new VehiclePathPoint(112317, 257133, -3610, 150, 800),
		new VehiclePathPoint(126273, 255313, -3610, 150, 800),
		new VehiclePathPoint(128067, 250961, -3610, 150, 800),
		new VehiclePathPoint(128520, 238249, -3610, 150, 800),
		new VehiclePathPoint(126428, 235072, -3610, 150, 800),
		new VehiclePathPoint(121843, 234656, -3610, 150, 800),
		new VehiclePathPoint(120096, 234268, -3610, 150, 800),
		new VehiclePathPoint(118572, 233046, -3610, 150, 800),
		new VehiclePathPoint(117671, 228951, -3610, 150, 800),
		new VehiclePathPoint(115936, 226540, -3610, 150, 800),
		new VehiclePathPoint(113628, 226240, -3610, 150, 800),
		new VehiclePathPoint(111300, 226240, -3610, 150, 800),
		new VehiclePathPoint(111264, 226240, -3610, 150, 800)
	};
	
	private static final VehiclePathPoint DOCK = TOUR[TOUR.length - 1];
	
	private final L2Vehicle _boat;
	private int _cycle = 0;
	
	private final CreatureSay ARRIVED_AT_INNADRIL;
	private final CreatureSay LEAVE_INNADRIL5;
	private final CreatureSay LEAVE_INNADRIL1;
	private final CreatureSay LEAVE_INNADRIL0;
	private final CreatureSay LEAVING_INNADRIL;
	
	private final CreatureSay ARRIVAL20;
	private final CreatureSay ARRIVAL15;
	private final CreatureSay ARRIVAL10;
	private final CreatureSay ARRIVAL5;
	private final CreatureSay ARRIVAL1;
	
	public BoatInnadrilTour(L2Vehicle boat)
	{
		_boat = boat;
		
		ARRIVED_AT_INNADRIL = new CreatureSay(0, ChatType.BOAT.getClientId(), 801, SystemMessageId.INNADRIL_BOAT_ANCHOR_10_MINUTES);
		LEAVE_INNADRIL5 = new CreatureSay(0, ChatType.BOAT.getClientId(), 801, SystemMessageId.INNADRIL_BOAT_LEAVE_IN_5_MINUTES);
		LEAVE_INNADRIL1 = new CreatureSay(0, ChatType.BOAT.getClientId(), 801, SystemMessageId.INNADRIL_BOAT_LEAVE_IN_1_MINUTE);
		LEAVE_INNADRIL0 = new CreatureSay(0, ChatType.BOAT.getClientId(), 801, SystemMessageId.INNADRIL_BOAT_LEAVE_SOON);
		LEAVING_INNADRIL = new CreatureSay(0, ChatType.BOAT.getClientId(), 801, SystemMessageId.INNADRIL_BOAT_LEAVING);
		
		ARRIVAL20 = new CreatureSay(0, ChatType.BOAT.getClientId(), 801, SystemMessageId.INNADRIL_BOAT_ARRIVE_20_MINUTES);
		ARRIVAL15 = new CreatureSay(0, ChatType.BOAT.getClientId(), 801, SystemMessageId.INNADRIL_BOAT_ARRIVE_15_MINUTES);
		ARRIVAL10 = new CreatureSay(0, ChatType.BOAT.getClientId(), 801, SystemMessageId.INNADRIL_BOAT_ARRIVE_10_MINUTES);
		ARRIVAL5 = new CreatureSay(0, ChatType.BOAT.getClientId(), 801, SystemMessageId.INNADRIL_BOAT_ARRIVE_5_MINUTES);
		ARRIVAL1 = new CreatureSay(0, ChatType.BOAT.getClientId(), 801, SystemMessageId.INNADRIL_BOAT_ARRIVE_1_MINUTE);
	}
	
	@Override
	public void run()
	{
		try
		{
			switch (_cycle)
			{
				case 0:
					BoatManager.getInstance().broadcastPacket(DOCK, DOCK, LEAVE_INNADRIL5);
					ThreadPoolManager.getInstance().scheduleGeneral(this, 240000);
					break;
				case 1:
					BoatManager.getInstance().broadcastPacket(DOCK, DOCK, LEAVE_INNADRIL1);
					ThreadPoolManager.getInstance().scheduleGeneral(this, 40000);
					break;
				case 2:
					BoatManager.getInstance().broadcastPacket(DOCK, DOCK, LEAVE_INNADRIL0);
					ThreadPoolManager.getInstance().scheduleGeneral(this, 20000);
					break;
				case 3:
					BoatManager.getInstance().broadcastPackets(DOCK, DOCK, LEAVING_INNADRIL, Sound.ITEMSOUND_SHIP_ARRIVAL_DEPARTURE.withObject(_boat));
					_boat.payForRide(0, 1, 107092, 219098, -3952);
					_boat.executePath(TOUR);
					ThreadPoolManager.getInstance().scheduleGeneral(this, 650000);
					break;
				case 4:
					BoatManager.getInstance().broadcastPacket(DOCK, DOCK, ARRIVAL20);
					ThreadPoolManager.getInstance().scheduleGeneral(this, 300000);
					break;
				case 5:
					BoatManager.getInstance().broadcastPacket(DOCK, DOCK, ARRIVAL15);
					ThreadPoolManager.getInstance().scheduleGeneral(this, 300000);
					break;
				case 6:
					BoatManager.getInstance().broadcastPacket(DOCK, DOCK, ARRIVAL10);
					ThreadPoolManager.getInstance().scheduleGeneral(this, 300000);
					break;
				case 7:
					BoatManager.getInstance().broadcastPacket(DOCK, DOCK, ARRIVAL5);
					ThreadPoolManager.getInstance().scheduleGeneral(this, 240000);
					break;
				case 8:
					BoatManager.getInstance().broadcastPacket(DOCK, DOCK, ARRIVAL1);
					break;
				case 9:
					BoatManager.getInstance().broadcastPackets(DOCK, DOCK, ARRIVED_AT_INNADRIL, Sound.ITEMSOUND_SHIP_ARRIVAL_DEPARTURE.withObject(_boat));
					ThreadPoolManager.getInstance().scheduleGeneral(this, 300000);
					break;
			}
			_cycle++;
			if (_cycle > 9)
				_cycle = 0;
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, e.getMessage());
		}
	}

	public static void load()
	{
		final L2Vehicle boat = BoatManager.getInstance().getNewBoat(4, 111264, 226240, -3610, 32768);
		if (boat != null)
		{
			boat.registerEngine(new BoatInnadrilTour(boat));
			boat.runEngine(180000);
		}
	}
}