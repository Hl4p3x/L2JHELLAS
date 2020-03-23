package com.l2jhellas.gameserver.ai;

public class NextAction
{
	private final CtrlEvent _event;
	private final CtrlIntention _intention;
	private final Runnable _runnable;
	
	public NextAction(CtrlEvent event, CtrlIntention intention, Runnable runnable)
	{
		_event = event;
		_intention = intention;
		_runnable = runnable;
	}
	
	public CtrlEvent getEvent()
	{
		return _event;
	}
	
	public CtrlIntention getIntention()
	{
		return _intention;
	}
	
	public void run()
	{
		_runnable.run();
	}
}