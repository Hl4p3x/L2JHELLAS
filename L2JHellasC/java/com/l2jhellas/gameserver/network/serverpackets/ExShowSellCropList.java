package com.l2jhellas.gameserver.network.serverpackets;

import java.util.HashMap;
import java.util.List;

import com.l2jhellas.gameserver.instancemanager.CastleManorManager.CropProcure;
import com.l2jhellas.gameserver.model.L2Manor;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;

public class ExShowSellCropList extends L2GameServerPacket
{
	private static final String _S__FE_21_EXSHOWSELLCROPLIST = "[S] FE:21 ExShowSellCropList";
	
	private int _manorId = 1;
	private final HashMap<Integer, L2ItemInstance> _cropsItems;
	private final HashMap<Integer, CropProcure> _castleCrops;
	
	public ExShowSellCropList(L2PcInstance player, int manorId, List<CropProcure> arrayList)
	{
		_manorId = manorId;
		_castleCrops = new HashMap<>();
		_cropsItems = new HashMap<>();
		
		List<Integer> allCrops = L2Manor.getInstance().getAllCrops();
		for (int cropId : allCrops)
		{
			L2ItemInstance item = player.getInventory().getItemByItemId(cropId);
			if (item != null)
			{
				_cropsItems.put(cropId, item);
			}
		}
		
		for (CropProcure crop : arrayList)
		{
			if (_cropsItems.containsKey(crop.getId()) && crop.getAmount() > 0)
			{
				_castleCrops.put(crop.getId(), crop);
			}
		}
	}
	
	@Override
	public void runImpl()
	{
		// no long running
	}
	
	@Override
	public void writeImpl()
	{
		writeC(0xFE);
		writeH(0x21);
		
		writeD(_manorId); // manor id
		writeD(_cropsItems.size()); // size
		
		for (L2ItemInstance item : _cropsItems.values())
		{
			writeD(item.getObjectId()); // Object id
			writeD(item.getItemId()); // crop id
			writeD(L2Manor.getInstance().getSeedLevelByCrop(item.getItemId())); // seed level
			writeC(1);
			writeD(L2Manor.getInstance().getRewardItem(item.getItemId(), 1)); // reward 1 id
			writeC(1);
			writeD(L2Manor.getInstance().getRewardItem(item.getItemId(), 2)); // reward 2 id
			
			if (_castleCrops.containsKey(item.getItemId()))
			{
				CropProcure crop = _castleCrops.get(item.getItemId());
				writeD(_manorId); // manor
				writeD(crop.getAmount()); // buy residual
				writeD(crop.getPrice()); // buy price
				writeC(crop.getReward()); // reward
			}
			else
			{
				writeD(0xFFFFFFFF); // manor
				writeD(0); // buy residual
				writeD(0); // buy price
				writeC(0); // reward
			}
			writeD(item.getCount()); // my crops
		}
	}
	
	@Override
	public String getType()
	{
		return _S__FE_21_EXSHOWSELLCROPLIST;
	}
}