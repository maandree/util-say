/**
 * Truncater — Truncates a pony to fit the terminal
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


/**
 * The main class of the Truncater program
 *
 * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
 */
public class Truncater
{
    /**
     * Non-constructor
     */
    private Truncater()
    {
	assert false : "This class [Truncater] is not meant to be instansiated.";
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
	    System.out.println("Truncates a pony to fit the terminal");
            System.out.println();
            System.out.println("USAGE:  Truncater [TERMINAL_WIDTH [SANE_WIDTH]] < RAW > TRUNCATED");
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
	
	final int width  = args.length > 0 ? Integer.parseInt(args[0]) : getWidth();
	final int sanity = args.length > 1 ? Integer.parseInt(args[1]) : 16;
	if (width >= sanity)
	{
	    final OutputStream stdout = new BufferedOutputStream(System.out);
	    OutputStream out = new OutputStream()
		    {
			/**
			 * The number of column on the current line
			 */
			private int x = 0;
			
			/**
			 * Escape sequence state
			 */
			private int esc = 0;
			
			/**
			 * Last bytes as written
			 */
			private boolean ok = true;
			
			
			
			/**
			 * {@inheritDoc}
			 */
			@Override
			public void write(final int b) throws IOException
			{
			    if (this.esc == 0)
			    {
				if (b == '\n')
				{
				    if (x >= width)
				    {
					write('\033');
					write('[');
					write('4');
					write('9');
					write('m');
				    }
				    this.x = -1;
				}
				else if (b == '\t')
				{
				    int nx = 8 - (x & 7);
				    for (int i = 0; i < nx; i++)
					write(' ');
				    return; //(!)
				}
				else if (b == '\033')
				    this.esc = 1;
			    }
			    else if (this.esc == 1)
			    {
				if      (b == '[')  this.esc = 2;
				else if (b == ']')  this.esc = 3;
				else                this.esc = 10;
			    }
			    else if (this.esc == 2)
			    {
				if ((('a' <= b) && (b <= 'z')) || (('A' <= b) && (b <= 'Z')))
				    this.esc = 10;
			    }
			    else if ((this.esc == 3) && (b == 'P'))
			    {
				this.esc = ~0;
			    }
			    else if (this.esc < 0)
			    {
				this.esc--;
				if (this.esc == ~7)
				    this.esc = 10;
			    }
			    else
				this.esc = 10;
			            
			    if ((x < width) || (this.esc != 0) || (ok && ((b & 0xC0) == 0x80)))
			    {
				stdout.write(b);
				if (this.esc == 0)
				    if ((b & 0xC0) != 0x80)
					x++;
				ok = true;
			    }
			    else
				ok = false;
			    if (this.esc == 10)
				this.esc = 0;
			}
			
			/**
			 * {@inheritDoc}
			 */
			@Override
			public void flush() throws IOException
			{
			    stdout.flush();
			}
		};
	    
	    System.setOut(new PrintStream(out));
	}
	
	
	InputStream in = System.in;
	OutputStream out = System.out;
	
	for (int d; (d = in.read()) != -1;)
	    out.write(d);
	out.flush();
    }
    
    
    /**
     * Gets the width of the terminal
     *
     * @return  The width of the terminal
     */
    public static int getWidth()
    {
	try
	{
	    /* This can be done much better with Java 7 and stty. */
	    Process process = (new ProcessBuilder("/bin/sh", "-c", "tput cols 2> " + (new File("/dev/stderr")).getCanonicalPath())).start();
	    String rcs = new String();
	    InputStream stream = process.getInputStream();
	    int c;
	    while (((c = stream.read()) != '\n') && (c != -1))
		rcs += (char)c;
	    try
	    {
		stream.close();
	    }
	    catch (final Throwable err)
	    {
		//Ignore
	    }
	    return Integer.parseInt(rcs);
	}
	catch (final Throwable err)
        {
	    return -1;
	}
    }
}
