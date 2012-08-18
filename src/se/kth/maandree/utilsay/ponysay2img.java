/**
 * ponysay2img — Ponysay to image conversion tool
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

import javax.imageio.*;
import java.awt.*;
import java.awt.image.*;
import java.util.*;
import java.io.*;


/**
 * The main class of the ponysay2img program
 *
 * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
 */
public class ponysay2img
{
    /**
     * Non-constructor
     */
    private ponysay2img()
    {
	assert false : "This class [ponysay2img] is not meant to be instansiated.";
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
	if ((args.length == 0) || args[0].equals("--help"))
	{
	    System.out.println("ponysay to image conversion tool");
	    System.out.println();
            System.out.println("USAGE:  ponysay2img [-2] [-c] [-l] [--] TARGET < SOURCE");
            System.out.println();
            System.out.println("Source (STDOUT):  ponysay pony file to convert");
            System.out.println("Target:           File name for new image pony");
            System.out.println();
            System.out.println("-2  Output image should have double dimensioned pixels.");
            System.out.println("-c  Output image should be cropped to drawing.");
            System.out.println("-l  Encode balloon links.");
            System.out.println();
            System.out.println("Supported output formats:");
            System.out.println("  ⋅  PNG");
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
	
	
	final PipeOut pout = new PipeOut();
	final PipeIn pin = new PipeIn(pout);
	final PrintStream Pout = new PrintStream(pout);
	
	System.setIn(stdin);
	System.setOut(Pout);
	
	ponysay2unisay.main();
	Pout.close();
	
	System.setIn(pin);
	System.setOut(stdout);
	
	unisay2img.main(args);
    }
    
    
    private static final InputStream stdin = System.in;
    private static final PrintStream stdout = System.out;
    
    
    static class PipeOut extends OutputStream
    {
	public PipeOut()
	{
	    this.queue = new ArrayDeque<int[]>();
	    this.queue.offer(new int[128]);
	    this.wptr = this.rptr = 0;
	}
	
	private final ArrayDeque<int[]> queue;
	private int wptr;
	private int rptr;
	
	public void write(final int data)
	{
	    if (wptr == 128)
	    {
		wptr = 0;
		queue.offerLast(new int[128]);
	    }
	    
	    this.queue.peekLast()[wptr++] = data;
	}
	
	public int read()
	{
	    if (rptr == 128)
	    {
		rptr = 0;
		queue.pollFirst();
	    }
	    
	    if (this.queue.isEmpty())
		return -1;
	    
	    if (this.queue.size() == 1)
		if (this.rptr + 1 == this.wptr)
		{
		    this.queue.clear();
		    this.rptr = this.wptr = 0;
		    return -1;
		}
	    
	    return this.queue.peekFirst()[rptr++];
	}
	
	public void close()
	{
	    write(-1);
	}
    }
    
    static class PipeIn extends InputStream
    {
	@SuppressWarnings("hiding")
	public PipeIn(final PipeOut out)
	{
	    this.out = out;
	}
	
	private final PipeOut out;
	
	public int read()
	{
	    return out.read();
	}
    }
    
}
