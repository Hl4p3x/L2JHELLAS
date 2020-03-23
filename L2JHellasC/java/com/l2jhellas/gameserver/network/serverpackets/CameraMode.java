package com.l2jhellas.gameserver.network.serverpackets;

public class CameraMode extends L2GameServerPacket
{
	private static final String _S__F1_CAMERAMODE = "[S] F1 CameraMode";
	
	private final int _mode;
	
	public CameraMode(int mode)
	{
		_mode = mode;
	}
	
	@Override
	public void writeImpl()
	{
		writeC(0xf1);
		writeD(_mode);
	}
	
	@Override
	public String getType()
	{
		return _S__F1_CAMERAMODE;
	}
}