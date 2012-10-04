/**
 * Wordwrap — Wraps text make words fit, unsplitted, sida an area
 *
 * Copyright © 2012  Mattias Andrée (maandree@kth.se)
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
import java.util.*;


/**
 * The main class of the Wordwrap program
 *
 * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
 */
public class Wordwrap
{
    /**
     * Non-constructor
     */
    private Wordwrap()
    {
	assert false : "This class [Wordwrap] is not meant to be instansiated.";
    }
    
    
    
    /**
     * This is the main entry point of the program
     * 
     * @param  args  Startup arguments, <code>[--help | TERMINAL_WIDTH [SANE_WIDTH]]</code>
     * 
     * @throws  IOException  On I/O exception
     */
    public static void main(final String... args) throws IOException
    {
	if ((args.length > 0) && args[0].equals("--help"))
	{
	    System.out.println("Wraps text make words fit, unsplitted, sida an area");
            System.out.println();
            System.out.println("USAGE:  Wordwrap WIDTH < RAW > WRAPPED");
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
            System.out.println("along with this program.  If not, see <http://www.gnu.org/licenses/>.");
            System.out.println();
            System.out.println();
	    return;
	}
	
	final int width = Integer.parseInt(args[0]);
	byte[] buf = new byte[width << 1];
	int ptr = 0, tabs = 0, d;
	
	for (;;)
	    if (((d = System.in.read()) == '\n') || (d == -1))
	    {
		int n = ptr;
		byte[] expanded = new byte[n + tabs << 3];
		ptr = 0;
		for (int i = 0, c = 0; i < n; i++, c++)
		    if (buf[i] == '\t')
		    {
			int w = 8 - (i & 7);
			c += w - 1;
			for (int j = 0; j < w; j++)
			    expanded[ptr++] = ' ';
		    }
		    else
			expanded[ptr++] = buf[i];
		wrap(expanded, ptr, width);
		if (d == -1)
		    break;
		System.out.write(d);
		ptr = tabs = 0;
	    }
	    else
	    {
		if (ptr == buf.length)
		    System.arraycopy(buf, 0, buf = new byte[ptr << 1], 0, ptr);
		buf[ptr++] = (byte)d;
		if (d == '\t')
		    tabs++;
	    }
    }
    
    
    public static void wrap(final byte[] buf, final int len, final int width)
    {
	final byte[] b = new byte[buf.length];
	final int[] map = new int[buf.length];
	int bi = 0, d, cols = 0, w = width;
	int indent = -1, indentc = 0;
	for (int i = 0; i <= len;)
	    if ((d = (i == len ? -1 : (buf[i++] & 255))) == 033)
	    {
		b[bi++] = (byte)d;
		d = (b[bi++] = buf[i++]) & 255;
		if (d == '[')
		    for (;;)
		    {
			d = (b[bi++] = buf[i++]) & 255;
			if ((('a' <= d) && (d <= 'z')) || (('A' <= d) && (d <= 'Z')) || (d == '~'))
			    break;
		    }
		else if (d == ']')
		{
		    d = (b[bi++] = buf[i++]) & 255;
		    if (d == 'P')
			for (int j = 0; j < 7; j++)
			    b[bi++] = buf[i++];
		}
	    }
	    else if ((d != -1) && (d != ' '))
	    {
		if (indent == -1)
	        {
		    indent = i - 1;
		    for (int j = 0; j < indent; j++)
			if (buf[j] == ' ')
			    indentc++;
		}
		b[bi++] = (byte)d;
		if ((d & 0xC0) != 0x80)
		    map[++cols] = bi;
	    }
	    else
	    {
		int m, mm = 0;
		while (((w > 8) && (cols > w + 3)) || (cols > width))
		{
		    System.out.write(b, 0, m = map[mm += w - 1]);
		    System.out.write('̣­');
		    System.out.write('\n');
		    cols -= w - 1;
		    System.arraycopy(b, m, b, 0, bi -= m);
		    w = width;
		    if (indent != -1)
		    {
			System.out.write(buf, 0, indent);
			w -= indentc;
		    }
		}
		if (cols > w)
		{
		    System.out.write('\n');
		    w = width;
		    if (indent != -1)
		    {
			System.out.write(buf, 0, indent);
			w -= indentc;
		    }
		}
		System.out.write(b, 0, bi);
		w -= cols;
		cols = bi = 0;
		if (d == -1)
		    i++;
		else
		    if (w > 0)
		    {
			System.out.write(' ');
			w--;
		    }
		    else
		    {
			System.out.write('\n');
			w = width;
			if (indent != -1)
			{
			    System.out.write(buf, 0, indent);
			    w -= indentc;
			}
		    }
	    }
    }
    
}
