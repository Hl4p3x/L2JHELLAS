package com.l2jhellas.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import com.l2jhellas.gameserver.instancemanager.CastleManorManager.CropProcure;
import com.l2jhellas.gameserver.model.L2Manor;

public class ExShowCropInfo extends L2GameServerPacket
{
	private static final String _S__FE_1C_EXSHOWSEEDINFO = "[S] FE:1D ExShowCropInfo";
	private List<CropProcure> _crops;
	private final int _manorId;
	
	public ExShowCropInfo(int manorId, List<CropProcure> arrayList)
	{
		_manorId = manorId;
		_crops = arrayList;
		if (_crops == null)
		{
			_crops = new ArrayList<>();
		}
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE); // Id
		writeH(0x1D); // SubId
		writeC(0);
		writeD(_manorId); // Manor ID
		writeD(0);
		writeD(_crops.size());
		for (CropProcure crop : _crops)
		{
			writeD(crop.getId()); // Crop id
			writeD(crop.getAmount()); // Buy residual
			writeD(crop.getStartAmount()); // Buy
			writeD(crop.getPrice()); // Buy price
			writeC(crop.getReward()); // Reward
			writeD(L2Manor.getInstance().getSeedLevelByCrop(crop.getId())); // Seed Level
			writeC(1); // reward 1 Type
			writeD(L2Manor.getInstance().getRewardItem(crop.getId(), 1)); // Reward 1 Type Item Id
			writeC(1); // reward 2 Type
			writeD(L2Manor.getInstance().getRewardItem(crop.getId(), 2)); // Reward 2 Type Item Id
		}
	}
	
	@Override
	public String getType()
	{
		return _S__FE_1C_EXSHOWSEEDINFO;
	}
}