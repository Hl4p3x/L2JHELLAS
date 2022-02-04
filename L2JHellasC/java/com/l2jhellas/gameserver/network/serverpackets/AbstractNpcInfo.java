package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.datatables.sql.ClanTable;
import com.l2jhellas.gameserver.enums.PolyType;
import com.l2jhellas.gameserver.enums.ZoneId;
import com.l2jhellas.gameserver.model.L2Clan;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.L2Summon;
import com.l2jhellas.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PetInstance;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;

public abstract class AbstractNpcInfo extends L2GameServerPacket
{
	protected int _x, _y, _z, _heading;
	protected int _idTemplate;
	protected int _mAtkSpd, _pAtkSpd;
	protected int _runSpd, _walkSpd;
	protected int _rhand, _lhand, _chest, _enchantEffect;
	protected int _clanCrest, _allyCrest, _allyId, _clanId;
	
	protected double _collisionHeight, _collisionRadius;
	
	protected boolean _isAttackable, _isSummoned;
	
	protected String _name = "", _title = "";
	
	public AbstractNpcInfo(L2Character cha)
	{
		_isSummoned = cha.isShowSummonAnimation();
		_x = cha.getX();
		_y = cha.getY();
		_z = cha.getZ();
		_heading = cha.getHeading();
		_mAtkSpd = cha.getMAtkSpd();
		_pAtkSpd = cha.getPAtkSpd();
		_runSpd = cha.getStat().getRunSpeed();
		_walkSpd = cha.getStat().getWalkSpeed();
	}


	public static class PolymorphInfo extends AbstractNpcInfo
	{
		private final L2PcInstance _pc;
		private final L2NpcTemplate _template;
		private final int _swimSpd;
		
		public PolymorphInfo(L2PcInstance cha, L2NpcTemplate template)
		{
			super(cha);
			_pc = cha;
			_template = template;
			
			_swimSpd = cha.getStat().getRunSpeed();
			
			_rhand = _template.rhand;
			_lhand = _template.lhand;
			
			_collisionHeight = _template.getCollisionHeight();
			_collisionRadius = _template.getCollisionRadius();
			
			_enchantEffect = 0;
		}
		
		@Override
		protected void writeImpl()
		{
			writeC(0x16);
			
			writeD(_pc.getObjectId());
			writeD(_pc.getPoly().getPolyId() + 1000000);
			writeD(1);
			
			writeD(_x);
			writeD(_y);
			writeD(_z);
			writeD(_heading);
			
			writeD(0x00);
			
			writeD(_mAtkSpd);
			writeD(_pAtkSpd);
			writeD(_runSpd);
			writeD(_walkSpd);
			writeD(_swimSpd);
			writeD(_swimSpd);
			writeD(_runSpd);
			writeD(_walkSpd);
			writeD(_runSpd);
			writeD(_walkSpd);
			
			writeF(_pc.getStat().getMovementSpeedMultiplier());
			writeF(_pc.getStat().getAttackSpeedMultiplier());
			
			writeF(_collisionRadius);
			writeF(_collisionHeight);
			
			writeD(_rhand);
			writeD(0);
			writeD(_lhand);
			
			writeC(1); 
			writeC(_pc.isRunning() ? 1 : 0);
			writeC(_pc.isInCombat() ? 1 : 0);
			writeC(_pc.isAlikeDead() ? 1 : 0);
			writeC(0); 
			
			writeS(_name);
			writeS(_title);
			
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			
			writeD(_pc.getAbnormalEffect());
			
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			
			writeC((_pc.isInsideZone(ZoneId.WATER)) ? 1 : (_pc.isFlying()) ? 2 : 0);
			writeC(_pc.getTeam().getId());
			
			writeF(_collisionRadius);
			writeF(_collisionHeight);
			
			writeD(_enchantEffect);
			writeD(0x00);
		}
	}
	
	public static class NpcInfo extends AbstractNpcInfo
	{
		private final L2Npc _npc;
		
		public NpcInfo(L2Npc cha, L2Character attacker)
		{
			super(cha);
			_npc = cha;
			
			_enchantEffect = 0;
			_isAttackable = _npc.isAutoAttackable(attacker);

			if (_npc.getPoly().getPolyType() == PolyType.NPC)
			{
				_idTemplate = _npc.getPoly().getPolyTemplate().idTemplate;
				_rhand = _npc.getPoly().getPolyTemplate().rhand;
				_lhand = _npc.getPoly().getPolyTemplate().lhand;
				_collisionHeight = _npc.getPoly().getPolyTemplate().getCollisionHeight();
				_collisionRadius = _npc.getPoly().getPolyTemplate().getCollisionRadius();
			}
			else
			{
				_idTemplate = _npc.getTemplate().idTemplate;
				_rhand = _npc.getRightHandItem();
				_lhand = _npc.getLeftHandItem();
				_collisionHeight = _npc.getCollisionHeight();
				_collisionRadius = _npc.getCollisionRadius();
			}
			
			if (_npc.getTemplate().serverSideName)
				_name = _npc.getTemplate().name;
			
			if (_npc.isChampion())
				_title = "Champion";
			else if (cha.getTemplate().serverSideTitle)
				_title = cha.getTemplate().title;
			
			if (Config.SHOW_NPC_LVL && _npc instanceof L2MonsterInstance)
				_title = "Lv " + _npc.getLevel() + (_npc.getTemplate().aggroRange > 0 ? "* " : " ") + _title;
			
			if (Config.SHOW_NPC_CREST && _npc.getCastle() != null && _npc.getCastle().getOwnerId() != 0)
			{
				L2Clan clan = ClanTable.getInstance().getClan(_npc.getCastle().getOwnerId());
				_clanCrest = clan.getCrestId();
				_clanId = clan.getClanId();
				_allyCrest = clan.getAllyCrestId();
				_allyId = clan.getAllyId();
			}
		}
		
