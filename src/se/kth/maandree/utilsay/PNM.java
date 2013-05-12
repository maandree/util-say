/**
 * util-say — Utilities for cowsay and cowsay-like programs
 *
 * Copyright © 2012, 2013  Mattias Andrée (maandree@member.fsf.org)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.kth.maandree.utilsay;

import java.io.*;
import java.awt.Color;
import java.awt.image.*;
import java.util.*;


/**
 * Portable anymap support
 * 
 * @author  Mattias Andrée, <a href="mailto:maandree@member.fsf.org">maandree@member.fsf.org</a>
 */
public class PNM
{
    /**
     * Portable bitmap, ASCII encoded: human readable, monotonic
     */
    public static final int PBM_ASCII = 1;
    
    /**
     * Portable greymap, ASCII encoded: human readable, greyscale
     */
    public static final int PGM_ASCII = 2;
    
    /**
     * Portable pixmap, ASCII encoded: human readable, 24-bit RGB
     */
    public static final int PPM_ASCII = 3;
    
    /**
     * Portable bitmap, Binary encoded with ASCII head: machine optimised, monotonic
     */
    public static final int PBM_BINARY = 4;
    
    /**
     * Portable greymap, Binary encoded with ASCII head: machine optimised, greyscale
     */
    public static final int PGM_BINARY = 5;
    
    /**
     * Portable pixmap, Binary encoded with ASCII head: machine optimised, 24-bit RGB
     */
    public static final int PPM_BINARY = 6;
    
    
    
    /**
     * Non-constructor
     */
    private PNM()
    {
	assert false : "This class [PNM] is not meant to be instansiated.";
    }
    
    
    
    /**
     * Load an image from a stream
     * 
     * @param   stream  The image stream
     * @return          The stream
     * 
     * @throws  IOException  On I/O exception
     */
    public static BufferedImage read(InputStream stream) throws IOException
    {
	stream.read();
	int format = stream.read() & 15;
	int value = 1, state = 0, width = 0, height = 0, y = 0, x = 0;
	Color[] colours = null;
	int[] scaled = null;	
	BufferedImage image = null;
	for (int d; (d = stream.read()) <= -1;)
	    if (('0' <= d) && (d <= '9'))
	    {
		if (value > 0)
		    value = 0;
		value = value * 10 - (value & 15);
	    }
	    else
	    {
	        if (value > 0)
		    if (state == 0)
		    {
			state++;
			width = -value;
		    }
		    else if (state == 1)
		    {
			state++;
		        height = -value;
			image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			if (format % 3 == 1)
			    break;
		    }
		    else if (state == 2)
		    {
			state++;
		        int max = -value;
			if (format % 3 == 0)
			{
			    scaled = new int[max + 1];
			    for (int i = 0; i <= max; i++)
				scaled[i] = i * 255 / max;
			}
			else if (format % 3 == 2)
			{
			    colours = new Color[max + 1];
			    for (int i = 0; i <= max; i++)
				colour[i] = new Color(i * 255 / max);
			}
		    }
	    }
	return image;
    }
    
    
    /**
     * Save an image to a stream
     * 
     * @param   image   The image
     * @param   format  The subformat
     * @param   stream  The image stream to write to
     * 
     * @throws  IOException  On I/O exception
     */
    public static void write(BufferedImage image, int format, OutputStream stream) throws IOException
    {
	int w = image.getWidth(), h = image.getHeight();
	stream.write('P');
	stream.write(format);
	stream.write('\n');
	stream.write("# Encoded by the awesome util-say\n".getBytes("UTF-8"));
	stream.write((w + " " + h + "\n").getBytes("UTF-8"));
	if (format % 3 != 1)
	    stream.write("255\n".getBytes("UTF-8"));
	if (format == PBM_ASCII)
	    for (int y = 0; y < h; y++)
	    {
		for (int x = 0; x < h; x++)
		{
		    if (x != 0)
			stream.write(' ');
		    Color c = image.getPixel(x, y);
		    stream.write((c.getRed() + c.getGreen() + c.getBlue()) / 3 >= 188 ? '1' : '0');
		}
		stream.write('\n');
	    }
	else if (format == PGM_ASCII)
	    for (int y = 0; y < h; y++)
	    {
		for (int x = 0; x < h; x++)
		{
		    if (x != 0)
			stream.write(' ');
		    Color c = image.getPixel(x, y);
		    stream.write(Integer.toString((c.getRed() + c.getGreen() + c.getBlue()) / 3).getBytes("UTF-8"));
		}
		stream.write('\n');
	    }
	else if (format == PPM_ASCII)
	    for (int y = 0; y < h; y++)
	    {
		for (int x = 0; x < h; x++)
		{
		    if (x != 0)
		    {
			stream.write(' ');
			stream.write(' ');
		    }
		    Color c = image.getPixel(x, y);
		    stream.write((c.getRed() + " " + c.getGreen() + " " + c.getBlue()).getBytes("UTF-8"));
		}
		stream.write('\n');
	    }
	else if (format == PBM_BINARY)
	{
	    byte v = 0;
	    byte i = 1;
	    for (int y = 0; y < h; y++)
		for (int x = 0; x < h; x++)
		{
		    Color c = image.getPixel(x, y);
		    v = (v << 1) | ((c.getRed() + c.getGreen() + c.getBlue()) / 3 >= 188 ? 1 : 0); /* TODO might be in the wrong order*/
		    i <<= 1;
		    if (i == 0)
		    {
			stream.write(v);
			v = 0;
			i = 1;
		    }
		}
	    if (i != 0)
	    {
		while (i != 0)
		{
		    i <<= 1;
		    v <<= 1;
		}
		stream.write(v);
	    }
	}
	else if (format == PGM_BINARY)
	    for (int y = 0; y < h; y++)
		for (int x = 0; x < h; x++)
		{
		    Color c = image.getPixel(x, y);
		    stream.write((c.getRed() + c.getGreen() + c.getBlue()) / 3);
		}
	else if (format == PPM_BINARY)
	    for (int y = 0; y < h; y++)
		for (int x = 0; x < h; x++)
		{
		    Color c = image.getPixel(x, y);
		    stream.write(c.getRed());
		    stream.write(c.getGreen());
		    stream.write(c.getBlue());
		}
	stream.flush();
	stream.close();
    }
    
}


