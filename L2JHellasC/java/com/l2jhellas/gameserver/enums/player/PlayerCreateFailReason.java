package com.l2jhellas.gameserver.enums.player;

/**
 * @author AbsolutePower
 */
public enum PlayerCreateFailReason
{
	REASON_CREATION_FAILED(0x00),
	REASON_TOO_MANY_CHARACTERS(0x01),
	REASON_NAME_ALREADY_EXISTS(0x02),
	REASON_16_ENG_CHARS(0x03),
	REASON_INCORRECT_NAME(0x04),
	REASON_CREATE_NOT_ALLOWED(0x05),
	REASON_CHOOSE_ANOTHER_SVR(0x06);
	
	private final int _reason;
	
	private PlayerCreateFailReason(int reason)
	{
		_reason = reason;
	}

	public int getReason()
	{
		return _reason;
	}
}