/**
 * ponysay2unisay — ponysay to unisay pony convertion tool
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
 * The main class of the ponysay2unisay program
 *
 * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
 */
public class ponysay2unisay
{
    /**
     * Non-constructor
     */
    private ponysay2unisay()
    {
	assert false : "This class [ponysay2unisay] is not meant to be instansiated.";
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
	if (args.length != 0)
	{
	    System.out.println("Pony cannibalisation tool");
	    System.out.println();
	    System.out.println("USAGE:  ponysay2unisay < SOURCE > TARGET");
	    System.out.println();
	    System.out.println("Source (STDIN):  Pony from ponysay");
	    System.out.println("Target (STDOUT): File name for new unisay pony");
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
	
	byte[] end = null;
	
	for (;;)
	{
	    for (;;)
		if (in.read() == '$')
		    break;
	    
	    if (in.read() != 't') continue;
	    if (in.read() != 'h') continue;
	    if (in.read() != 'e') continue;
	    if (in.read() != '_') continue;
	    if (in.read() != 'c') continue;
	    if (in.read() != 'o') continue;
	    if (in.read() != 'w') continue;
	    
	    for (;;)
		if (in.read() == '<')
		    break;
	    
	    in.read(); // <
	    
	    final byte[] eoc = new byte[256];
	    int ptr = 0;
	    
	    for (int d;;)
		if ((d = in.read()) != ';')
		    eoc[ptr++] = (byte)d;
		else
		    break;
	    
	    end = new byte[ptr];
	    System.arraycopy(eoc, 0, end, 0, ptr);
	    
	    for (;;)
		if (in.read() == '\n')
		    break;
	    
	    break;
	}
	
	out.println("$baloon$");
	
	final int[] link = {'$', 't', 'h', 'o', 'u', 'g', 'h', 't', 's'};
	
	outer:
	    for (int d; (d = read()) != -1;)
	    {
		if (d == '\\')
		{
		    final String HEX = "0123456789ABCDEF";
		    final String hex = "0123456789abcdef";
		    
		    int u = 0, v;
		    char c;
		    d = read();
		    switch (d)
		    {
			case 'a':  out.write(7);   break;
			case 'b':  out.write(8);   break;
			case 'e':  out.write(27);  break;
			case 'f':  out.write(12);  break;
			case 'n':  out.write(10);  break;
			case 'r':  out.write(13);  break;
			case 't':  out.write(9);   break;
			case 'v':  out.write(11);  break;
			case 'u':
			    for (int i = 0; i < 4; i++)
			    {
				c = (char)read();
				v = (v = HEX.indexOf(c)) < 0 ? hex.indexOf(c) : v;
				u = (u << 4) | v;
			    }
			    out.write(toBytes(u));
			    break;
			case 'N':
			    for (;;)
				if (read() == '+')
				    break;
			    for (;;)
			    {
				c = (char)read();
				if (c == '}')
				    break;
				v = (v = HEX.indexOf(c)) < 0 ? hex.indexOf(c) : v;
				u = (u << 4) | v;
			    }
			    out.write(toBytes(u));
			    break;
			default:
			    out.write(d);
			    break;
		    }
		}
		else if (d == link[0])
		{
		    final int[] buf = new int[link.length];
		    int ptr = 0;
		    buf[ptr++] = d;
		    for (; (d = read()) != -1;)
		    {
			buf[ptr++] = d;
			if (ptr == 2)
			    if ((buf[0] == '$') && (buf[1] == '$'))
			    {
				out.print("$$");
				continue outer;
			    }
			if (ptr == buf.length)
			    break;
		    }
		    if (ptr < link.length)
		    {
			out.write(castBytes(buf));
			break outer;
		    }
		    int found = 0;
		    for (int i = 0, n = buf.length; i < n; i++)
		    {
			if (buf[i] == link[0])
			    found = i;
			if (buf[i] != link[i])
			{
			    if (found == 0)
				found = i;
			    out.write(castBytes(buf), 0, found);
			    final int[] p = new int[buf.length - found];
			    System.arraycopy(buf, found, p, 0, buf.length - found);
			    if (prebuf != null)
				if (preptr < prebuf.length)
				{
				    final int[] q = new int[prebuf.length - preptr];
				    System.arraycopy(prebuf, preptr, q, 0, q.length);
				    prebuf = null;
				    pre.add(0, q);
				}
			    pre.add(0, p);
			    continue outer;
			}
		    }
		    out.print("$\\$");
		}
		else if (d == end[0])
		{
		    final int[] buf = new int[end.length];
		    int ptr = 0;
		    buf[ptr++] = d;
		    for (; (d = read()) != -1;)
		    {
			buf[ptr++] = d;
			if (ptr == buf.length)
			    break;
		    }
		    if (ptr < end.length)
		    {
			out.write(castBytes(buf));
			break outer;
		    }
		    int found = 0;
		    for (int i = 0, n = buf.length; i < n; i++)
		    {
			if (buf[i] == end[0])
			    found = i;
			if (buf[i] != end[i])
			{
			    if (found == 0)
				found = i;
			    out.write(castBytes(buf), 0, found);
			    final int[] p = new int[buf.length - found];
			    System.arraycopy(buf, found, p, 0, buf.length - found);
			    if (prebuf != null)
				if (preptr < prebuf.length)
				{
				    final int[] q = new int[prebuf.length - preptr];
				    System.arraycopy(prebuf, preptr, q, 0, q.length);
				    prebuf = null;
				    pre.add(0, q);
				}
			    pre.add(0, p);
			    continue outer;
			}
		    }
		    break outer;
		}
		else
		    out.write(d);
	    }
    }
    
    private static int read() throws IOException
    {
	if (prebuf != null)
	    if (preptr == prebuf.length)
		prebuf = null;
	if (prebuf == null)
	    if (pre.isEmpty() == false)
	    {
		prebuf = pre.get(0);
		pre.remove(0);
		preptr = 0;
	    }
	if (prebuf == null)
	    return System.in.read();
	return prebuf[preptr++];
    }
    
    private static byte[] castBytes(final int[] ints)
    {
	final byte[] rc = new byte[ints.length];
	for (int i = 0, n = rc.length; i < n; i++)
	    rc[i] = (byte)(ints[i]);
	return rc;
    }
    
    private static byte[] toBytes(final int character)
    {
        //7:  0xxxyyyy
        //11: 110xyyyy 10xxyyyy
        //16: 1110xxxx 10xxyyyy 10xxyyyy
        //21: 11110xxx 10xxyyyy 10xxyyyy 10xxyyyy
        //26: 111110xx 10xxyyyy 10xxyyyy 10xxyyyy 10xxyyyy
        //31: 1111110x 10xxyyyy 10xxyyyy 10xxyyyy 10xxyyyy 10xxyyyy
	
	final byte[] rc;
	final int c = character;
	
	if (c < 0x80)
	    rc = new byte[] { (byte)c };
	else
	{
	    final byte[] buf = new byte[6];
	    int i = 0;
	    int b = c;
	    while (b >= 0x40)
	    {
		buf[i++] = (byte)((b & 0x3F) | 0x80);
		b >>>= 6;
	    }
	    buf[i++] = (byte)b;
	    rc = new byte[i];
	    for (int j = 0; j < i; j++)
		rc[j] = buf[i - j - 1];
	    rc[0] |= (byte)((0xFF << (8 - i)) & 0xFF);
	}
	
        return rc;
    }
    
}
