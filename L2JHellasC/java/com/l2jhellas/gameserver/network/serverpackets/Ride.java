package com.l2jhellas.gameserver.network.serverpackets;

public class Ride extends L2GameServerPacket
{
	private static final String _S__86_Ride = "[S] 86 Ride";
	public static final int ACTION_MOUNT = 1;
	public static final int ACTION_DISMOUNT = 0;
	private final int _id;
	private final int _bRide;
	private int _rideType;
	private final int _rideClassID;
	
	public Ride(int id, int action, int rideClassId)
	{
		_id = id; // charobjectID
		_bRide = action; // 1 for mount ; 2 for dismount
		_rideClassID = rideClassId + 1000000; // npcID
		
		switch (rideClassId)
		{
		// Striders
			case 12526:
			case 12527:
			case 12528:
				_rideType = 1;
				break;
			// Wyvern
			case 12621:
				_rideType = 2;
				break;
		}
	}
	
	@Override
	public void runImpl()
	{
		
	}
	
	public int getMountType()
	{
		return _rideType;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x86);
		writeD(_id);
		writeD(_bRide);
		writeD(_rideType);
		writeD(_rideClassID);
	}
	
	@Override
	public String getType()
	{
		return _S__86_Ride;
	}
}