package com.l2jhellas.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.io.Reader;

public class UnicodeReader extends Reader
{
	private final PushbackInputStream internalIn;
	private InputStreamReader internalIn2 = null;
	private final String defaultEnc;
	private static final int BOM_SIZE = 4;
	
	public UnicodeReader(final InputStream in, final String defaultEnc)
	{
		internalIn = new PushbackInputStream(in, BOM_SIZE);
		this.defaultEnc = defaultEnc;
	}
	
	public String getDefaultEncoding()
	{
		return defaultEnc;
	}
	
	public String getEncoding()
	{
		if (internalIn2 == null)
			return null;
		
		return internalIn2.getEncoding();
	}

	protected void init() throws IOException
	{
		if (internalIn2 != null)
			return;
		
		final String encoding;
		final byte[] bom = new byte[BOM_SIZE];
		final int n;
		final int unread;
		n = internalIn.read(bom, 0, bom.length);
		
		if (bom[0] == (byte) 0xEF && bom[1] == (byte) 0xBB && bom[2] == (byte) 0xBF)
		{
			encoding = "UTF-8";
			unread = n - 3;
		}
		else if (bom[0] == (byte) 0xFE && bom[1] == (byte) 0xFF)
		{
			encoding = "UTF-16BE";
			unread = n - 2;
		}
		else if (bom[0] == (byte) 0xFF && bom[1] == (byte) 0xFE)
		{
			encoding = "UTF-16LE";
			unread = n - 2;
		}
		else if (bom[0] == (byte) 0x00 && bom[1] == (byte) 0x00 && bom[2] == (byte) 0xFE && bom[3] == (byte) 0xFF)
		{
			encoding = "UTF-32BE";
			unread = n - 4;
		}
		else if (bom[0] == (byte) 0xFF && bom[1] == (byte) 0xFE && bom[2] == (byte) 0x00 && bom[3] == (byte) 0x00)
		{
			encoding = "UTF-32LE";
			unread = n - 4;
		}
		else
		{
			// Unicode BOM mark not found, unread all bytes
			encoding = defaultEnc;
			unread = n;
		}
		
		if (unread > 0)
			internalIn.unread(bom, (n - unread), unread);
		
		// Use given encoding
		internalIn2 = encoding == null ? new InputStreamReader(internalIn) : new InputStreamReader(internalIn, encoding);
	}
	
	@Override
	public void close() throws IOException
	{
		init();
		internalIn.close();
		internalIn2.close();
	}
	
	@Override
	public int read(final char[] cbuf, final int off, final int len) throws IOException
	{
		init();
		return internalIn2.read(cbuf, off, len);
	}
}