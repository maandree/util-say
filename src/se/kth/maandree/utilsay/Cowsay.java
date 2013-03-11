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
 * Cowsay support module
 * 
 * @author  Mattias Andrée, <a href="mailto:maandree@member.fsf.org">maandree@member.fsf.org</a>
 */
public class Cowsay extends Ponysay
{
    /**
     * Constructor
     * 
     * @param  flags  Flags passed to the module
     */
    public Cowsay(HashMap<String, String> flags)
    {
	super(Cowsay.modifyFlags(flags));
	this.flags = flags;
    }
    
    
    
    /**
     * Flags passed to the module
     */
    private HashMap<String, String> flags;
    
    
    
    /**
     * Import the pony from file
     * 
     * @return  The pony
     * 
     * @throws  IOException  On I/O error
     */
    public Pony importCow() throws IOException
    {
	InputStream in = System.in;
	if (this.file != null)
	    in = new BufferedInputStream(new FileInputStream(this.file));
	Scanner sc = new Scanner(in, "UTF-8");
	
	
	StringBuilder cow = new StringBuilder();
	StringBuilder data = new StringBuilder();
	boolean meta = false;
	
	cow.append("#!/usr/bin/perl\n");
	cow.append("$thoughts = \"\\$\\\\\\$\";\n");
	cow.append("$tongue = \"\\$tongue\\$\";\n");
	cow.append("$eyes = \"\\$eye\\$eye\";\n");
	while (sc.hasNextLine())
	{
	    String line = sc.nextLine();
	    if (line.replace("\t", "").replace(" ", "").startsWith("#"))
	    {
		if (meta == false)
		{
		    meta = true;
		    data.append("$$$\n");
		}
		line = line.substring(line.indexOf("#") + 1);
		if (line.equals("$$$"))
		    line = "$$$(!)";
		data.append(line + "\n");
		data.append('\n');
	    }
	    else
	    {
		cow.append(line.replace("chop($eyes);", "\"\\$eye\\$\";\n$eyes = \"\\$eye\\$\";"));
		cow.append('\n');
	    }
	}
	if (meta)
	    data.append("$$$\n");
	cow.append("print \"$the_cow\";\n");
	
	
	if (in != System.in)
	    in.close();
	
	
	String pony = new String(execCow(cow.toString()), "UTF-8");
	String line = pony.substring(0, pony.indexOf('\n'));
	int pos = line.indexOf("$\\$") + 3;
	
	if (pos > 3)
	    data.append("$balloon" + pos + "$"); 
	else
	    data.append("$balloon$"); 
	data.append(pony);
	
	
	InputStream stdin = System.in;
	try
	{
	    final byte[] streamdata = data.toString().getBytes("UTF-8");
	    System.setIn(new InputStream()
		{
		    int ptr = 0;
		    @Override
		    public int read()
		    {
			if (this.ptr == streamdata.length)
			    return -1;
			return streamdata[this.ptr++] & 255;
		    }
		    @Override
		    public int available()
		    {
			return streamdata.length - this.ptr;
		    }
		});
	    this.flags.put("file", null);
	    Ponysay ponysay = new Ponysay(this.flags);
	    if (ponysay.version == this.version)
		throw new Error("Default ponysay version should not be the cowsay version");
	    return ponysay.importPony();
	}
	finally
	{
	    System.setIn(stdin);
	}
    }
    
    
    /**
     * Executes a perl script
     *
     * @param   cow  The cow cowsay→ponysay converting script in perl
     * @return       The cow in ponysay
     * 
     * @throws  IOException  On perl executing failure
     */
    public static byte[] execCow(final String cow) throws IOException
    {
	Process process = (new ProcessBuilder("perl")).start();
	OutputStream cowout = process.getOutputStream();
	InputStream stream = process.getInputStream();
	    
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
		System.arraycopy(buf, 0, buf = new byte[ptr << 1], 0, ptr);
	}
	try
	{
	    stream.close();
	}
	catch (final Throwable err)
	{
	    //Ignore
	}
	System.arraycopy(buf, 0, buf = new byte[ptr], 0, ptr);
	return buf;
    }
    
    
    /**
     * Modify the flags to fit this module
     * 
     * @param   flag  The flags
     * @return        The flags
     */
    private static HashMap<String, String> modifyFlags(HashMap<String, String> flags)
    {
	flags.put("version", "0.1");
	return flags;
    }
    
}

