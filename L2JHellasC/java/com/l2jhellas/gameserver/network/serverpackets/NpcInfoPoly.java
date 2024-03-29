package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.datatables.sql.NpcData;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;

public class NpcInfoPoly extends L2GameServerPacket
{
	// ddddddddddddddddddffffdddcccccSSddd dddddc
	private static final String _S__22_NPCINFO = "[S] 16 NpcInfo";
	private L2Character _activeChar;
	private final L2Object _obj;
	private int _x, _y, _z, _heading;
	private final int _npcId;
	private boolean _isAttackable;
	private final boolean _isSummoned;
	private boolean _isRunning;
	private boolean _isInCombat;
	private boolean _isAlikeDead;
	private int _mAtkSpd, _pAtkSpd;
	private int _runSpd, _walkSpd, _swimRunSpd, _swimWalkSpd, _flRunSpd, _flWalkSpd, _flyRunSpd, _flyWalkSpd;
	private int _rhand, _lhand;
	private String _name, _title;
	private int _abnormalEffect;
	L2NpcTemplate _template;
	private final int _collisionRadius;
	private final int _collisionHeight;
	
	public NpcInfoPoly(L2Object obj, L2Character attacker)
	{
		_obj = obj;
		_npcId = obj.getPoly().getPolyId();
		_template = NpcData.getInstance().getTemplate(_npcId);
		_isAttackable = true;
		_rhand = 0;
		_lhand = 0;
		_isSummoned = false;
		_collisionRadius = (int)_template.collisionRadius;
		_collisionHeight = (int)_template.collisionHeight;
		if (_obj instanceof L2Character)
		{
			_activeChar = (L2Character) obj;
			_isAttackable = obj.isAutoAttackable(attacker);
			_rhand = _template.rhand;
			_lhand = _template.lhand;
			
		}
		
		if (_obj instanceof L2ItemInstance)
		{
			_x = _obj.getX();
			_y = _obj.getY();
			_z = _obj.getZ();
			_heading = 0;
			_mAtkSpd = 100; // yes, an item can be dread as death
			_pAtkSpd = 100;
			_runSpd = 120;
			_walkSpd = 80;
			_swimRunSpd = _flRunSpd = _flyRunSpd = _runSpd;
			_swimWalkSpd = _flWalkSpd = _flyWalkSpd = _walkSpd;
			_isRunning = _isInCombat = _isAlikeDead = false;
			_name = "item";
			_title = "polymorphed";
			_abnormalEffect = 0;
		}
		else
		{
			_x = _activeChar.getX();
			_y = _activeChar.getY();
			_z = _activeChar.getZ();
			_heading = _activeChar.getHeading();
			_mAtkSpd = _activeChar.getMAtkSpd();
			_pAtkSpd = _activeChar.getPAtkSpd();
			_runSpd = _activeChar.getRunSpeed();
			_walkSpd = _activeChar.getWalkSpeed();
			_swimRunSpd = _flRunSpd = _flyRunSpd = _runSpd;
			_swimWalkSpd = _flWalkSpd = _flyWalkSpd = _walkSpd;
			_isRunning = _activeChar.isRunning();
			_isInCombat = _activeChar.isInCombat();
			_isAlikeDead = _activeChar.isAlikeDead();
			_name = _activeChar.getName();
			_title = _activeChar.getTitle();
			_abnormalEffect = _activeChar.getAbnormalEffect();
			
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x16);
		writeD(_obj.getObjectId());
		writeD(_npcId + 1000000); // npctype id
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
		writeD(_swimRunSpd); // swimspeed
		writeD(_swimWalkSpd); // swimspeed
		writeD(_flRunSpd);
		writeD(_flWalkSpd);
		writeD(_flyRunSpd);
		writeD(_flyWalkSpd);
		writeF(1);
		writeF(1);
		writeF(_collisionRadius);
		writeF(_collisionHeight);
		writeD(_rhand); // right hand weapon
		writeD(0);
		writeD(_lhand); // left hand weapon
		writeC(1);// name above char 1=true ... ??
		writeC(_isRunning ? 1 : 0);
		writeC(_isInCombat ? 1 : 0);
		writeC(_isAlikeDead ? 1 : 0);
		writeC(_isSummoned ? 2 : 0); // invisible ?? 0=false 1=true 2=summoned (only works if model has a summon animation)
		writeS(_name);
		writeS(_title);
		writeD(0);
		writeD(0);
		writeD(0000); // hmm karma ??
		
		writeH(_abnormalEffect); // C2
		writeH(0x00); // C2
		writeD(0000); // C2
		writeD(0000); // C2
		writeD(0000); // C2
		writeD(0000); // C2
		writeC(0000); // C2
	}
	
	@Override
	public String getType()
	{
		return _S__22_NPCINFO;
	}
}