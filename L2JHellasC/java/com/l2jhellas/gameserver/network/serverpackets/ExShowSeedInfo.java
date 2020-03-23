package com.l2jhellas.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import com.l2jhellas.gameserver.instancemanager.CastleManorManager.SeedProduction;
import com.l2jhellas.gameserver.model.L2Manor;

public class ExShowSeedInfo extends L2GameServerPacket
{
	private static final String _S__FE_1C_EXSHOWSEEDINFO = "[S] FE:1C ExShowSeedInfo";
	private List<SeedProduction> _seeds;
	private final int _manorId;
	
	public ExShowSeedInfo(int manorId, List<SeedProduction> arrayList)
	{
		_manorId = manorId;
		_seeds = arrayList;
		if (_seeds == null)
		{
			_seeds = new ArrayList<>();
		}
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE); // Id
		writeH(0x1C); // SubId
		writeC(0);
		writeD(_manorId); // Manor ID
		writeD(0);
		writeD(_seeds.size());
		for (SeedProduction seed : _seeds)
		{
			writeD(seed.getId()); // Seed id
			writeD(seed.getCanProduce()); // Left to buy
			writeD(seed.getStartProduce()); // Started amount
			writeD(seed.getPrice()); // Sell Price
			writeD(L2Manor.getInstance().getSeedLevel(seed.getId())); // Seed Level
			writeC(1); // reward 1 Type
			writeD(L2Manor.getInstance().getRewardItemBySeed(seed.getId(), 1)); // Reward 1 Type Item Id
			writeC(1); // reward 2 Type
			writeD(L2Manor.getInstance().getRewardItemBySeed(seed.getId(), 2)); // Reward 2 Type Item Id
		}
	}
	
	@Override
	public String getType()
	{
		return _S__FE_1C_EXSHOWSEEDINFO;
	}
}