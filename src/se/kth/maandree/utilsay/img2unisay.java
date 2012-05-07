/**
 * img2unisay — Image to unisay convertion tool
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
 * The main class of the img2unisay program
 *
 * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
 */
public class img2unisay
{
    /**
     * Non-constructor
     */
    private img2unisay()
    {
	assert false : "This class [img2unisay] is not meant to be instansiated.";
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
	    System.out.println("Image to unisay convertion tool");
	    System.out.println();
	    System.out.println("USAGE:  img2unisay [-2] [--] SOURCE > TARGET");
	    System.out.println();
	    System.out.println("Source:          Image file");
	    System.out.println("Target (STDOUT): File name for new unisay pony");
	    System.out.println();
	    System.out.println("-2  Input image have double dimensioned pixels.");
	    System.out.println("-p  Use OSI P colouring for Linux VT");
	    System.out.println();
	    System.out.println("Known supported input formats:");
	    System.out.println("  ⋅  PNG  (non-animated)");
	    System.out.println("  ⋅  GIF  (first frame)");
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
	
	boolean useP = false;
	int ai = 0;
	int ps = 1;
	
	for (;; ai++)
	    if      (args[ai].equals("-2"))  ps = 2;
	    else if (args[ai].equals("-p"))  useP = true;
	    else
		break;
	
	if (args[ai].equals("--"))
	    ai++;
	
	String file = args[ai++];
	
	final PrintStream out = System.out;
	final BufferedImage img = ImageIO.read(new File(file));
	
	int w = img.getWidth() / ps;
	int h = img.getHeight() / ps;
	
	int maxx = 0;
	int minx = w;
	
	int[][] pony = new int[h + 1][w];
	final int[] emptyset = new int[w];
	for (int x = 0; x < w; x++)
	    emptyset[x] = -1;

	for (int y = 0; y < h; y++)
	{
	    boolean empty = true;
	    for (int x = 0; x < w; x++)
	    {
		final int argb = img.getRGB(x * ps, y * ps);
		int a = (argb >> 24) & 0xFF;
		int r = (argb >> 16) & 0xFF;
		int g = (argb >>  8) & 0xFF;
		int b =  argb        & 0xFF;
		
		if ((0 < a) && (a < 255))
		{
		    r = r * a / 255 + 255 - a;
		    g = g * a / 255 + 255 - a;
		    b = b * a / 255 + 255 - a;
		}
		
		if (a != 0)
		{
		    pony[y][x] = useP ? ((r << 16) | (g << 8) | b) : (new Colour((byte)r, (byte)g, (byte)b)).index;
		    empty = false;
		    if (maxx < x)  maxx = x;
		    if (minx > x)  minx = x;
		}
		else
		    pony[y][x] = -1;
	    }
	    if (empty)
		pony[y] = null;
	}
	
	int yoff = 0;
	while (pony[yoff] == null)
	    yoff++;
	
	for (int y = yoff; y < h; y++)
	    pony[y - yoff] = pony[y];
	
	h -= yoff;
	
	while (pony[h - 1] == null)
	    h--;
	
	for (int y = 0; y < h; y++)
	    if (pony[y] == null)
		pony[y] = emptyset;
	
	pony[h] = emptyset;
	
	int fore = -1;
	int back = -1;
	
	minx = (minx -= 1) < 0 ? 0 : minx;
	
	int bw = 0;
	String offl = new String();
	for (int x = minx; x <= maxx; x++)
	{
	    if (pony[0][x] >= 0)
		break;
	    if (x - minx > 3)
	    {
		offl += ' ';
		bw++;
	    }
	}
	
	if (useP)
	    System.out.println("\033c");
	System.out.println("$baloon" + (bw + 3) +  "$\033[0m");
	System.out.println(offl + "$\\$");
	System.out.println(offl + " $\\$");
	System.out.println(offl + "  $\\$");
	
	for (int y = 0; y < h; y += 2)
	{
	    for (int x = minx; x <= maxx; x++)
	    {
		final int upper = pony[y][x];
		final int lower = pony[y + 1][x];
		
		if ((upper < 0) && (lower < 0))
		{
		    if (fore >= 0)  System.out.print("\033[39m");
		    if (back >= 0)  System.out.print("\033[49m");
		    fore = back = -1;
		    System.out.print(' ');
		}
		else if (upper < 0)
		{
		    if (back >= 0)  System.out.print("\033[49m");
		    back = -1;
		    if (fore != lower)
			if (useP)
			    System.out.print(getOSIPCode(fore = lower, false));
			else
			    System.out.print("\033[38;5;" + (fore = lower) + "m");
		    System.out.print('▄');
		}
		else if (lower < 0)
		{
		    if (back >= 0)  System.out.print("\033[49m");
		    back = -1;
		    if (fore != upper)
			if (useP)
			    System.out.print(getOSIPCode(fore = upper, false));
			else
			    System.out.print("\033[38;5;" + (fore = upper) + "m");
		    System.out.print('▀');
		}
		else if ((back == lower) || (fore == upper))
		{
		    if (fore != upper)
			if (useP)
			    System.out.print(getOSIPCode(fore = upper, false));
			else
			    System.out.print("\033[38;5;" + (fore = upper) + "m");
		    
		    if (back != lower)
			if (useP)
			    System.out.print(getOSIPCode(back = lower, true));
			else
			    System.out.print("\033[48;5;" + (back = lower) + "m");
		    
		    System.out.print('▀');
		}
		else
		{
		    if (back != upper)
			if (useP)
			    System.out.print(getOSIPCode(back = upper, true));
			else
			    System.out.print("\033[48;5;" + (back = upper) + "m");
			
		    if (fore != lower)
			if (useP)
			    System.out.print(getOSIPCode(fore = lower, false));
			else
			    System.out.print("\033[38;5;" + (fore = lower) + "m");
			
		    System.out.print('▄');
		}
	    }
	    fore = back = -1;
	    if (useP)
	    {
		System.out.print("\033]P7aaaaaa");
		System.out.print("\033]Pfffffff");
	    }
	    System.out.println("\033[0m");
	}
    }
    

    private static final String HEX = "0123456789abcdef";    
    private static String getOSIPCode(final int colour, final boolean background)
    {
	int r = colour >> 16;
	int g = colour >> 8;
	int b = colour;
			    
	String code = new String();
	code += HEX.charAt((r >> 4) & 15);
	code += HEX.charAt( r       & 15);
	code += HEX.charAt((g >> 4) & 15);
	code += HEX.charAt( g       & 15);
	code += HEX.charAt((b >> 4) & 15);
	code += HEX.charAt( b       & 15);
	
	return "\033]P" + (background ? '7' : 'f') + code + "\033[" + (background ? "4" : "1;3") + "7m";
    }
    
}
