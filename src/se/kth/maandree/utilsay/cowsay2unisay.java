/**
 * cowsay2unisay — cowsay to unisay cow conversion tool
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

import java.util.*;
import java.io.*;


/**
 * The main class of the cowsay2unisay program
 *
 * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
 */
public class cowsay2unisay
{
    /**
     * Non-constructor
     */
    private cowsay2unisay()
    {
	assert false : "This class [cowsay2unisay] is not meant to be instansiated.";
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
	    System.out.println("Cow cannibalisation tool");
	    System.out.println();
	    System.out.println("USAGE:  cowsay2unisay < SOURCE > TARGET");
	    System.out.println();
	    System.out.println("Source (STDIN):  Cow from cowsay");
	    System.out.println("Target (STDOUT): File name for new unisay cow");
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
	
	final InputStream in = System.in;
	final PrintStream out = System.out;
	final Scanner sc = new Scanner(in, "UTF-8");
	
	
	final StringBuilder cow = new StringBuilder();
	cow.append("#!/usr/bin/perl\n");
	cow.append("$thoughts = \"\\$\\\\\\$\";\n");
	cow.append("$tongue = \"\\$tongue\\$\";\n");
	cow.append("$eyes = \"\\$eye\\$eye\";\n");
	while (sc.hasNextLine())
	{
	    cow.append(sc.nextLine().replace("chop($eyes);", "\"\\$eye\\$\";\n$eyes = \"\\$eye\\$\";"));
	    cow.append("\n");
	}
	cow.append("print \"$the_cow\";\n");
	
	
	final String uni = new String(execCow(cow.toString()), "UTF-8");
	String line = uni.substring(0, uni.indexOf('\n'));
	int pos = line.indexOf("$\\$") + 3;
	if (pos > 3)
	    System.out.println("$balloon" + pos + "$"); 
	else
	    System.out.println("$balloon$"); 
	System.out.print(uni);
	System.out.flush();
    }
    
    
    /**
     * Executes a perl script
     *
     * @param   cow  The cow cowsay→unisay converting script in perl
     * @return       The cow in unisay
     * 
     * @throws  IOException  On perl executing failure
     */
    public static byte[] execCow(final String cow) throws IOException
    {
	final Process process = (new ProcessBuilder("perl")).start();
	final OutputStream cowout = process.getOutputStream();
	final InputStream stream = process.getInputStream();
	    
	cowout.write(cow.getBytes("UTF-8"));
	cowout.flush();
	try
	{
	    cowout.close();
	}
	catch (final Throwable err)
	{
	    //Ignore
	}
	    
	byte[] buf = new byte[2048];
	int ptr = 0;
	for (int d; (d = stream.read()) != -1;)
	{
	    buf[ptr++] = (byte)d;
	    if (ptr == buf.length)
	    {
		final byte[] nbuf = new byte[ptr << 1];
		System.arraycopy(buf, 0, nbuf, 0, ptr);
		buf = nbuf;
	    }
	}
	try
	{
	    stream.close();
	}
	catch (final Throwable err)
	{
	    //Ignore
	}
	final byte[] rc = new byte[ptr];
	System.arraycopy(buf, 0, rc, 0, ptr);
	return rc;
    }
    
}
