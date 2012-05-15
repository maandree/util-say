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
	    if      (args[ai].equals("-e"))  allowEsc = true;
	    else if (args[ai].equals("-p"))  paletteArg = args[++ai];
	    else if (args[ai].equals("-c"))  chromaArg = args[++ai];
	
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
	else
	{
	    paletteArg = new String();
	    for (int i = 0; i < 16; i++)
		paletteArg += "\033]P" + "0123456789ABCDEF".charAt(i) + palette[i];
	}
	if (allowEsc)
	    paletteArg = paletteArg.replace("\033", "\\e");
	
	if      (chromaArg == null)       chromaArg = "1";
	else if (chromaArg.equals("no"))  chromaArg = null;
	
	double chroma = 1.;
	if (chromaArg != null)
	    chroma = Double.parseDouble(chromaArg);
	
	final double[][] backpal = new double[8][];
	final double[][] forepal = new double[8][];
	final double[][] cpal = new double[16][];
	for (int i = 0; i < 16; i++)
	{
	    cpal[i] = pal(palette[i], chromaArg != null, chroma);
	    if (i < 8)
		backpal[i] = cpal[i];
	    else
		forepal[i & 7] = cpal[i];
	}
	
	int back = 0;
        int fore = 7;
	boolean bold = false;
	
	String esc = null;
	final char[] pcs = new char[6];
	for (int d; (d = System.in.read()) != -1;)
	    if (esc != null)
	    {
		String e = esc + (char)d;
		esc = null;
		
		if (d == ']')
		{
		    if ((d = System.in.read()) != 'P')
		    {
			System.out.print(e);
			System.out.write(d);
			continue;
		    }
		    e += (char)d;
		    d = System.in.read();
		    
		    int P = ((d & 64) >> 5) * 10 + (d & 15);
		    boolean bright = (P & 8) == 8;
		    for (int i = 0; i < 6; i++)
			pcs[i] = (char)(System.in.read());
		    final String p = new String(pcs);
		    double[] pal = pal(p, chromaArg != null, chroma);
		    
		    P = nearest(pal, bright ? forepal : backpal);
		    e += Integer.toString((bright ? 8 : 0) | P, 16).toUpperCase();
		    e += p;
		    
		    System.out.print(e);
		    e = e.substring(0, e.indexOf(']'));
		    
		    if (bright)
		    {
			if (P != fore)
			    System.out.print(e + "[3" + P + "m");
			if (bold == false)
			{
			    System.out.print(e + "[1m");
			    bold = true;
			}
		    }
		    else
			if (P != back)
			    System.out.print(e + "[4" + P + "m");
		}
		else if (d == '[')
		{
		    e += (char)(d = System.in.read());
		    if (d == 'm')
		    {
			back = 0;
			fore = 7;
			bold = false;
			System.out.print(paletteArg);
			System.out.print(e);
		    }
		    else
			for (;;)
			{
			    e += (char)(d = System.in.read());
			    if (false == ((('0' <= d) && (d <= '9')) || (d == ';')))
			    {
				if (d == 'm')
				{
				    final String pre = e.substring(0, e.indexOf('['));
				    e = e.substring(e.indexOf('[') + 1);
				    e = e.substring(0, e.length() - 1);
				    final String[] cds = e.split(";");
				    
				    for (final String cd : cds)
					if (cd.equals("1"))        bold = true;
					else if (cd.equals("21"))  bold = false;
					else if (cd.equals("39"))  fore = 7;
					else if (cd.equals("49"))
				        {
					    back = 0;
					    System.out.print(pre + "]P0" + palette[0]);
					    System.out.print(pre + "[" + e + "m");
					}
					else if (cd.startsWith("3"))
					{
					    bold = true;
					    System.out.print(pre + "[3" + fore + (bold ? "m" : ";1m"));
					    System.out.print(pre + "[" + e + "m");
					}
					else if (cd.startsWith("4"))
					    System.out.print(pre + "[4" + back + "m");
					else if (cd.equals("0"))
					{
					    back = 0;
					    fore = 7;
					    bold = false;
					    System.out.print(paletteArg);
					    System.out.print(pre + "[" + e + "m");
					}
				}
				else
				    System.out.print(e);
				break;
			    }
			}
		}
		else
		    System.out.print(e);
	    }
	    else if (allowEsc && (d == '\\'))
		if ((d = System.in.read()) == 'e')
		    esc = "\\e";
		else
		{
		    System.out.write('\\');
		    System.out.write(d);
		}
	    else if (d == '\033')
		esc = "\033";
	    else
		System.out.write(d);
	
	System.out.flush();
    }
    
    
    private static int nearest(final double[] pal, final double[][] pals)
    {
	double d = -100.;
        int best = 0;
        
	double L = pal[0];
	double a = pal[1];
	double b = pal[2];
	
        for (int i = 0, n = pals.length; i < n; i++)
	{
	    final double[] tLab = pals[i];
	    double ðL = L - tLab[0];
	    double ða = a - tLab[1];
	    double ðb = b - tLab[2];
            
	    double ð = ðL*ðL + ða*ða + ðb*ðb;
            
	    if ((d > ð) || (d < -50.))
	    {
		d = ð;
		best = i;
	    }
	}
	
	return best;
    }
    
    private static double[] pal(final String pal, final boolean chroma, final double weight)
    {
	char c;
	int rh = (((c = pal.charAt(0)) & 64) >> 5) * 10 + (c & 15);
	int rl = (((c = pal.charAt(1)) & 64) >> 5) * 10 + (c & 15);
	int gh = (((c = pal.charAt(2)) & 64) >> 5) * 10 + (c & 15);
	int gl = (((c = pal.charAt(3)) & 64) >> 5) * 10 + (c & 15);
	int bh = (((c = pal.charAt(4)) & 64) >> 5) * 10 + (c & 15);
	int bl = (((c = pal.charAt(5)) & 64) >> 5) * 10 + (c & 15);
	int r = (rh << 4) | rl;
	int g = (gh << 4) | gl;
	int b = (bh << 4) | bl;
	
	if (chroma)
	    return Colour.toLab(r, g, b, weight);
	return new double[] { (double)r, (double)g, (double)b };
    }

}
