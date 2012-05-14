/**
 * tty2colourfultty — 3-palette TTY pony to 16-palette TTY pony conversion tool
 *
 * Copyright © 2012  Mattias Andrée (maandree@kth.se)
 *
 * This prorgram is free software: you can redistribute it and/or modify
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

import java.util.*;
import java.io.*;


/**
 * The main class of the tty2colourfultty program
 *
 * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
 */
public class tty2colourfultty
{
    /**
     * Non-constructor
     */
    private tty2colourfultty()
    {
	assert false : "This class [tty2colourfultty] is not meant to be instansiated.";
    }
    
    
    
    /**
     * This is the main entry point of the program
     * 
     * @param  args  Startup arguments, start the program with </code>--help</code> for details
     * 
     * @throws  IOException  On I/O exception
     */
    public static void main(final String... args) throws IOException
    {
	if ((args.length > 0) && args[0].equals("--help"))
	{
	    System.out.println("3-palette TTY pony to 16-palette TTY pony conversion tool");
	    System.out.println();
	    System.out.println("USAGE:  tty2colourfultty [-e] [-p PALETTE] [-c CHROMA] < SOURCE > TARGET]");
	    System.out.println();
	    System.out.println("Source (STDIN):  3-palette TTY pony");
	    System.out.println("Target (STDOUT): 16-palettel TTY pony");
	    System.out.println();
	    System.out.println("-e  Allow escaped input and output. Use if you are converting ponysay ponies.");
	    System.out.println("-p  Palette, echo in your TTY palette setup string.");
	    System.out.println("-c  Colour match chromaticity weight. 1 is default, enter 'no' for sRGB distances.");
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
	
	final String palette[] = new String[16];
	for (int i = 0; i < 16; i++)
	    if (i == 3)
		palette[i] = "AA5500";
	    else if (i < 8)
	    {
		palette[i]  = (i & 1) == 0 ? "00" : "AA";
		palette[i] += (i & 2) == 0 ? "00" : "AA";
		palette[i] += (i & 4) == 0 ? "00" : "AA";
	    }
	    else
	    {
		palette[i]  = (i & 1) == 0 ? "55" : "FF";
		palette[i] += (i & 2) == 0 ? "55" : "FF";
		palette[i] += (i & 4) == 0 ? "55" : "FF";
	    }
	
	boolean allowEsc = false;
	String paletteArg = null;
	String chromaArg = null;
	
	for (int ai = 0, an = args.length; ai < an; ai++)
	    if (args[ai].equals("-e"))
		allowEsc = true;
	    else if (args[ai].equals("-p"))
		paletteArg = args[++ai];
	    else if (args[ai].equals("-c"))
		chromaArg = args[++ai];
	
	if (paletteArg != null)
	{
	    final char[] p = new char[6];
	    for (int i = 0, n = paletteArg.length(); i < n;)
	    {
		if (paletteArg.charAt(i++) != '\033')  continue;
		if (paletteArg.charAt(i++) != ']')     continue;
		if (paletteArg.charAt(i++) != 'P')     continue;
		
		final char c = paletteArg.charAt(i++);
		final int P = ((c & 64) >> 6) * 10 + (c & 15);
		
		p[0] = paletteArg.charAt(i++);
		p[1] = paletteArg.charAt(i++);
		p[2] = paletteArg.charAt(i++);
		p[3] = paletteArg.charAt(i++);
		p[4] = paletteArg.charAt(i++);
		p[5] = paletteArg.charAt(i++);
		
		palette[P & 15] = new String(p);
	    }
	}
	
	if (chromaArg.equals("no"))  chromaArg = null;
	else if (chromaArg == null)  chromaArg = "1";
	
	double chroma = 1.;
	if (chromaArg != null)
	    chroma = Double.parseDouble(chromaArg);
    }

}
