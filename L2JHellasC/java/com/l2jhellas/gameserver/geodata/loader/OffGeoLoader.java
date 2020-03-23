package com.l2jhellas.gameserver.geodata.loader;

import java.io.ByteArrayOutputStream;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.l2jhellas.gameserver.geodata.GeoEngine;

public class OffGeoLoader extends AbstractGeoLoader
{
	
	protected static final Logger log = Logger.getLogger(OffGeoLoader.class.getName());
	
	private static final Pattern PATTERN = Pattern.compile("[\\d]{2}_[\\d]{2}_conv.dat");
	
	@Override
	protected byte[][] parse(byte[] data)
	{
		
		// 18 + ((256 * 256) * (2 * 3)) - it's minimal size of geodata (whole region with flat blocks)
		if (data.length <= 393234)
		{
			return null;
		}
		
		// Indexing geo files, so we will know where each block starts
		int index = 18; // Skip firs 18 bytes, they have nothing with data;
		
		byte[][] blocks = new byte[65536][]; // 256 * 256
		
		for (int block = 0, n = blocks.length; block < n; block++)
		{
			short type = makeShort(data[index + 1], data[index]);
			index += 2;
			
			byte[] geoBlock;
			
			switch (type)
			{
				case 0:
					geoBlock = new byte[2 + 1];
					
					geoBlock[0] = GeoEngine.BLOCKTYPE_FLAT;
					geoBlock[1] = data[index + 2];
					geoBlock[2] = data[index + 3];
					
					blocks[block] = geoBlock;
					index += 4;
					break;
				
				case 0x0040:
					geoBlock = new byte[128 + 1];
					
					geoBlock[0] = GeoEngine.BLOCKTYPE_COMPLEX;
					System.arraycopy(data, index, geoBlock, 1, 128);
					
					index += 128;
					
					blocks[block] = geoBlock;
					break;
				default:
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					baos.write(GeoEngine.BLOCKTYPE_MULTILEVEL);
					
					for (int b = 0; b < 64; b++)
					{
						byte layers = (byte) makeShort(data[index + 1], data[index]);
						if (layers > GeoEngine.MAX_LAYERS)
							GeoEngine.MAX_LAYERS = layers;
						
						index += 2;
						
						baos.write(layers);
						for (int i = 0; i < layers << 1; i++)
						{
							baos.write(data[index++]);
						}
					}
					blocks[block] = baos.toByteArray();
					break;
			}
		}
		
		return blocks;
	}
	
	protected short makeShort(byte b1, byte b0)
	{
		return (short) (b1 << 8 | b0 & 0xff);
	}
	
	@Override
	public Pattern getPattern()
	{
		return PATTERN;
	}
	
	@Override
	public byte[] convert(byte[] data)
	{
		return data;
	}
}
