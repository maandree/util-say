/**
 * unisay2ttyunisay — TTY suitifying unisay pony tool
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
 * The main class of the unisay2ttyunisay program
 *
 * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
 */
public class unisay2ttyunisay
{
    /**
     * Non-constructor
     */
    private unisay2ttyunisay()
    {
	assert false : "This class [unisay2ttyunisay] is not meant to be instansiated.";
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
	    System.out.println("TTY suitifying unisay pony tool");
	    System.out.println();
	    System.out.println("USAGE:  unisay2ttyunisay < SOURCE > TARGET");
	    System.out.println();
	    System.out.println("Source (STDIN):  Regular unisay pony");
	    System.out.println("Target (STDOUT): New TTY unisay pony");
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
	
	out.println("\033c");
	
	boolean dollar = false;
	
	for (int d; (d = in.read()) != -1;)
	    if (d == '$')
	    {
		dollar ^= true;
		out.write(d);
	    }
	    else if (dollar)
		out.write(d);
	    else if (d == '\033')
	    {
                d = System.in.read();
                if (d == '[')
		{
		    d = System.in.read();
		    if (d == 'm')
			System.out.print("\033]P7aaaaaa\033]Pfffffff\033[0m");
		    
		    int lastlast = 0;
		    int last = 0;
		    int item = 0;
		    for (;;)
		    {
			if ((d == ';') || (d == 'm'))
			{
			    item = -item;
			    
			    if      (item == 0)   out.print("\033]P7aaaaaa\033]Pfffffff\033[0m");
			    else if (item == 39)  out.print("\033[39m");
			    else if (item == 49)  out.print("\033[49m");
			    else if ((last == 5) && (lastlast == 38))
			    {
				Colour colour = new Colour(item);
				out.print(getOSIPCode(colour.red, colour.green, colour.blue, false));
			    }
			    else if ((last == 5) && (lastlast == 48))
			    {
				Colour colour = new Colour(item);
				out.print(getOSIPCode(colour.red, colour.green, colour.blue, true));
			    }
			    else if ((item != 5) || ((last != 38) && (last != 48)))
				if ((item != 38) && (item != 48))
				{
				    System.err.println("Not a pretty pony.  Stop.");
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
                else
		{
		    System.err.println("Not a pretty pony.  Stop.");
		    System.exit(-1);
		}
	    }
	    else
		out.write(d);
    }
    
    
    private static final String HEX = "0123456789abcdef";
    private static String getOSIPCode(final int r, final int g, final int b, final boolean background)
    {
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
