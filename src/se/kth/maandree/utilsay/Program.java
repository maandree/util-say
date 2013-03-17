/**
 * util-say — Utilities for cowsay and cowsay-like programs
 *
 * Copyright © 2012, 2013  Mattias Andrée (maandree@member.fsf.org)
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
 * Program selector for util-say
 *
 * @author  Mattias Andrée, <a href="mailto:maandree@member.fsf.org">maandree@member.fsf.org</a>
 */
public class Program
{
    /**
     * Non-constructor
     */
    private Program()
    {
	assert false : "This class [Program] is not meant to be instansiated.";
    }
    
    
    
    /**
     * This is the main entry point of the program
     * 
     * @param  args  Startup arguments
     * 
     * @throws  IOException  On I/O exception
     */
    public static void main(final String... args) throws IOException
    {
	if ((args.length == 0) || ((args.length == 1) && args[0].equals("--help")))
	{
            System.out.println("Copyright (C) 2012, 2013  Mattias Andrée <maandree@member.fsf.org>");
            System.out.println();
            System.out.println("You can use --list to get a list of all programs");
            System.out.println("USAGE:  util-say [--help | PROGRAM ARGUMENTS...]");
	    System.out.println();
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
	
	HashMap<String, String> params = new HashMap<String, String>();
	String pname = null;
	HashMap<String, String> inparams = null;
	String intype = null;
	final ArrayList<HashMap<String, String>> outparams = new ArrayList<HashMap<String, String>>();
	final ArrayList<String> outtypes = new ArrayList<String>();
	
	for (final String arg : args)
	    if (arg.equals("--in") || arg.equals("--import") || arg.equals("--out") || arg.equals("--export"))
	    {	if (pname != null)
		    params.put(pname, "yes");
		pname = arg.intern();
	    }
	    else if ((pname == "--in") || (pname == "--import"))
	    {	inparams = params;
		intype = arg.toLowerCase().intern();
		params = new HashMap<String, String>();
		pname = null;
	    }
	    else if ((pname == "--out") || (pname == "--export"))
	    {   outparams.add(params);
		outtypes.add(arg.toLowerCase().intern());
		params = new HashMap<String, String>();
		pname = null;
	    }
	    else if (arg.startsWith("--") || (pname == null))
	    {	if (pname != null)
		    params.put(pname, "yes");
		int eq = arg.indexOf("=");
		if (eq < 0)
		    pname = arg.replace("-", "");
		else
		{   pname = null;
		    params.put(arg.substring(0, eq).replace("-", ""), arg.substring(eq + 1));
	    }   }
	    else
	    {	params.put(pname, arg);
		pname = null;
	    }
	
	Pony pony = null;
	if      (intype == "ponysay")  pony = (new Ponysay(inparams)).importPony();
	else if (intype == "unisay")   pony = (new Unisay (inparams)).importPony();
	else if (intype == "cowsay")   pony = (new Cowsay (inparams)).importPony();
	else if (intype == "image")    pony = (new Image  (inparams)).importPony();
	
	for (int i = 0, n = outtypes.size(); i < n; i++)
	{   final String outtype = outtypes.get(i);
	    params = outparams.get(i);
	    
	    if      (outtype == "ponysay")  (new Ponysay(params)).exportPony(pony);
	    else if (outtype == "unisay")   (new Unisay (params)).exportPony(pony);
	    else if (outtype == "cowsay")   (new Cowsay (params)).exportPony(pony);
	    else if (outtype == "image")    (new Image  (params)).exportPony(pony);
	}
    }
}
