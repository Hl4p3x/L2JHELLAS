package com.l2jhellas.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.enums.items.ItemLocation;
import com.l2jhellas.gameserver.model.actor.instance.L2MerchantInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;

public class SellList extends L2GameServerPacket
{
	private static final String _S__10_SELLLIST = "[S] 10 SellList";
	private static Logger _log = Logger.getLogger(SellList.class.getName());
	private final L2PcInstance _activeChar;
	private final L2MerchantInstance _lease;
	private final int _money;
	private final List<L2ItemInstance> _selllist = new ArrayList<>();
	
	public SellList(L2PcInstance player)
	{
		_activeChar = player;
		_lease = null;
		_money = _activeChar.getAdena();
		doLease();
	}
	
	public SellList(L2PcInstance player, L2MerchantInstance lease)
	{
		_activeChar = player;
		_lease = lease;
		_money = _activeChar.getAdena();
		doLease();
	}
	
	private void doLease()
	{
		if (_lease == null)
		{
			for (L2ItemInstance item : _activeChar.getInventory().getItems())
			{
				if (!item.isEquipped() && item.getLocation() == ItemLocation.INVENTORY && item.getItem().isSellable() && (_activeChar.getPet() == null ||
				
				item.getObjectId() != _activeChar.getPet().getControlItemId()))
				{
					_selllist.add(item);
					if (Config.DEBUG)
						_log.fine("item added to selllist: " + item.getItem().getItemName());
				}
			}
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x10);
		writeD(_money);
		writeD(_lease == null ? 0x00 : 1000000 + _lease.getTemplate().npcId);
		
		writeH(_selllist.size());
		
		for (L2ItemInstance item : _selllist)
		{
			if (item == null || item.getItem() == null)
				continue;
			
			writeH(item.getItem().getType1());
			writeD(item.getObjectId());
			writeD(item.getItemId());
			writeD(item.getCount());
			writeH(item.getItem().getType2());
			writeH(0x00);
			writeD(item.getItem().getBodyPart());
			writeH(item.getEnchantLevel());
			writeH(0x00);
			writeH(0x00);
			
			if (_lease == null)
				writeD(item.getItem().getReferencePrice() / 2);
			// wtf??? there is no conditional part in SellList!!
			// this d should always be here 0.o! fortunately the lease stuff
			// are never ever use so the if always executes
		}
	}
	
	@Override
	public String getType()
	{
		return _S__10_SELLLIST;
	}
}