/**
 * unzebra — Remove zebra characteristics from the user of block elements
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
 * The main class of the unzebra program
 *
 * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
 */
public class unzebra
{
    /**
     * Non-constructor
     */
    private unzebra()
    {
	assert false : "This class [unzebra] is not meant to be instansiated.";
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
	    System.out.println("Remove zebra characteristics from the user of block elements");
	    System.out.println();
	    System.out.println("USAGE:  unzebra [-e] < SOURCE > TARGET]");
	    System.out.println();
	    System.out.println("Source (STDIN):  Zebra pony");
	    System.out.println("Target (STDOUT): Non-zebra pony");
	    System.out.println();
	    System.out.println("-e  Allow escaped input and output. Use if you are fixing ponysay ponies.");
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
	
	boolean allowEsc = false;
	
	for (int ai = 0, an = args.length; ai < an; ai++)
	    if (args[ai].equals("-e"))
		allowEsc = true;
	
	int back = 0;
        int fore = 7;
	
	String esc = null;
	for (int d; (d = System.in.read()) != -1;)
	    if (esc != null)
	    {
		System.out.print(esc);
		esc = null;
		System.out.write(d);
		if (d != '[')
		    continue;
		String seq = "";
		for (;;)
	        {
		    d = System.in.read();
		    System.out.write(d);
		    String last = "";
		    String llast = "";
		    if (d == 'm')
		    {
			for (final String p : seq.split(";"))
			{
			    if (p.equals("0"))
			    {
				back = 0;
				fore = 7;
			    }
			    else if (last.equals("5"))
			    {
				if (llast.equals("38"))  fore = Integer.parseInt(p);
				if (llast.equals("48"))  back = Integer.parseInt(p);
			    }
			    else if (p.equals("5") && (last.equals("38") || last.equals("48")))  /* do nothing */;
			    else if (p.equals("49"))  back = 0;
			    else if (p.equals("39"))  fore = 7;
			    else if (p.equals("48"))  /* do nothing */;
			    else if (p.equals("38"))  /* do nothing */;
			    else if (p.equals("1"))   fore = fore >= 0 ? ~fore : fore;
			    else if (p.equals("21"))  fore = fore >= 0 ? fore : ~fore;
			    else if ((p.length() == 2) && p.startsWith("3"))   fore = p.charAt(1) - '0';
			    else if ((p.length() == 2) && p.startsWith("9"))   fore = -(p.charAt(1) - '0');
			    else if ((p.length() == 2) && p.startsWith("4"))   back = p.charAt(1) - '0';
			    else if ((p.length() == 3) && p.startsWith("10"))  back = -(p.charAt(2) - '0');
			    else
			    {
				System.err.println("Not a pretty pony.  Stop.");
				System.exit(-1);
			    }
			    llast = last;
			    last = p;
			}
			break;
		    }
		    else if ((d == ';') || (('0' <= d) && (d <= '9')))
			seq += (char)d;
		    else
			break;
		}
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
	    else if (d == 226)
	    {
		d = System.in.read();
		if (d == 150)
		{
		    d = System.in.read();
		    if (((d == 128) || (d == 132)) && (back == fore))
			System.out.write(' ');
		    else
		    {
			System.out.write(226);
			System.out.write(150);
			System.out.write(d);
		    }
		}
		else
		{
		    System.out.write(226);
		    System.out.write(d);
		}
	    }
	    else
		System.out.write(d);
	
	System.out.flush();
    }

}
