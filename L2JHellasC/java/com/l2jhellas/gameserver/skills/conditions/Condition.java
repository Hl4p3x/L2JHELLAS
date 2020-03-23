package com.l2jhellas.gameserver.skills.conditions;

import com.l2jhellas.gameserver.skills.Env;

public abstract class Condition implements ConditionListener
{
	private ConditionListener _listener;
	private String _msg;
	private boolean _result;
	
	public final void setMessage(String msg)
	{
		_msg = msg;
	}
	
	public final String getMessage()
	{
		return _msg;
	}
	
	void setListener(ConditionListener listener)
	{
		_listener = listener;
		notifyChanged();
	}
	
	final ConditionListener getListener()
	{
		return _listener;
	}
	
	public final boolean test(Env env)
	{
		boolean res = testImpl(env);
		if ((_listener != null) && res != _result)
		{
			_result = res;
			notifyChanged();
		}
		return res;
	}
	
	abstract boolean testImpl(Env env);
	
	@Override
	public void notifyChanged()
	{
		if (_listener != null)
			_listener.notifyChanged();
	}
}