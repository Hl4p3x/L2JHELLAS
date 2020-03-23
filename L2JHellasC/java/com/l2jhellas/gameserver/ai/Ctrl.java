package com.l2jhellas.gameserver.ai;

import com.l2jhellas.gameserver.model.actor.L2Character;

public interface Ctrl
{
	
	L2Character getActor();
	
	CtrlIntention getIntention();
	
	L2Character getAttackTarget();
	
	void setIntention(CtrlIntention intention);
	
	void setIntention(CtrlIntention intention, Object arg0);
	
	void setIntention(CtrlIntention intention, Object arg0, Object arg1);
	
	void notifyEvent(CtrlEvent evt);
	
	void notifyEvent(CtrlEvent evt, Object arg0);
	
	void notifyEvent(CtrlEvent evt, Object arg0, Object arg1);
}