package com.l2jhellas.mmocore.network;

public interface IClientFactory<T extends MMOClient<?>>
{
	public T create(final MMOConnection<T> con);
}