		@Override
		protected void writeImpl()
		{
			writeC(0x16);
			
			writeD(_npc.getObjectId());
			writeD(_idTemplate + 1000000);
			writeD(_isAttackable ? 1 : 0);
			
			writeD(_x);
			writeD(_y);
			writeD(_z);
			writeD(_heading);
			
			writeD(0x00);
			
			writeD(_mAtkSpd);
			writeD(_pAtkSpd);
			writeD(_runSpd);
			writeD(_walkSpd);
			writeD(_runSpd);
			writeD(_walkSpd);
			writeD(_runSpd);
			writeD(_walkSpd);
			writeD(_runSpd);
			writeD(_walkSpd);
			
			writeF(_npc.getStat().getMovementSpeedMultiplier());
			writeF(_npc.getStat().getAttackSpeedMultiplier());
			
			writeF(_collisionRadius);
			writeF(_collisionHeight);
			
			writeD(_rhand);
			writeD(_chest);
			writeD(_lhand);
			
			writeC(1); 
			writeC(_npc.isRunning() ? 1 : 0);
			writeC(_npc.isInCombat() ? 1 : 0);
			writeC(_npc.isAlikeDead() ? 1 : 0);
			writeC(_isSummoned ? 2 : 0);
			
			writeS(_name);
			writeS(_title);
			
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			
			writeD(_npc.getAbnormalEffect());
			
			writeD(_clanId);
			writeD(_clanCrest);
			writeD(_allyId);
			writeD(_allyCrest);
			
			writeC((_npc.isInsideZone(ZoneId.WATER)) ? 1 : (_npc.isFlying()) ? 2 : 0);
			writeC(0x00);
			
			writeF(_collisionRadius);
			writeF(_collisionHeight);
			
			writeD(_enchantEffect);
			writeD(_npc.isFlying() ? 1 : 0);
		}
	}

	public static class SummonInfo extends AbstractNpcInfo
	{
		private final L2Summon _summon;
		private final L2PcInstance _owner;
		private int _summonAnimation = 0;
		
		public SummonInfo(L2Summon cha, L2PcInstance attacker, int val)
		{
			super(cha);
			
			_summon = cha;
			_owner = _summon.getOwner();
			
			_summonAnimation = val;
			
			if (_summon.isShowSummonAnimation())
				_summonAnimation = 2;
				
			_isAttackable = _summon.isAutoAttackable(attacker);
			_rhand = _summon.getWeapon();
			_lhand = 0;
			_chest = _summon.getArmor();
			_enchantEffect = 0;
			_title = (_owner == null || !_owner.isOnline()) ? "" : _owner.getName();
			_idTemplate = _summon.getTemplate().idTemplate;
			
			_collisionHeight = _summon.getTemplate().getCollisionHeight();
			_collisionRadius = _summon.getTemplate().getCollisionRadius();
			
			if (Config.SHOW_NPC_CREST && _owner != null && _owner.getClan() != null)
			{
				L2Clan clan = ClanTable.getInstance().getClan(_owner.getClanId());
				_clanCrest = clan.getCrestId();
				_clanId = clan.getClanId();
				_allyCrest = clan.getAllyCrestId();
				_allyId = clan.getAllyId();
			}
		}
		
		@Override
		protected void writeImpl()
		{
			if (_owner != null && !_owner.getAppearance().isVisible())
				return;
			
			writeC(0x16);
			
			writeD(_summon.getObjectId());
			writeD(_idTemplate + 1000000);
			writeD(_isAttackable ? 1 : 0);
			
			writeD(_x);
			writeD(_y);
			writeD(_z);
			writeD(_heading);
			
			writeD(0x00);
			
			writeD(_mAtkSpd);
			writeD(_pAtkSpd);
			writeD(_runSpd);
			writeD(_walkSpd);
			writeD(_runSpd);
			writeD(_walkSpd);
			writeD(_runSpd);
			writeD(_walkSpd);
			writeD(_runSpd);
			writeD(_walkSpd);
			
			writeF(_summon.getStat().getMovementSpeedMultiplier());
			writeF(_summon.getStat().getAttackSpeedMultiplier());
			
			writeF(_collisionRadius);
			writeF(_collisionHeight);
			
			writeD(_rhand);
			writeD(_chest);
			writeD(_lhand);
			
			writeC(1);
			writeC(_summon.isRunning() ? 1 : 0);
			writeC(_summon.isInCombat() ? 1 : 0);
			writeC(_summon.isAlikeDead() ? 1 : 0);
			writeC(_summonAnimation);
			
			writeS(_name);
			writeS(_title);
			
			writeD(_summon instanceof L2PetInstance ? 0x00 : 0x01);
			writeD(_summon.getPvpFlag());
			writeD(_summon.getKarma());
			
			writeD(_summon.getAbnormalEffect());
			
			writeD(_clanId);
			writeD(_clanCrest);
			writeD(_allyId);
			writeD(_allyCrest);
			
			writeC((_summon.isInsideZone(ZoneId.WATER)) ? 1 : (_summon.isFlying()) ? 2 : 0);
			writeC(_summon.getTeam());
			
			writeF(_collisionRadius);
			writeF(_collisionHeight);
			
			writeD(_enchantEffect);
			writeD(0x00);
		}
	}
}