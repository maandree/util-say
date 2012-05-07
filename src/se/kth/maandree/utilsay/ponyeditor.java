/**
 * ponyeditor — pony editing tool for unisay
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

import java.util.*;
import java.io.*;


/**
 * The main class of the ponyeditor program
 *
 * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
 */
public class ponyeditor
{
    public static final int TRANSPARENT = (1 << 31) | 8;
    public static final int LINK_O      = 1 << 31;
    public static final int LINK_Y      = LINK_O | 1;
    public static final int LINK_FO     = LINK_O | 2;
    public static final int LINK_FY     = LINK_Y | 2;
    
    
    
    /**
     * Non-constructor
     */
    private ponyeditor()
    {
	assert false : "This class [ponyeditor] is not meant to be instansiated.";
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
	if (args.length != 2)
	{
	    System.out.println("Pony editing tool");
	    System.out.println();
	    System.out.println("USAGE:  ponyeditor SOURCE TARGET");
	    System.out.println();
	    System.out.println("Source: Original pony");
	    System.out.println("Target: Where the new pony should be stored");
	    System.out.println();
	    System.out.println("KEYBOARD COMMANDS:");
	    System.out.println();
	    System.out.println("        \\    Override with \\ directional baloon link");
	    System.out.println( "        /    Override with / directional baloon link");
	    System.out.println( "    space    Override with colour");
	    System.out.println( "    enter    Insert new row");
	    System.out.println( "   delete    Delete row");
	    System.out.println( "       ^H    Delete cell");
	    System.out.println( "        +    Insert new cell");
	    System.out.println( "        b    Insert baloon");
	    System.out.println( "        e    Remove one row from the baloon");
	    System.out.println( "        d    Add one row to the baloon");
	    System.out.println( "        a    Move left edge to one step to the left");
	    System.out.println( "        s    Move left edge to one step to the right");
	    System.out.println( "        f    Move right edge to one step to the left");
	    System.out.println( "        g    Move right edge to one step to the right");
	    System.out.println( "        o    Save the pony");
	    System.out.println( "        q    Save the pony and quit");
	    System.out.println( "        p    Preview pony (and exit preview)");
	    System.out.println( "     left    Move the cursor one step to the left");
	    System.out.println( "    right    Move the cursor one step to the right");
	    System.out.println( "       up    Move the cursor one step up");
	    System.out.println( "     down    Move the cursor one step down");
	    System.out.println( "   S-left    Move the view one step to the left");
	    System.out.println( "  S-right    Move the view one step to the right");
	    System.out.println( "     S-up    Move the view one step up");
	    System.out.println( "   S-down    Move the view one step down");
	    System.out.println( "        0    Reset view");
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
	
	final InputStream  in  = new BufferedInputStream (new FileInputStream (new File(args[0])));
	final OutputStream out = new BufferedOutputStream(new FileOutputStream(new File(args[1])));
	
	int[][] img = readPony(in);
	if (img == null)
	{
	    System.err.println("\033[31mponyeditor: Not a pony.  Stop.\033[0m");
	    System.exit(1);
	    return;
	}
    }
    
    
    
    public static int[][] readPony(final InputStream in) throws IOException
    {
	final ArrayList<ArrayList<int[]>> img = new ArrayList<ArrayList<int[]>>();
	ArrayList<int[]> cur = new ArrayList<int[]>();
	img.add(cur);
	
	//upper = ▀ = 2580₁₆ = 0010'010110'000000₂ =
	//          = UTF-8(11100010₂, 10010110₂, 10000000₂) =
	//          = 226₁₀, 150₁₀, 128₁₀
	
	//lower = ▄ = 2584₁₆ = 0010'010110'000100₂ =
	//          = UTF-8(11100010₂, 10010110₂, 10000100₂) =
	//          = 226₁₀, 150₁₀, 132₁₀
	
	int fore = 0;
	int back = TRANSPARENT;
	int baloon = 0;
	
	for (int d; (d = in.read()) != -1;)
	    switch (d)
	    {
		case '$':
		    String dollar = new String();
		    for (int dd; (dd = in.read()) != '$';)
		    {
			if (dd == -1)
			    return null;
		        int c = dd;
			int cn = 0;
			while ((c & 128) != 0)
			{
			    cn++;
			    c <<= 1;
			}
			c = (c & 255) >> cn;
			for (int i = 1; i < cn; i++)
			{
			    dd = in.read();
			    dd &= 127;
			    c = (c << 6) | dd;
			}
			if (c <= 0xFFFF)
			    dollar += (char)c;
			else
			{
			    if (c > 0x10FFFF)
				return null; //can't be encoded in UTF-16, no character exists, yet, beyond 0x10FFFF
			    int hi = (c >> 10) & 1023;
			    int lo = c & 1023;
			    hi |= 0xD800;
			    lo |= 0xDC00;
			    dollar += (char)hi;
			    dollar += (char)lo;
			}
		    }
		    
		    if (dollar.equals("\\"))
			cur.add(new int[] { LINK_O, LINK_Y });
		    else if (dollar.equals("/"))
			cur.add(new int[] { LINK_FO, LINK_FY });
		    else if (dollar.startsWith("baloon"))
			try
			{
			    final int w = dollar.length() == 6 ? 1 : Integer.parseInt(dollar.substring(6));
			    if (w < 1)     return null;
			    if (w > 1023)  return null; //sanity
			    
			    baloon--;
			    cur.add(new int[] { baloon, baloon });
			}
			catch (final Throwable err)
			{
			    return null; //support issue
			}
		    else
			 return null; //support issue and unlikly to be used
			
		    break;
		case '\n':
		    img.add(cur = new ArrayList<int[]>());
		    break;
		case '\033':
		    if (in.read() != '[')
			return null;
		    d = in.read();
		    if ((d == '0') || (d == 'm'))
		    {
			fore = 0;
			back = TRANSPARENT;
			if ((d == '0') && (in.read() != 'm'))
			    return null;
		    }
		    else if ((d == '3') || (d == '4'))
		    {
			final boolean edfore = d == '3';
			d = in.read();
			if (d == '9')
			{
			    if (edfore)  fore = 0;
			    else         back = TRANSPARENT;
			    if (in.read() != 'm')
				return null;
			}
			else if (d == '8')
			{
			    if (in.read() != ';')  return null;
			    if (in.read() != '5')  return null;
			    if (in.read() != ';')  return null;
			    
			    int c = 0;
			    for (int dd; (dd = in.read()) != 'm';)
				if (('0' <= dd) && (dd <= '9'))
				    c = (c * 10) - (dd & 15);
				else
				    return null;
			    c = -c;
			    if (edfore)  fore = c;
			    else         back = c;
			    
			    if (c > 255)
				return null;
			}
			else
			    return null;
		    }
		    break;
		case ' ':
		    cur.add(new int[] { back, back });
		    break;
		case 226:
		    if (in.read() == 150)
			if ((d = in.read()) == 128)
			    cur.add(new int[] { fore, back });
			else if (d == 132)
			    cur.add(new int[] { back, fore });
			else
			    return null;
		    else
			return null;
		    break;
		default:
		    return null;
	    }
	
	int w = 0;
	for (final ArrayList<int[]> row : img)
	    if (w > row.size())
		w = row.size();
	
	for (final ArrayList<int[]> row : img)
	    for (int i = row.size(); i < w; i++)
		row.add(new int[] { TRANSPARENT, TRANSPARENT });
	
	final int[][] rc = new int[img.size() << 1][w];
	for (int y = 0, h = img.size(); y < h; y++)
	    for (int x = 0; x < w; x++)
	    {
		rc[(y << 1) | 0][x] = img.get(y).get(x)[0];
		rc[(y << 1) | 1][x] = img.get(y).get(x)[1];
	    }
	
	return rc;
    }
    
}


