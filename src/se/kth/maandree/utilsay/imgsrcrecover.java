/**
 * imgsrcrecover — Source image recover tool kit
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
 * The main class of the imgsrcrecover program
 *
 * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
 */
public class imgsrcrecover
{
    /**
     * Non-constructor
     */
    private imgsrcrecover()
    {
	assert false : "This class [imgsrcrecover] is not meant to be instansiated.";
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
        if      (args[0].equals("1") && (args.length == 3))  stage1(args[1], args[2]);
	else if (args[0].equals("2") && (args.length == 2))  stage2(args[1]);
	else if (args[0].equals("3") && (args.length == 3))  ;  //  ...
	else if (args[0].equals("4") && (args.length == 3))  ;  //  ...
	else if (args[0].equals("5") && (args.length == 5))  ;  //  ...
	else if (args[0].equals("6") && (args.length == 5))  ;  //  ...
	else if (args[0].equals("7") && (args.length == 6))  ;  //  ...
	else if (args[0].equals("8") && (args.length == 2))  ;  //  ...
	else
	{
	    boolean worked = false;
	    
	    if (args.length == 7)
	    {
		final String stages = args[0].replace("all", "12345678");
		
		if (stages.contains("1"))  main("1", args[1], args[2]);
		if (stages.contains("2"))  main("2", args[2]);
		if (stages.contains("3"))  main("3", args[2], args[4]);
		if (stages.contains("4"))  main("4", args[2], args[4]);
		if (stages.contains("5"))  main("5", args[2], args[3], args[4], args[5]);
		if (stages.contains("6"))  main("6", args[2], args[3], args[4], args[5]);
		if (stages.contains("7"))  main("7", args[2], args[3], args[4], args[5], args[6]);
		if (stages.contains("8"))  main("8", args[6]);
		
		if      (stages.contains("1"))  worked = true;
		else if (stages.contains("2"))  worked = true;  
		else if (stages.contains("3"))  worked = true;
		else if (stages.contains("4"))  worked = true;
		else if (stages.contains("5"))  worked = true;
		else if (stages.contains("6"))  worked = true;
		else if (stages.contains("7"))  worked = true;
		else if (stages.contains("8"))  worked = true;
	    }
	    
	    if (worked == false)
		return;
	    
	    System.out.println("Source image recover tool kit");
	    System.out.println();
	    System.out.println("USAGE:  ⋅ imgsrcrecover 1 SRCSRC SRC");
	    System.out.println("        ⋅ imgsrcrecover 2 SRC");
	    System.out.println("        ⋅ imgsrcrecover 3 SRC RES");
	    System.out.println("        ⋅ imgsrcrecover 4 SRC RES");
	    System.out.println("        ⋅ imgsrcrecover 5 SRC SRCHASH RES RESHASH");
	    System.out.println("        ⋅ imgsrcrecover 6 SRC SRCHASH RES RESHASH");
	    System.out.println("        ⋅ imgsrcrecover 7 SRC SRCHASH RES RESHASH MATCH");
	    System.out.println("        ⋅ imgsrcrecover 8 MATCH");
	    System.out.println("        ⋅ imgsrcrecover all SRCSRC SRC SRCHASH RES RESHASH MATCH");
	    System.out.println();
	    System.out.println("1  Stage 1:  Collect all image files in SRCSRC and subs and put in SRC");
	    System.out.println("2  Stage 2:  Burst all .gif files in SRC and delete bursted files");
	    System.out.println("3  Stage 3:  Crop all files in SRC and RES");
	    System.out.println("4  Stage 4:  Unzoom all files in SRC and RES as much as possible");
	    System.out.println("5  Stage 5:  Create alpha channel hash collection for all files in SRC to");
	    System.out.println("             the files SRCHASH and all from RES to the files RESHASH");
	    System.out.println("6  Stage 6:  Remove all unmatchable files from SRC, SRCHASH, RES and RESHASH");
	    System.out.println("7  Stage 7:  Match all files from RES(HASH) with SRC(HASH) and put in MATCH");
	    System.out.println("8  Stage 8:  Delete all incorrect matches from MATCH");
	    System.out.println();
	    System.out.println("all  Perform all stages at once");
	    System.out.println();
	    System.out.println("Known supported input formats:");
	    System.out.println("  ⋅  PNG  (non-animated)");
	    System.out.println("  ⋅  GIF  (animated)");
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
	
    }
    
    
    
    /**
     * Stage 2:  Burst all .gif files in SRC and delete bursted files
     */
    public static void stage2(final String src) throws IOException
    {
	final File dir = new File(src);
	String absdir = dir.getAbsolutePath();
	if (absdir.endsWith("/") == false)
	    absdir += '/';
	
	if (dir.exists() == false)
	{
	    System.err.println("Stage 2: File does not exists.  Stop.");
	    System.exit(-201);
	}
	if (dir.isDirectory() == false)
	{
	    System.err.println("Stage 2: File is not a directory.  Stop.");
	    System.exit(-202);
	}
	
	int ev;
	for (final String file : dir.list())
	    if (file.toLowerCase().endsWith(".gif"))
	    {
		final String abs = absdir + file;
		if ((ev = exec("gifasm", "-d", abs + '.', abs)) != 0)
		    System.err.println("\033[31mCan't(" + ev + ") burst " + abs + "\033[m");
	    }
    }
    
    /**
     * Stage 1:  Collect all image files in SRCSRC and subs and put in SRC
     */
    public static void stage1(final String srcsrc, final String src) throws IOException
    {
	final File root = new File(srcsrc);
	String absroot = root.getAbsolutePath();
	if (absroot.endsWith("/") == false)
	    absroot += '/';
	String srcd = src;
	if (srcd.endsWith("/") == false)
	    srcd += '/';
	
	if (root.exists() == false)
	{
	    System.err.println("Stage 1: File does not exists.  Stop.");
	    System.exit(-101);
	}
	if (root.isDirectory() == false)
	{
	    System.err.println("Stage 1: File is not a directory.  Stop.");
	    System.exit(-102);
	}
	
	final File dest = new File(src);
	if (dest.exists() == false)
	{
	    dest.mkdir();
	}
	else if (dest.isDirectory() == false)
	{
	    System.err.println("Stage 1: File is not a directory.  Stop.");
	    System.exit(-103);
	}
	
	final ArrayDeque<String> dirs = new ArrayDeque<String>();
	dirs.add(absroot);
	
	while (dirs.isEmpty() == false)
	{
	    int ev;
	    final String dir = dirs.pollLast();
	    final String pre = srcd + dir.substring(absroot.length()).replace("/", "\\");
	    for (final String file : (new File(dir)).list())
		if ((new File(dir + file)).isDirectory())
		    dirs.offerLast(dir + file + '/');
		else
		    if ((ev = exec("ln", "-s", dir + file, pre + file)) != 0)
			System.err.println("\033[31mCan't(" + ev + ") symlink " + dir + file + "  →  " + pre + file + "\033[m");
	}
    }
    
    
    //* Easiest way to copy files without Java7, not too hard: exec("cp", src, dest) *//
    //* And "only" way to link without Java7: exec("ln", ["-s"], from, to)*//
    public static int exec(final String... command)
    {
	try
	{
	    final Process process = (new ProcessBuilder(command)).start();
	    process.waitFor();
	    return process.exitValue();
	}
	catch (final Throwable err)
	{
	    return ~0;
	}
    }

}

