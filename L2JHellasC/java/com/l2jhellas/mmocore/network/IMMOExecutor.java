package com.l2jhellas.mmocore.network;

public interface IMMOExecutor<T extends MMOClient<?>>
{
	public void execute(ReceivablePacket<T> packet);
}