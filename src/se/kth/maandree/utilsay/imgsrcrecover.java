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
import java.lang.ref.*;
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
        if      ((args.length == 3) && args[0].equals("1"))  stage1(args[1], args[2]);
	else if ((args.length == 2) && args[0].equals("2"))  stage2(args[1]);
	else if ((args.length == 3) && args[0].equals("3"))  stage3(args[1], args[2]);
	else if ((args.length == 3) && args[0].equals("4"))  stage4(args[1], args[2]);
	else if ((args.length == 5) && args[0].equals("5"))  stage5(args[1], args[2], args[3], args[4]);
	else if ((args.length == 5) && args[0].equals("6"))  stage6(args[1], args[2], args[3], args[4]);
	else if ((args.length == 6) && args[0].equals("7"))  stage7(args[1], args[2], args[3], args[4], args[5]);
	else
	{
	    boolean worked = false;
	    
	    if (args.length == 7)
	    {
		final String stages = args[0].replace("all", "1234567");
		
		if (stages.contains("1"))  main("1", args[1], args[2]);
		if (stages.contains("2"))  main("2", args[2]);
		if (stages.contains("3"))  main("3", args[2], args[4]);
		if (stages.contains("4"))  main("4", args[2], args[4]);
		if (stages.contains("5"))  main("5", args[2], args[3], args[4], args[5]);
		if (stages.contains("6"))  main("6", args[2], args[3], args[4], args[5]);
		if (stages.contains("7"))  main("7", args[2], args[3], args[4], args[5], args[6]);
		
		if      (stages.contains("1"))  worked = true;
		else if (stages.contains("2"))  worked = true;  
		else if (stages.contains("3"))  worked = true;
		else if (stages.contains("4"))  worked = true;
		else if (stages.contains("5"))  worked = true;
		else if (stages.contains("6"))  worked = true;
		else if (stages.contains("7"))  worked = true;
	    }
	    
	    if (worked)
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
	    System.out.println("             and delete all incorrect matches from MATCH");
	    System.out.println();
	    System.out.println("all  Perform all stages at once");
	    System.out.println();
	    System.out.println("Known supported input formats:");
	    System.out.println("  ⋅  PNG      (non-animated)");
	    System.out.println("  ⋅  GIF      (animated)");
	    System.out.println("  ⋅  ponysay  (must have file name extension: .pony)");
	    System.out.println("  ⋅  unisay");
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
     * Stage 7:  Match all files from RES(HASH) with SRC(HASH) and put in MATCH and delete all incorrect matches from MATCH
     */
    @SuppressWarnings("unchecked")
    public static void stage7(final String src, final String srchash, final String res, final String reshash, final String match) throws IOException
    {
	final File dirsrc = new File(src);
	final File dirres = new File(res);
	final File fsrchash = new File(srchash);
	final File freshash = new File(reshash);
	final File dirmatch = new File(match);
	
	String abssrc = dirsrc.getAbsolutePath();
	if (abssrc.endsWith("/") == false)
	    abssrc += '/';
	String absres = dirres.getAbsolutePath();
	if (absres.endsWith("/") == false)
	    absres += '/';
	String absmatch = dirmatch.getAbsolutePath();
	if (absmatch.endsWith("/") == false)
	    absmatch += '/';
	
	if (dirsrc.exists() == false)
	{
	    System.err.println("Stage 7: File does not exists.  Stop.");
	    System.exit(-701);
	}
	if (dirsrc.isDirectory() == false)
	{
	    System.err.println("Stage 7: File is not a directory.  Stop.");
	    System.exit(-702);
	}
	if (dirres.exists() == false)
	{
	    System.err.println("Stage 7: File does not exists.  Stop.");
	    System.exit(-703);
	}
	if (dirres.isDirectory() == false)
	{
	    System.err.println("Stage 7: File is not a directory.  Stop.");
	    System.exit(-704);
	}
	if (fsrchash.exists())
	{
	    System.err.println("Stage 7: File already exists.  Stop.");
	    System.exit(-705);
	}
	if (freshash.exists())
	{
	    System.err.println("Stage 7: File already exists.  Stop.");
	    System.exit(-706);
	}
	if (dirmatch.exists() == false)
	{
	    dirmatch.mkdir();
	}
	else if (dirmatch.isDirectory() == false)
	{
	    System.err.println("Stage 7: File is not a directory.  Stop.");
	    System.exit(-707);
	}
	
	final HashMap<Long, ArrayList<String>> srcmap = new HashMap<Long, ArrayList<String>>();
	final HashMap<Long, ArrayList<String>> resmap = new HashMap<Long, ArrayList<String>>();
	
	for (final Object[] objs : new Object[][] {{fsrchash, srcmap}, {freshash, resmap}})
	{
	    final File hash = (File)(objs[0]);
	    final HashMap<Long, ArrayList<String>> map = (HashMap<Long, ArrayList<String>>)(objs[1]);
	    
	    final Scanner sc = new Scanner(new BufferedInputStream(new FileInputStream(hash)));
	    while (sc.hasNext())
	    {
		final String line = sc.nextLine();
		if (line.isEmpty())
		    break;
		
		final int space = line.indexOf(' ');
		final Long key = Long.valueOf(line.substring(0, space));
		final String value = line.substring(space + 1).replace("/", "\n");
		ArrayList<String> values = map.get(key);
		if (values == null)
		    map.put(key, values = new ArrayList<String>());
		values.add(value);
	    }
	}
	
	final Set<Long> keys = srcmap.keySet();
	for (final Long key : keys)
        {
	    final ArrayList<String> srcs = srcmap.get(key);
	    final ArrayList<String> ress = srcmap.get(key);
	    final HashMap<String, SoftReference<long[]>> map = new HashMap<String, SoftReference<long[]>>();
	    
	    for (final String relsrc : srcs)
	    {
		final String asrc = abssrc + relsrc;
		final long[] dsrc;
		final int sw, sh;
		
		{
		    final BufferedImage img = ImageIO.read(new File(asrc));
		    sw = img.getWidth();
		    sh = img.getHeight();
		    dsrc = new long[((sw * sh) + 63) >> 6];
		    
		    long p = 0;
		    for (int y = 0; y < sh; y++)
			for (int x = 0; x < sw; x++, p++)
			    if ((img.getRGB(x, y) & 0xFF000000) != 0)
				dsrc[(int)(p >> 6)] ^= 1L << (p & 63);
		}
		
		mid:
		    for (final String relres : ress)
		    {
			final String ares = absres + relres;
			final SoftReference<long[]> dressr = map.get(relres);
			long[] dres = dressr == null ? null : dressr.get();
			
			final BufferedImage img = ImageIO.read(new File(ares));
			final int w = img.getWidth(), h = img.getHeight();
			if ((w != sw) || (h != sh))
			    continue;
			
			if (dres == null)
			{
			    dres = new long[((w * h) + 63) >> 6];
			    long p = 0;
			    for (int y = 0; y < h; y++)
				for (int x = 0; x < w; x++, p++)
				    if ((img.getRGB(x, y) & 0xFF000000) != 0)
					dres[(int)(p >> 6)] ^= 1L << (p & 63);
			    
			    map.put(relres, new SoftReference<long[]>(dres));
			}
			
			for (int i = 0, n = dsrc.length; i < n; i++)
			    if (dsrc[i] != dres[i])
				continue mid;
			
			int ev;
			if (exec("ln", "-P", ares, absmatch + relres) != 0)
			    if ((ev = exec("cp", ares, absmatch + relres)) != 0)
				System.err.println("\033[31mCan't(" + ev + ") copy " + ares + "  →  " + absmatch + relres + "\033[m");
		    }
		
		(new File(abssrc + relsrc)).delete();
	    }
	    
	    for (final String relres : ress)
		(new File(absres + relres)).delete();
	}
    }
    
    
    /**
     * Stage 6:  Remove all unmatchable files from SRC, SRCHASH, RES and RESHASH
     */
    @SuppressWarnings("unchecked")
    public static void stage6(final String src, final String srchash, final String res, final String reshash) throws IOException
    {
	final File dirsrc = new File(src);
	final File dirres = new File(res);
	final File fsrchash = new File(srchash);
	final File freshash = new File(reshash);
	
	String abssrc = dirsrc.getAbsolutePath();
	if (abssrc.endsWith("/") == false)
	    abssrc += '/';
	String absres = dirres.getAbsolutePath();
	if (absres.endsWith("/") == false)
	    absres += '/';
	
	if (dirsrc.exists() == false)
	{
	    System.err.println("Stage 6: File does not exists.  Stop.");
	    System.exit(-601);
	}
	if (dirsrc.isDirectory() == false)
	{
	    System.err.println("Stage 6: File is not a directory.  Stop.");
	    System.exit(-602);
	}
	if (dirres.exists() == false)
	{
	    System.err.println("Stage 6: File does not exists.  Stop.");
	    System.exit(-603);
	}
	if (dirres.isDirectory() == false)
	{
	    System.err.println("Stage 6: File is not a directory.  Stop.");
	    System.exit(-604);
	}
	if (fsrchash.exists())
	{
	    System.err.println("Stage 6: File already exists.  Stop.");
	    System.exit(-605);
	}
	if (freshash.exists())
	{
	    System.err.println("Stage 6: File already exists.  Stop.");
	    System.exit(-606);
	}
	
	final ArrayDeque<long[]> srchashes = new ArrayDeque<long[]>();
	final ArrayDeque<long[]> reshashes = new ArrayDeque<long[]>();
	final ArrayDeque<String> srcfiles  = new ArrayDeque<String>();
	final ArrayDeque<String> resfiles  = new ArrayDeque<String>();
	
	for (final Object[] objs : new Object[][] {{fsrchash, srchashes, srcfiles}, {freshash, reshashes, resfiles}})
	{
	    final File hash = (File)(objs[0]);
	    final ArrayDeque<long[]> hashes = (ArrayDeque<long[]>)(objs[1]);
	    final ArrayDeque<String> files  = (ArrayDeque<String>)(objs[2]);
	    
	    final Scanner sc = new Scanner(new BufferedInputStream(new FileInputStream(hash)));
	    while (sc.hasNext())
	    {
		final String line = sc.nextLine();
		if (line.isEmpty())
		    break;
		
		final int space = line.indexOf(' ');
		hashes.offerLast(new long[] { Long.parseLong(line.substring(0, space)) });
		files.offerLast(line.substring(space + 1).replace("/", "\n"));
	    }
	}
	
	final ArrayDeque<String> srcok  = new ArrayDeque<String>();
	final ArrayDeque<String> resok  = new ArrayDeque<String>();
	
	while ((srcfiles.isEmpty() || resfiles.isEmpty()) == false)
	{
	    final long srch = srchashes.peekLast()[0];
	    final long resh = reshashes.peekLast()[0];
	    
	    if (srch > resh)
	    {
		srchashes.pollLast();
		(new File(abssrc + srcfiles.pollLast())).delete();
	    }
	    else if (resh > resh)
	    {
		reshashes.pollLast();
		(new File(absres + resfiles.pollLast())).delete();
	    }
	    else
	    {
		do
		{
		    srchashes.pollLast();
		    srcok.offerLast(Long.toString(srch) + " " + srcfiles.pollLast().replace("\n", "/"));
		}
		  while (srchashes.peekLast()[0] == srch);
		
		do
		{
		    reshashes.pollLast();
		    resok.offerLast(Long.toString(resh) + " " + resfiles.pollLast().replace("\n", "/"));
		}
	          while (reshashes.peekLast()[0] == resh);
	    }
	}
	
	while (srcfiles.isEmpty() == false)
	{
	    srchashes.pollLast();
	    (new File(abssrc + srcfiles.pollLast())).delete();
	}
	
	while (resfiles.isEmpty() == false)
	{
	    reshashes.pollLast();
	    (new File(absres + resfiles.pollLast())).delete();
	}
	
	
	{
	    final PrintStream fout = new PrintStream(new BufferedOutputStream(new FileOutputStream(fsrchash)));
	    while (srcok.isEmpty() == false)
		fout.println(srcok.pollLast());
	    fout.flush();
	    fout.close();
	}
	{
	    final PrintStream fout = new PrintStream(new BufferedOutputStream(new FileOutputStream(freshash)));
	    while (resok.isEmpty() == false)
		fout.println(resok.pollLast());
	    fout.flush();
	    fout.close();
	}
    }
    
    
    /**
     * Stage 5:  Create alpha channel hash collection for all files in SRC
     *           to the files SRCHASH and all from RES to the files RESHASH
     */
    public static void stage5(final String src, final String srchash, final String res, final String reshash) throws IOException
    {
	final File dirsrc = new File(src);
	final File dirres = new File(res);
	final File fsrchash = new File(srchash);
	final File freshash = new File(reshash);
	
	String abssrc = dirsrc.getAbsolutePath();
	if (abssrc.endsWith("/") == false)
	    abssrc += '/';
	String absres = dirres.getAbsolutePath();
	if (absres.endsWith("/") == false)
	    absres += '/';
	
	if (dirsrc.exists() == false)
	{
	    System.err.println("Stage 5: File does not exists.  Stop.");
	    System.exit(-501);
	}
	if (dirsrc.isDirectory() == false)
	{
	    System.err.println("Stage 5: File is not a directory.  Stop.");
	    System.exit(-502);
	}
	if (dirres.exists() == false)
	{
	    System.err.println("Stage 5: File does not exists.  Stop.");
	    System.exit(-503);
	}
	if (dirres.isDirectory() == false)
	{
	    System.err.println("Stage 5: File is not a directory.  Stop.");
	    System.exit(-504);
	}
	if (fsrchash.exists())
	{
	    System.err.println("Stage 5: File already exists.  Stop.");
	    System.exit(-505);
	}
	if (freshash.exists())
	{
	    System.err.println("Stage 5: Files already exists.  Stop.");
	    System.exit(-506);
	}
	
	for (final String[] dirhash : new String[][] {{abssrc, srchash}, {absres, reshash}})
	{
	    final String dir = dirhash[0];
	    final String hash = dirhash[1];
	    final String[] files = (new File(dir)).list();
	    final String[] hashes = new String[files.length];
	    
	    int findex = 0;
	    for (final String file : files)
	    {
		final BufferedImage img = ImageIO.read(new File(dir + file));
		final int w = img.getWidth(), h = img.getHeight();
		
		long ihash = 0;
		long p = 1;
		
		for (int y = 0; y < h; y++)
		    for (int x = 0; x < w; x++)
		    {
			ihash ^= (img.getRGB(x, y) & 0xFF000000) == 0 ? 0 : p;
			p <<= 1;
			if (p == 0)
			    p = 1;
		    }
		
		hashes[findex++] = Long.toString(ihash) + " " + file.replace("\n", "/");
	    }
	    
	    Arrays.sort(hashes);
	    
	    final PrintStream fout = new PrintStream(new BufferedOutputStream(new FileOutputStream(new File(hash))));
	    for (final String line : hashes)
		fout.println(line);
	    fout.flush();
	    fout.close();
	}
    }
    
    
    /**
     * Stage 4:  Unzoom all files in SRC and RES as much as possible
     */
    public static void stage4(final String src, final String res) throws IOException
    {
	final File dirsrc = new File(src);
	final File dirres = new File(res);
	
	String abssrc = dirsrc.getAbsolutePath();
	if (abssrc.endsWith("/") == false)
	    abssrc += '/';
	String absres = dirres.getAbsolutePath();
	if (absres.endsWith("/") == false)
	    absres += '/';
	
	if (dirsrc.exists() == false)
	{
	    System.err.println("Stage 4: File does not exists.  Stop.");
	    System.exit(-401);
	}
	if (dirsrc.isDirectory() == false)
	{
	    System.err.println("Stage 4: File is not a directory.  Stop.");
	    System.exit(-402);
	}
	if (dirres.exists() == false)
	{
	    System.err.println("Stage 4: File does not exists.  Stop.");
	    System.exit(-403);
	}
	if (dirres.isDirectory() == false)
	{
	    System.err.println("Stage 4: File is not a directory.  Stop.");
	    System.exit(-404);
	}
	
	for (final String dir : new String[] {abssrc, absres})
	    for (final String file : (new File(dir)).list())
	    {
		final BufferedImage img = ImageIO.read(new File(dir + file));
		final int w = img.getWidth(), h = img.getHeight();
		
		int zoom = 1;
		outer:
		    for (int z = 2; (z <= w) && (z <= h); z++)
		    {
			if ((w % z != 0) || (h % z != 0))
			    continue;
			
			int[] ps = new int[z * z];
			
			for (int y = 0; y < h; y += z)
			    for (int x = 0; x < w; x += z)
			    {
				/* Not using 'final' here, it could be heavy duty. */
				
				int ref = img.getRGB(x, y);
				img.getRGB(x, y, z, z, ps, 0, z);
				for (final int p : ps)
				    if (p != ref)
					break outer;
			    }
			
			zoom = z;
		    }
		
		final int zw = w / zoom, zh = h / zoom;
		final BufferedImage cimg = new BufferedImage(zw, zh, BufferedImage.TYPE_INT_ARGB);
		
		for (int y = 0, zy = 0; y < h; y += zoom, zy++)
		    for (int x = 0, zx = 0; x < w; x += zoom, zx++)
			cimg.setRGB(zx, zy, img.getRGB(x, y));
		
		ImageIO.write(cimg, file.substring(file.lastIndexOf('.') + 1).toUpperCase(), new File(dir + file));
	    }
    }
    
    
    /**
     * Stage 3:  Crop all files in SRC and RES
     */
    public static void stage3(final String src, final String res) throws IOException
    {
	final File dirsrc = new File(src);
	final File dirres = new File(res);
	
	String abssrc = dirsrc.getAbsolutePath();
	if (abssrc.endsWith("/") == false)
	    abssrc += '/';
	String absres = dirres.getAbsolutePath();
	if (absres.endsWith("/") == false)
	    absres += '/';
	
	if (dirsrc.exists() == false)
	{
	    System.err.println("Stage 3: File does not exists.  Stop.");
	    System.exit(-301);
	}
	if (dirsrc.isDirectory() == false)
	{
	    System.err.println("Stage 3: File is not a directory.  Stop.");
	    System.exit(-302);
	}
	if (dirres.exists() == false)
	{
	    dirres.mkdir();
	}
	else if (dirres.isDirectory() == false)
	{
	    System.err.println("Stage 3: File is not a directory.  Stop.");
	    System.exit(-303);
	}
	
	for (final String dir : new String[] {abssrc, absres})
	    for (final String file : (new File(dir)).list())
	    {
		BufferedImage img = ImageIO.read(new File(dir + file));
		if (img == null) // not an image file
		{
		    final InputStream stdin = System.in;
		    System.setIn(new BufferedInputStream(new FileInputStream(new File(dir + file))));
		    
		    if (file.endsWith(".pony"))  ponysay2img.main("--", dir + file);
		    else                         unisay2img.main("--", dir + file);
		    
		    img = ImageIO.read(new File(dir + file));
		    System.setIn(stdin);
		}
		final int w = img.getWidth(), h = img.getHeight();
		int top = 0, bottom = 0, left = 0, right = 0;
		
		int[] argbs = new int[w];
		
		loopTop:
		    for (; top < h; top++)
		    {
			img.getRGB(0, top, w, 1, argbs, 0, w);
			for (final int argb : argbs)
			    if ((argb & 0xFF000000) != 0)
				break loopTop;
		    }
		    
		loopBottom:
		    for (; bottom < h - top; bottom++)
		    {
			img.getRGB(0, h - bottom - 1, w, 1, argbs, 0, w);
			for (final int argb : argbs)
			    if ((argb & 0xFF000000) != 0)
				break loopBottom;
		    }
		    
		final int ch;
		argbs = new int[ch = h - top - bottom];
		    
		loopLeft:
		    for (; left < w; left++)
		    {
			img.getRGB(left, top, 1, ch, argbs, 0, 1);
			for (final int argb : argbs)
			    if ((argb & 0xFF000000) != 0)
				break loopLeft;
		    }
		    
		loopRight:
		    for (; right < w - left; right++)
		    {
			img.getRGB(w - right - 1, top, 1, ch, argbs, 0, 1);
			for (final int argb : argbs)
			    if ((argb & 0xFF000000) != 0)
				break loopRight;
		    }
		    
		final int cw = w - left - right;
		
		final BufferedImage cimg = new BufferedImage(cw, ch, BufferedImage.TYPE_INT_ARGB);
		argbs = new int[ch * cw];
		img.getRGB(left, right, cw, ch, argbs, 0, cw);
		cimg.setRGB(0, 0, cw, ch, argbs, 0, cw);
		ImageIO.write(cimg, file.substring(file.lastIndexOf('.') + 1).toUpperCase(), new File(dir + file));
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
		else
		    exec("rm", abs);
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
		    if ((ev = exec("cp", dir + file, pre + file)) != 0)
			System.err.println("\033[31mCan't(" + ev + ") copy " + dir + file + "  →  " + pre + file + "\033[m");
	}
    }
    
    
    //* Easiest way to copy files without Java7, not too hard: exec("cp", src, dest) *//
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

