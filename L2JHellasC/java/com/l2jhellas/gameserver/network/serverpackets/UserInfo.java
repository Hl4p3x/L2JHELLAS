package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.enums.ZoneId;
import com.l2jhellas.gameserver.enums.skills.AbnormalEffect;
import com.l2jhellas.gameserver.instancemanager.CursedWeaponsManager;
import com.l2jhellas.gameserver.model.actor.L2Summon;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.Inventory;

public class UserInfo extends L2GameServerPacket
{
	private static final String _S__04_USERINFO = "[S] 04 UserInfo";
	private final L2PcInstance _activeChar;
	private int _relation;
	
	public UserInfo(L2PcInstance character)
	{
		_activeChar = character;
		_relation = getRelation(_activeChar);
	}
	
	private int getRelation(L2PcInstance activeChar)
	{
		int relation = activeChar.isClanLeader() ? 0x40 : 0;
		
		if (activeChar.getSiegeState() == 1)
			relation |= 0x180;
		if (activeChar.getSiegeState() == 2)
			relation |= 0x80;
		
		return relation;
	}
	
	@Override
	protected final void writeImpl()
	{
		
		if (_activeChar == null)
			return;
		
		if (_activeChar != getClient().getActiveChar())
			return;
		
		writeC(0x04);
		
		writeD(_activeChar.getX());
		writeD(_activeChar.getY());
		writeD(_activeChar.getZ());
		writeD(_activeChar.getVehicle() != null ? _activeChar.getVehicle().getObjectId() : 0);
		
		writeD(_activeChar.getObjectId());
		writeS(_activeChar.getName());
		writeD(_activeChar.getRace().ordinal());
		writeD(_activeChar.getAppearance().getSex().ordinal());
		writeD(_activeChar.getClassIndex() == 0 ? _activeChar.getClassId().getId() : _activeChar.getBaseClass());
		
		writeD(_activeChar.getLevel());
		writeQ(_activeChar.getExp());
		writeD(_activeChar.getSTR());
		writeD(_activeChar.getDEX());
		writeD(_activeChar.getCON());
		writeD(_activeChar.getINT());
		writeD(_activeChar.getWIT());
		writeD(_activeChar.getMEN());
		writeD(_activeChar.getMaxHp());
		writeD((int)_activeChar.getCurrentHp());
		writeD(_activeChar.getMaxMp());
		writeD((int)_activeChar.getCurrentMp());
		writeD(_activeChar.getSp());
		writeD(_activeChar.getCurrentLoad());
		writeD(_activeChar.getMaxLoad());
		writeD(_activeChar.getActiveWeaponItem() != null ? 40 : 20); // 20 no weapon, 40 weapon equipped
		
		writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_HAIRALL));
		writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_REAR));
		writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LEAR));
		writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_NECK));
		writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_RFINGER));
		writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LFINGER));
		writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_HEAD));
		writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_RHAND));
		writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LHAND));
		writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_GLOVES));
		writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_CHEST));
		writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LEGS));
		writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_FEET));
		writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_BACK));
		writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_RHAND));
		writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_HAIR));
		writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_FACE));
		
		writeD(_activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_HAIRALL));
		writeD(_activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_REAR));
		writeD(_activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LEAR));
		writeD(_activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_NECK));
		writeD(_activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_RFINGER));
		writeD(_activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LFINGER));
		writeD(_activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_HEAD));
		writeD(_activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
		writeD(_activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LHAND));
		writeD(_activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_GLOVES));
		writeD(_activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_CHEST));
		writeD(_activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LEGS));
		writeD(_activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_FEET));
		writeD(_activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_BACK));
		writeD(_activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
		writeD(_activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_HAIR));
		writeD(_activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_FACE));
		
		// c6 new h's
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeD(_activeChar.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_RHAND));
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeD(_activeChar.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_LHAND));
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		// end of c6 new h's
		
		writeD(_activeChar.getPAtk(null));
		writeD(_activeChar.getPAtkSpd());
		writeD(_activeChar.getPDef(null));
		writeD(_activeChar.getEvasionRate(null));
		writeD(_activeChar.getAccuracy());
		writeD(_activeChar.getCriticalHit(null, null));
		writeD(_activeChar.getMAtk(null, null));		
		writeD(_activeChar.getMAtkSpd());
		writeD(_activeChar.getPAtkSpd());	
		writeD(_activeChar.getMDef(null, null));
		
		writeD(_activeChar.getPvpFlag()); // 0-non-pvp 1-pvp = violett name
		writeD(_activeChar.getKarma());

		final int runSpd =  (int)Math.round(_activeChar.getRunSpeed() / _activeChar.getMovementSpeedMultiplier());
		final int walkSpd =  (int)Math.round(_activeChar.getWalkSpeed() / _activeChar.getMovementSpeedMultiplier());
		final int swimSpd = (int)Math.round(_activeChar.getMoveSpeed() / _activeChar.getMovementSpeedMultiplier());	
		
		writeD(runSpd);
		writeD(walkSpd);
		writeD(swimSpd);
		writeD(swimSpd);
		writeD(0);
		writeD(0);
		writeD((_activeChar.isFlying()) ? runSpd : 0);
		writeD((_activeChar.isFlying()) ? walkSpd : 0);
		
		writeF(_activeChar.getStat().getMovementSpeedMultiplier());
		writeF(_activeChar.getStat().getAttackSpeedMultiplier());
		
		L2Summon pet = _activeChar.getPet();
		if (_activeChar.getMountType() != 0 && pet != null)
		{
			writeF(pet.getTemplate().getCollisionRadius());
			writeF(pet.getTemplate().getCollisionHeight());
		}
		else
		{
			writeF(_activeChar.getBaseTemplate().getCollisionRadius(_activeChar.getAppearance().getSex()));
			writeF(_activeChar.getBaseTemplate().getCollisionHeight(_activeChar.getAppearance().getSex()));
		}
		
		writeD(_activeChar.getAppearance().getHairStyle());
		writeD(_activeChar.getAppearance().getHairColor());
		writeD(_activeChar.getAppearance().getFace());
		writeD(_activeChar.isGM() ? 1 : 0); // builder level
		
		String title = _activeChar.getTitle();
		
		if (!_activeChar.getAppearance().isVisible() && _activeChar.isGM())
			title = "Invisible";
		
		writeS((_activeChar.getPoly().isMorphed()) ? "Morphed" : title);
		
		writeD(_activeChar.getClanId());
		writeD(_activeChar.getClanCrestId());
		writeD(_activeChar.getAllyId());
		writeD(_activeChar.getAllyCrestId()); // ally crest id
		writeD(_relation); 	// 0x40 leader rights // siege flags: attacker - 0x180 sword over name, defender - 0x80 shield, 0xC0 crown (|leader), 0x1C0 flag (|leader)
		writeC(_activeChar.getMountType()); // mount type
		writeC(_activeChar.getPrivateStoreType().getId());
		writeC(_activeChar.hasDwarvenCraft() ? 1 : 0);
		writeD(_activeChar.getPkKills());
		writeD(_activeChar.getPvpKills());

		writeH(_activeChar.getCubics().size());
		_activeChar.getCubics().keySet().forEach(this::writeH);
		
		writeC(_activeChar.isInPartyMatchRoom() ? 1 : 0);
		
		writeD(!_activeChar.getAppearance().isVisible() && _activeChar.isGM() ? _activeChar.getAbnormalEffect() | AbnormalEffect.STEALTH.getMask() : _activeChar.getAbnormalEffect());
		
		writeC(_activeChar.isInsideZone(ZoneId.WATER) ? 1 : _activeChar.isFlying() ? 2 : 0);
		
		writeD(_activeChar.getClanPrivileges());
		
		writeH(_activeChar.getRecomLeft()); // c2 recommendations remaining
		writeH(_activeChar.getRecomHave()); // c2 recommendations received
		writeD(_activeChar.getMountObjectID() > 0 ? _activeChar.getMountObjectID() + 1000000 : 0);
		writeH(_activeChar.getInventoryLimit());
		
		writeD(_activeChar.getClassId().getId());
		writeD(0x00); // special effects? circles around player...
		writeD(_activeChar.getMaxCp());
		writeD((int) _activeChar.getCurrentCp());
		writeC(_activeChar.isMounted() ? 0 : _activeChar.getEnchantEffect());
		
		writeC(_activeChar.getTeam().getId());
		
		writeD(_activeChar.getClanCrestLargeId());
		writeC(_activeChar.isNoble() ? 1 : 0); // 0x01: symbol on char menu ctrl+I
		writeC((_activeChar.isHero() || (_activeChar.isGM() && Config.GM_HERO_AURA)) ? 1 : 0); // 0x01: Hero Aura
		
		writeC(_activeChar.isFishing() ? 1 : 0); // Fishing Mode
		writeD(_activeChar.GetFishx()); // fishing x
		writeD(_activeChar.GetFishy()); // fishing y
		writeD(_activeChar.GetFishz()); // fishing z
		
		writeD(_activeChar.getAppearance().getNameColor());
		
		writeC(_activeChar.isRunning() ? 0x01 : 0x00); // changes the Speed display on Status Window
		
		writeD(_activeChar.getPledgeClass()); // changes the text above CP on Status Window
		writeD(_activeChar.getPledgeType());
		
		writeD(_activeChar.getAppearance().getTitleColor());

		writeD(_activeChar.isCursedWeaponEquiped() ? CursedWeaponsManager.getInstance().getLevel(_activeChar.getCursedWeaponEquipedId()) : 0);		
	}
	
	@Override
	public String getType()
	{
		return _S__04_USERINFO;
	}
}