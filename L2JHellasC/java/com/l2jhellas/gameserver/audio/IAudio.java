package com.l2jhellas.gameserver.audio;

import com.l2jhellas.gameserver.network.serverpackets.PlaySound;

public interface IAudio
{
	public String getSoundName();
	public PlaySound getPacket();
}