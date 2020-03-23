package com.l2jhellas.gameserver.network.clientpackets;

import java.util.ArrayList;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.instancemanager.CastleManager;
import com.l2jhellas.gameserver.instancemanager.CastleManorManager;
import com.l2jhellas.gameserver.instancemanager.CastleManorManager.CropProcure;

public class RequestSetCrop extends L2GameClientPacket
{
	private static final String _C__D0_0B_REQUESTSETCROP = "[C] D0:0B RequestSetCrop";
	
	private int _size;
	
	private int _manorId;
	
	private int[] _items; // _size*4
	
	@Override
	protected void readImpl()
	{
		_manorId = readD();
		_size = readD();
		if (_size * 13 > _buf.remaining() || _size > 500)
		{
			_size = 0;
			return;
		}
		_items = new int[_size * 4];
		for (int i = 0; i < _size; i++)
		{
			int itemId = readD();
			_items[i * 4 + 0] = itemId;
			int sales = readD();
			_items[i * 4 + 1] = sales;
			int price = readD();
			_items[i * 4 + 2] = price;
			int type = readC();
			_items[i * 4 + 3] = type;
		}
	}
	
	@Override
	protected void runImpl()
	{
		if (_size < 1)
			return;
		
		ArrayList<CropProcure> crops = new ArrayList<>();
		for (int i = 0; i < _size; i++)
		{
			int id = _items[i * 4 + 0];
			int sales = _items[i * 4 + 1];
			int price = _items[i * 4 + 2];
			int type = _items[i * 4 + 3];
			if (id > 0)
			{
				CropProcure s = CastleManorManager.getInstance().getNewCropProcure(id, sales, type, price, sales);
				crops.add(s);
			}
		}
		
		CastleManager.getInstance().getCastleById(_manorId).setCropProcure(crops, CastleManorManager.PERIOD_NEXT);
		if (Config.ALT_MANOR_SAVE_ALL_ACTIONS)
			CastleManager.getInstance().getCastleById(_manorId).saveCropData(CastleManorManager.PERIOD_NEXT);
	}
	
	@Override
	public String getType()
	{
		return _C__D0_0B_REQUESTSETCROP;
	}
}