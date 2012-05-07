/**
 * unisay2ponysay — unisay to ponysay pony convertion tool
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
 * The main class of the unisay2ponysay program
 *
 * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
 */
public class unisay2ponysay
{
    /**
     * Non-constructor
     */
    private unisay2ponysay()
    {
	assert false : "This class [unisay2ponysay] is not meant to be instansiated.";
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
	if (args.length != 0)
	{
	    System.out.println("Pony cannibalisation tool");
	    System.out.println();
	    System.out.println("USAGE:  unisay2ponysay < SOURCE > TARGET");
	    System.out.println();
	    System.out.println("Source (STDIN):  Pony from unisay");
	    System.out.println("Target (STDOUT): File name for new ponysay pony");
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
	
	final InputStream in = System.in;
	final PrintStream out = System.out;
	
	final HashMap<String, String> vars = new HashMap<String, String>();
	vars.put("", "$");
	
	out.println("$the_cow =<<EOC;");
	String buf = null;
	boolean skipln = false;
	boolean esc = false;
	
	int d;
        while ((d = in.read()) != -1)
	{
	    if (esc)
	    {
		esc = false;
		if (d == 'c')
		{
		    skipln = true;
		    continue;
		}
		out.print("\\e");
	    }
	    
	    if ((d & 0xC0) == 0x80) continue;
	    if ((d & 0x80) == 0x80)
	    {
		int dn = 0;
		while ((d & 0x80) == 0x80)
	        {
		    d <<= 1;
		    dn++;
		}
		d &= 0xFF;
		d >>>= dn;
		for (int i = 1; i < dn; i++)
		{
		    int dd = in.read();
		    if ((dd & 0xC0) != 0x80)
			break;
		    d <<= 6;
		    d |= dd & 0x3F;
		}
	    }
	    
	    if (buf != null)
		if (d == '$')
		{
		    if      (buf.equals("\\") || buf.equals("/"))  out.print("$thoughts");
		    else if (buf.startsWith("baloon"))             skipln = true;
		    else if (buf.contains("="))                    vars.put(buf.substring(0, buf.indexOf("=")), buf.substring(buf.indexOf("=") + 1));
		    else                                           out.print(vars.get(buf));
		    buf = null;
		}
		else if (d > 0xFFFF)
		{
		    int hi = d - 0x10000;
		    int lo = d - 0x10000;
		    hi >>= 10;
		    hi &= 1023;
		    lo &= 1023;
		    
		    buf += (char)(hi | 0xD800);
		    buf += (char)(lo | 0xDC00);
		}
		else
		    buf += (char)d;
	    else if ((d == '\n') && skipln)  skipln = false;
	    else if (d == '$')               buf = new String();
	    else if (d == '\033')            esc = true;
	    else if (d < 128)                out.write(d);
	    else
	    {
		String u = Integer.toString(d, 16).toUpperCase();;
		while (u.length() < 4)
		    u = "0" + u;
		out.print("\\N{U+" + u + "}");
	    }
	}
	
	if (d != '\n')
	    out.println();
	out.println("EOC");
    }
}
