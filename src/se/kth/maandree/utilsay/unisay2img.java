/**
 * unisay2img — unisay to image convertion tool
 *
 * Copyright © 2012  Mattias Andrée (maandree@kth.se)
 *
 * This library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.kth.maandree.utilunisay;

import javax.imageio.*;
import java.awt.*;
import java.awt.image.*;
import java.util.*;
import java.io.*;


/**
 * The main class of the unisay2img program
 *
 * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
 */
public class unisay2img
{
    public static final Color LINK_B = new Color(0, 0, 255, 128);
    public static final Color LINK_F = new Color(255, 255, 0, 128);
    
    
    
    /**
     * Non-constructor
     */
    private unisay2img()
    {
	assert false : "This class [unisay2img] is not meant to be instansiated.";
    }
    
    
    
    private static final ArrayList<int[]> pre = new ArrayList<int[]>();
    private static int[] prebuf = null;
    private static int preptr = 0;
    
    
    
    /**
     * This is the main entry point of the program
     * 
     * @param  args  Startup arguments, start the program with </code>--help</code> for details
     * 
     * @throws  IOException  On I/O exception
     */
    public static void main(final String... args) throws IOException
    {
	if (args.length == 0)
	{
	    System.out.println("unisay to image convertion tool");
	    System.out.println();
	    System.out.println("USAGE:  unisay2img [-2] [-c] [-l] [--] TARGET < SOURCE");
	    System.out.println();
	    System.out.println("Source (STDOUT):  Unisay pony file to convert");
	    System.out.println("Target:           File name for new image pony");
	    System.out.println();
	    System.out.println("-2  Output image should have double dimensioned pixels.");
	    System.out.println("-c  Output image should be cropped to drawing.");
	    System.out.println("-l  Encode baloon links.");
	    System.out.println();
	    System.out.println("Known supported input formats:");
	    System.out.println("  ⋅  PNG");
	    System.out.println();
	    System.out.println();
	    System.out.println("Copyright (C) 2012  Mattias Andrée <maandree@kth.se>");
	    System.out.println();
	    System.out.println("This program is free software: you can redistribute it and/or modify");
	    System.out.println("it under the terms of the GNU General Public License as published by");
	    System.out.println("the Free Software Foundation, either version 3 of the License, or");
	    System.out.println("(at your option) any later version.");
	    System.out.println();
	    System.out.println("This program is distributed in the hope that it will be useful,");
	    System.out.println("but WITHOUT ANY WARRANTY; without even the implied warranty of");
	    System.out.println("MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the");
	    System.out.println("GNU General Public License for more details.");
	    System.out.println();
	    System.out.println("You should have received a copy of the GNU General Public License");
	    System.out.println("along with this library.  If not, see <http://www.gnu.org/licenses/>.");
	    System.out.println();
	    System.out.println();
	    return;
	}
	
	boolean link = false;
	boolean crop = false;
	int ai = 0;
	int ps = 1;
	
	for (;; ai++)
	    if      (args[ai].equals("-2"))  ps = 2;
	    else if (args[ai].equals("-c"))  crop = true;
	    else if (args[ai].equals("-l"))  link = true;
	    else
		break;
	
	if (args[ai].equals("--"))
	    ai++;
	
	String file = args[ai++];
	
	Color osip = new Color(0xAA, 0xAA, 0xAA, 255);
	Color back = new Color(0, 0, 0, 0);
	Color fore = new Color(255, 255, 255, 255);
	
	final ArrayList<ArrayList<Color>> upper = new ArrayList<ArrayList<Color>>();
	final ArrayList<ArrayList<Color>> lower = new ArrayList<ArrayList<Color>>();
	
	for (;;)
	    if (System.in.read() == '\n')
		break;
	
	upper.add(new ArrayList<Color>());
	lower.add(new ArrayList<Color>());
	
	for (int d; (d = System.in.read()) != -1;)
        {
	    if (d == '$')
	    {
		d = System.in.read();
		
		if (d == '\\')
		    if (link)
		    {
			upper.get(upper.size() - 1).add(LINK_B);
			lower.get(lower.size() - 1).add(LINK_B);
		    }
		    else
		    {
			upper.get(upper.size() - 1).add(back == null ? osip : back);
			lower.get(lower.size() - 1).add(back == null ? osip : back);
		    }
		else if (d == '/')
		    if (link)
		    {
			upper.get(upper.size() - 1).add(LINK_F);
			lower.get(lower.size() - 1).add(LINK_F);
		    }
		    else
		    {
			upper.get(upper.size() - 1).add(back == null ? osip : back);
			lower.get(lower.size() - 1).add(back == null ? osip : back);
		    }
		else
		{
		    System.err.println("Not a pretty pony.  Stop. (0)");
		    System.exit(-1);
		}
		
		if (System.in.read() != '$')
		{
		    System.err.println("Not a pretty pony.  Stop. (1)");
		    System.exit(-1);
		}
	    }
	    else if (d == '\n')
	    {
		upper.add(new ArrayList<Color>());
		lower.add(new ArrayList<Color>());
	    }
	    else if (d == '\033')
	    {
		d = System.in.read();
		if (d == '[')
		{
		    d = System.in.read();
		    if (d == 'm')
		    {
			back = new Color(0, 0, 0, 0);
			fore = new Color(255, 255, 255, 255);
		    }
		    
		    int lastlast = 0;
		    int last = 0;
		    int item = 0;
		    for (;;)
		    {
			if ((d == ';') || (d == 'm'))
			{
			    item = -item;
			    
			    if (item == 0)
			    {
				back = new Color(0, 0, 0, 0);
				fore = new Color(255, 255, 255, 255);
			    }
			    else if (item == 39)  fore = new Color(255, 255, 255, 255);
			    else if (item == 49)  back = new Color(0, 0, 0, 0);
			    else if (item == 47)  back = null;
			    else if ((last == 5) && (lastlast == 38))
			    {
				Colour colour = new Colour(item);
				fore = new Color(colour.red, colour.green, colour.blue, 255);
			    }
			    else if ((last == 5) && (lastlast == 48))
			    {
				Colour colour = new Colour(item);
				back = new Color(colour.red, colour.green, colour.blue, 255);
			    }
			    else if ((item != 5) || ((last != 38) && (last != 48)))
				if ((item != 38) && (item != 48))
				{
				    System.err.println("Not a pretty pony.  Stop. (2)");
				    System.exit(-1);
				}
			    
			    lastlast = last;
			    last = item;
			    item = 0;
			    if (d == 'm')
				break;
			}
			else
			    item = (item * 10) - (d & 15);
			d = System.in.read();
		    }
		}
		else if ((d == ']') && (System.in.read() == 'P') && (System.in.read() == '7'))
		{
		    int r0 = ((d = System.in.read()) & 15) + ((d & 64) >> 3) + ((d & 64) >> 6);
		    int r1 = ((d = System.in.read()) & 15) + ((d & 64) >> 3) + ((d & 64) >> 6);
		    int g0 = ((d = System.in.read()) & 15) + ((d & 64) >> 3) + ((d & 64) >> 6);
		    int g1 = ((d = System.in.read()) & 15) + ((d & 64) >> 3) + ((d & 64) >> 6);
		    int b0 = ((d = System.in.read()) & 15) + ((d & 64) >> 3) + ((d & 64) >> 6);
		    int b1 = ((d = System.in.read()) & 15) + ((d & 64) >> 3) + ((d & 64) >> 6);
		    
		    r0 <<= 8;
		    g0 <<= 8;
		    b0 <<= 8;
		    
		    osip = new Color(r0 | r1, g0 | g1, b0 | b1, 255);
		}
		else
		{
		    System.err.println("Not a pretty pony.  Stop. (3)");
		    System.exit(-1);
		}
	    }
	    else if (d == 0xE2)
	    {
		if (System.in.read() != 0x96)
		{
		    System.err.println("Not a pretty pony.  Stop. (4)");
		    System.exit(-1);
		}
		
		d = System.in.read();
		if (d == 0x80) // ▀
	        {
		    upper.get(upper.size() - 1).add(fore);
		    lower.get(lower.size() - 1).add(back == null ? osip : back);
		}
		else if (d == 0x84) // ▄
		{
		    upper.get(upper.size() - 1).add(back == null ? osip : back);
		    lower.get(lower.size() - 1).add(fore);
		}
		else
		{
		    System.err.println("Not a pretty pony.  Stop. (5)");
		    System.exit(-1);
		}
	    }
	    else if (d == ' ')
	    {
		upper.get(upper.size() - 1).add(back == null ? osip : back);
		lower.get(lower.size() - 1).add(back == null ? osip : back);
	    }
	    else
	    {
		System.err.println("Not a pretty pony.  Stop. (6: " + d + ")");
		System.exit(-1);
	    }
	}
	
	if (lower.get(lower.size() - 1).size() == 0)
	{
	    upper.remove(upper.size() - 1);
	    lower.remove(lower.size() - 1);
	}
	
	int height = upper.size();
	int width = 0;
	for (final ArrayList<Color> line : upper)
	    if (width < line.size())
		width = line.size();
	
	BufferedImage img = new BufferedImage(width * ps, (height << 1) * ps, BufferedImage.TYPE_INT_ARGB);
	
	for (int y = 0; y < height; y++)
	{
	    ArrayList<Color> upperY = upper.get(y);
	    ArrayList<Color> lowerY = lower.get(y);
	    for (int x = 0; x < width; x++)
		for (int yy = 0; yy < ps; yy++)
		    for (int xx = 0; xx < ps; xx++)
		    {
			if (x < upperY.size())  img.setRGB(x * ps + xx, ((y << 1) | 0) * ps + yy, upperY.get(x).getRGB());
			if (x < lowerY.size())  img.setRGB(x * ps + xx, ((y << 1) | 1) * ps + yy, lowerY.get(x).getRGB());
		    }
	}
	
	assert crop == false : "Crop [-c] is not implemented.";
	
	ImageIO.write(img, "png", new File(file));
    }
    
}
