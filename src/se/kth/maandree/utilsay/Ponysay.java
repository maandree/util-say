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
import java.awt.*;
import java.awt.image.*;


// This class should be tried to be keeped as optimised as possible for the
// lastest version of ponysay (only) without endangering its maintainability.


/**
 * Ponysay support module
 * 
 * @author  Mattias Andrée, <a href="mailto:maandree@member.fsf.org">maandree@member.fsf.org</a>
 */
public class Ponysay
{
    /**
     * Until, and including, version 2.0 of ponysay the cowsay format was used
     */
    private static final int VERSION_COWSAY = 0;
    
    /**
     * Until, and including, version 2.8 of ponysay the unisay format was used
     */
    private static final int VERSION_UNISAY = 1;
    
    /**
     * Until, but excluding, version 3.0 of ponysay's pony format was extended to support metadata
     */
    private static final int VERSION_METADATA = 2;
    
    /**
     * In version 3.0 of ponysay's pony format was extended to support horizontal justifiction of balloons
     */
    private static final int VERSION_HORIZONTAL_JUSTIFICATION = 3;
    
    
    
    /**
     * Constructor
     * 
     * @param  flags  Flags passed to the module
     */
    public Ponysay(HashMap<String, String> flags)
    {
	this.file = (flags.containsKey("file") ? (this.file = flags.get("file")).equals("-") : true) ? null : this.file;
	this.even = (flags.containsKey("even") == false) || flags.get("even").toLowerCase().startsWith("y");
	this.fullblocks = (flags.containsKey("fullblocks") == false) || flags.get("fullblocks").toLowerCase().startsWith("y");
	this.spacesave = flags.containsKey("spacesave") && flags.get("spacesave").toLowerCase().startsWith("y");
	this.zebra = flags.containsKey("zebra") && flags.get("zebra").toLowerCase().startsWith("y");
	this.version = flags.containsKey("version") ? parseVersion(flags.get("version")) : VERSION_HORIZONTAL_JUSTIFICATION;
	this.utf8 = this.version > VERSION_COWSAY ? true : (flags.containsKey("utf8") && flags.get("utf8").toLowerCase().startsWith("y"));
	this.balloon = (flags.containsKey("balloon") == false) ? -1 : Common.parseInteger(flags.get("balloon"), 2);
	this.left = (flags.containsKey("left") == false) ? 3 : Common.parseInteger(flags.get("left"), 3);
	this.right = (flags.containsKey("right") == false) ? 0 : Common.parseInteger(flags.get("right"), 0);
	this.top = (flags.containsKey("top") == false) ? 1 : Common.parseInteger(flags.get("top"), 1);
	this.bottom = (flags.containsKey("bottom") == false) ? 1 : Common.parseInteger(flags.get("bottom"), 1);
	this.ignoreballoon = flags.containsKey("ignoreballoon") && flags.get("ignoreballoon").toLowerCase().startsWith("y");
	this.ignorelink = flags.containsKey("ignorelink") ? flags.get("ignorelink").toLowerCase().startsWith("y") : this.ignoreballoon;
	this.escesc = this.version > VERSION_COWSAY ? false : (flags.containsKey("escesc") && flags.get("escesc").toLowerCase().startsWith("y"));
	
	String platform = flags.get("platform");
	if (platform != null)
        {   platform = platform.toLowerCase().replace("-", "").replace("_", "").replace(" ", "");
	    platform = platform.replace("colour", "color");
	    platform = platform.replace("colors", "color");
	    platform = platform.intern();
	}
	if ((platform == "xterm") || (platform == "xterm256color"))
	    this.submodule = new PonysayXterm(flags);
	else if ((platform == "linux") || (platform == "tty"))
	    this.submodule = new PonysayLinux(flags);
	else if (platform == "haiku")
	    this.submodule = new PonysayHaiku(flags);
	else
	{   this.submodule = new PonysayXterm(flags);
	    if (platform != null)
		System.err.println("\033[01;31mutil-say: warning: ponysay submodule does not exist: " + platform + "\033[00m");
	}
    }
    
    
    
    /**
     * Input/output option: pony file
     */
    protected String file;
    
    /**
     * Input/output option: ignore the balloon
     */
    protected boolean ignoreballoon;
    
    /**
     * Input/output option: ignore the balloon link
     */
    protected boolean ignorelink;
    
    /**
     * Output option: pad right side
     */
    protected boolean even;
    
    /**
     * Output option: allow solid block elements
     */
    protected boolean fullblocks;
    
    /**
     * Output option: make foreground for whitespace the same as the background
     */
    protected boolean spacesave;
    
    /**
     * Output option: zebra effect
     */
    protected boolean zebra;
    
    /**
     * Input/output option: ponysay version
     */
    protected int version;
    
    /**
     * Output option: use utf8 encoding on pixels
     */
    protected boolean utf8;
    
    /**
     * Output option: escape escape charactes
     */
    protected boolean escesc;
    
    /**
     * Output option: left margin, negative for unmodified
     */
    protected int left;
    
    /**
     * Output option: right margin, negative for unmodified
     */
    protected int right;
    
    /**
     * Output option: top margin, negative for unmodified
     */
    protected int top;
    
    /**
     * Output option: bottom margin, negative for unmodified
     */
    protected int bottom;
    
    /**
     * Input/output option: insert balloon into the image, negative for false, otherwise the additional space between the pony and balloon
     */
    protected int balloon;
    
    /**
     * Input/output option: colouring submodule
     */
    protected PonysaySubmodule submodule;
    
    
    
    /**
     * Import the pony from file
     * 
     * @return  The pony
     * 
     * @throws  IOException  On I/O error
     */
    public Pony importPony() throws IOException
    {
	if (this.version == VERSION_COWSAY)
	    return this.importCow();
	
	
	boolean[] PLAIN = new boolean[9];
	
	Color[] colours = new Color[256];
	boolean[] format = new boolean[9];
	Color background = null, foreground = null;
	
	for (int i = 0; i < 256; i++)
	{   Colour colour = new Colour(i);
	    colours[i] = new Color(colour.red, colour.green, colour.blue);
	}
	submodule.initImport(colours);
	
	InputStream in = System.in;
	if (this.file != null)
	    in = new BufferedInputStream(new FileInputStream(this.file));
	
	boolean dollar = false;
	boolean escape = false;
	
	int[] buf = new int[256];
	int ptr = 0;
	int dollareql = -1;
	
	int width = 0;
	int curwidth = 0;
	int height = 1;
	
	LinkedList<Object> items = new LinkedList<Object>();
	String comment = null;
	String[][] tags = null;
	int tagptr = 0;
	
	int[] unmetabuf = new int[4];
	int unmetaptr = 0;
	unmetabuf[unmetaptr++] = in.read();
	unmetabuf[unmetaptr++] = in.read();
	unmetabuf[unmetaptr++] = in.read();
	unmetabuf[unmetaptr++] = in.read();
	if ((unmetabuf[0] == '$') && (unmetabuf[1] == '$') && (unmetabuf[2] == '$') && (unmetabuf[3] == '\n'))
	{   unmetaptr = 0;
	    byte[] data = new byte[256];
	    int d = 0;
	    while ((d = in.read()) != -1)
	    {
		if (ptr == data.length)
		    System.arraycopy(data, 0, data = new byte[ptr << 1], 0, ptr);
		data[ptr++] = (byte)d;
		if ((ptr >= 5) && (data[ptr - 1] == '\n') && (data[ptr - 2] == '$') && (data[ptr - 3] == '$') && (data[ptr - 4] == '$') && (data[ptr - 5] == '\n'))
		{   ptr -= 5;
		    break;
		}
		if ((ptr == 4) && (data[ptr - 1] == '\n') && (data[ptr - 2] == '$') && (data[ptr - 3] == '$') && (data[ptr - 4] == '$'))
		{   ptr -= 4;
		    break;
		}
	    }
	    if (d == -1)
		throw new RuntimeException("Metadata was never closed");
	    String[] code = (new String(data, 0, ptr, "UTF-8")).split("\n");
	    StringBuilder commentbuf = new StringBuilder();
	    for (String line : code)
	    {
		int colon = line.indexOf(':');
		boolean istag = colon > 0;
		String name = null, value = null;
		block: {
		    if (istag)
		    {	istag = false;
			name = line.substring(0, colon).replace('\t', ' ');
			value = line.substring(colon + 1).replace('\t', ' ');
			char c;
			for (int i = 0, n = name.length(); i < n; i++)
			    if ((c = name.charAt(i)) != ' ')
				if (('A' > c) || (c > 'Z'))
				    break block;
			istag = true;
		    }}
		if (istag)
		{   if (tags == null)
			tags = new String[32][];
		    else if (tagptr == tags.length)
			System.arraycopy(tags, 0, tags = new String[tagptr << 1][], 0, tagptr);
		    tags[tagptr++] = new String[] {name.trim(), value.trim()};
		}
		else
		{   commentbuf.append(line);
		    commentbuf.append('\n');
		}
	    }
	    ptr = 0;
	    comment = commentbuf.toString();
	    while ((ptr < comment.length()) && (comment.charAt(ptr) == '\n'))
		ptr++;
	    if (ptr > 0)
	    {   comment = comment.substring(ptr);
		ptr = 0;
	    }
	    if (comment.isEmpty())
		comment = null;
	    if ((tags != null) && (tagptr < tags.length))
		System.arraycopy(tags, 0, tags = new String[tagptr][], 0, tagptr);
	}
	
	for (int d = 0, stored = -1, c;;)
	{
	    if (unmetaptr > 0)
	    {   d = unmetabuf[3 - --unmetaptr];
		if (d == -1)
		    break;
	    }
	    else if ((d = stored) != -1)
		stored = -1;
	    else if ((d = in.read()) == -1)
		break;
	    
	    if (((c = d) & 0x80) == 0x80)
	    {   int n = 0;
		while ((c & 0x80) == 0x80)
		{   c <<= 1;
		    n++;
		}
		c = (c & 255) >> n--;
		while (((d = (unmetaptr > 0 ? unmetabuf[3 - --unmetaptr] : in.read())) & 0xC0) == 0x80)
	        {   c = (c << 6) | (d & 0x3F);
		    n--;
		}
		if (n != 0)
		    System.err.println("\033[01;31mutil-say: warning: UTF-8 decoding balance should be 0 but is: " + n + "\033[00m");
		stored = d;
	    }
	    if (dollar)
		if ((c == '\033') && !escape)
		    escape = true;
		else if ((c == '$') && !escape)
		{   dollar = false;
		    if (dollareql == -1)
		    {
			int[] _name = new int[ptr];
			System.arraycopy(buf, 0, _name, 0, _name.length);
			String name = Common.utf32to16(_name);
			if (name.equals("\\"))
		        {   curwidth++;
			    items.add(new Pony.Cell(this.ignorelink ? ' ' : Pony.Cell.NNW_SSE, null, null, PLAIN));
			}
			else if (name.equals("/"))
		        {   curwidth++;
			    items.add(new Pony.Cell(this.ignorelink ? ' ' : Pony.Cell.NNE_SSW, null, null, PLAIN));
			}
			else if (name.startsWith("balloon") == false)
			    items.add(new Pony.Recall(name, foreground, background, format));
			else if (this.ignoreballoon == false)
			{   String[] parts = (name.substring("balloon".length()) + ",,,,,,-").split(",");
			    Integer h = parts[1].isEmpty() ? null : new Integer(parts[1]);
			    int justify = Pony.Balloon.NONE;
			    if      (parts[0].contains("l"))  justify = Pony.Balloon.LEFT;
			    else if (parts[0].contains("r"))  justify = Pony.Balloon.RIGHT;
			    else if (parts[0].contains("c"))  justify = Pony.Balloon.CENTRE;
			    else
				items.add(new Pony.Balloon(null, null, parts[0].isEmpty() ? null : new Integer(parts[0]), h, null, null, Pony.Balloon.NONE));
			    if (justify != Pony.Balloon.NONE)
			    {
				parts = parts[0].replace('l', ',').replace('r', ',').replace('c', ',').split(",");
				int part0 = Integer.parseInt(parts[0]), part1 = Integer.parseInt(parts[1]);
				items.add(new Pony.Balloon(new Integer(part0), null, new Integer(part1 - part0 + 1), h, null, null, justify));
			}   }
		    }
		    else
		    {   int[] name = new int[dollareql];
			System.arraycopy(buf, 0, name, 0, name.length);
			int[] value = new int[ptr - dollareql - 1];
			System.arraycopy(buf, dollareql + 1, value, 0, value.length);
			items.add(new Pony.Store(Common.utf32to16(name), Common.utf32to16(value)));
		    }
		    ptr = 0;
		    dollareql = -1;
		}
		else
		{   escape = false;
		    if (ptr == buf.length)
			System.arraycopy(buf, 0, buf = new int[ptr << 1], 0, ptr);
		    if ((dollareql == -1) && (c == '='))
			dollareql = ptr;
		    buf[ptr++] = c;
		}
	    else if (escape)
	    {   Object[] back_fore_state = this.submodule.parseEscape(c, background, foreground, format, colours);
		background = (Color)(back_fore_state[0]);
		foreground = (Color)(back_fore_state[1]);
		escape = back_fore_state[2] == Boolean.TRUE;
	    }
	    else if (c == '\033')
		escape = true;
	    else if (c == '$')
		dollar = true;
	    else if (c == '\n')
	    {   if (width < curwidth)
		    width = curwidth;
		curwidth = 0;
		height++;
		items.add(null);
	    }
	    else
	    {	boolean combining = false;
		if ((0x0300 <= c) && (c <= 0x036F))  combining = true;
		if ((0x20D0 <= c) && (c <= 0x20FF))  combining = true;
		if ((0x1DC0 <= c) && (c <= 0x1DFF))  combining = true;
		if ((0xFE20 <= c) && (c <= 0xFE2F))  combining = true;
		if (combining)
		    items.add(new Pony.Combining(c, foreground, background, format));
		else
		{   curwidth++;
		    Color fore = foreground == null ? colours[7] : foreground;
		    if (c == '▀')
			items.add(new Pony.Cell(Pony.Cell.PIXELS, fore, background, format));
		    else if (c == '▄')
			items.add(new Pony.Cell(Pony.Cell.PIXELS, background, fore, format));
		    else if (c == '█')
			items.add(new Pony.Cell(Pony.Cell.PIXELS, fore, fore, format));
		    else if (c == ' ')
			items.add(new Pony.Cell(Pony.Cell.PIXELS, background, background, format));
		    else
			items.add(new Pony.Cell(c, foreground, background, format));
	    }   }
	}
	
	if ((items.size() > 0) && (items.get(items.size() - 1) == null))
	{   items.remove(items.size() - 1);
	    height--;
	}
	
	if (in != System.in)
	    in.close();
	
	
	Pony pony = new Pony(height, width, comment, tags);
	int y = 0, x = 0;
	Pony.Meta[] metabuf = new Pony.Meta[256];
	int metaptr = 0;
	
	for (Object obj : items)
	    if (obj == null)
	    {
		if (metaptr != 0)
		{   Pony.Meta[] metacell = new Pony.Meta[metaptr];
		    System.arraycopy(metabuf, 0, metacell, 0, metaptr);
		    pony.metamatrix[y][x] = metacell;
		    metaptr = 0;
		}
		y++;
		x = 0;
	    }
	    else if (obj instanceof Pony.Cell)
	    {
		if (metaptr != 0)
		{   Pony.Meta[] metacell = new Pony.Meta[metaptr];
		    System.arraycopy(metabuf, 0, metacell, 0, metaptr);
		    pony.metamatrix[y][x] = metacell;
		    metaptr = 0;
		}
		Pony.Cell cell = (Pony.Cell)obj;
		pony.matrix[y][x++] = cell;
	    }
	    else
	    {
		Pony.Meta meta = (Pony.Meta)obj;
		if (metaptr == metabuf.length)
		    System.arraycopy(metabuf, 0, metabuf = new Pony.Meta[metaptr << 1], 0, metaptr);
		metabuf[metaptr++] = meta;
	    }
	if (metaptr != 0)
	{   Pony.Meta[] metacell = new Pony.Meta[metaptr];
	    System.arraycopy(metabuf, 0, metacell, 0, metaptr);
	    pony.metamatrix[y][x] = metacell;
	    metaptr = 0;
	}
	
	if (this.balloon >= 0)
	    Common.insertBalloon(pony, this.balloon);
	
	pony.height = pony.matrix.length;
	pony.width = pony.height == 0 ? 0 : pony.matrix[0].length;
	return pony;
    }
    
    
    /**
     * Import the pony from file using the cowsay format
     * 
     * @return  The pony
     * 
     * @throws  IOException  On I/O error
     */
    protected Pony importCow() throws IOException
    {
	this.version++;
	InputStream stdin = System.in;
	try
	{
	    InputStream in = System.in;
	    if (this.file != null)
		in = new BufferedInputStream(new FileInputStream(this.file));
	    Scanner sc = new Scanner(in, "UTF-8");
	    
	    StringBuilder cow = new StringBuilder();
	    StringBuilder data = new StringBuilder();
	    boolean meta = false;
	    
	    while (sc.hasNextLine())
	    {
		String line = sc.nextLine();
		if (line.replace("\t", "").replace(" ", "").startsWith("#"))
		{
		    if (meta == false)
		    {   meta = true;
			data.append("$$$\n");
		    }
		    line = line.substring(line.indexOf("#") + 1);
		    if (line.equals("$$$"))
			line = "$$$(!)";
		    data.append(line);
		    data.append('\n');
		}
		else
		{
		    line = line.replace("$thoughts", "${thoughts}").replace("${thoughts}", "$\\$");
		    line = line.replace("\\N{U+002580}", "▀");
		    line = line.replace("\\N{U+002584}", "▄");
		    line = line.replace("\\N{U+002588}", "█");
		    line = line.replace("\\N{U+2580}", "▀");
		    line = line.replace("\\N{U+2584}", "▄");
		    line = line.replace("\\N{U+2588}", "█");
		    line = line.replace("\\e", "\033");
		    cow.append(line);
		    cow.append('\n');
		}
	    }
	    if (meta)
		data.append("$$$\n");
	    
	    String pony = cow.toString();
	    pony = pony.substring(pony.indexOf("$the_cow") + 8);
	    pony = pony.substring(pony.indexOf("<<") + 2);
	    String eop = pony.substring(0, pony.indexOf(";"));
	    if (eop.startsWith("<")) /* here document */
		pony = eop.substring(1);
	    else
	    {   pony = pony.substring(pony.indexOf('\n') + 1);
		pony = pony.substring(0, pony.indexOf('\n' + eop + '\n'));
	    }
	    data.append("$balloon" + (pony.indexOf("$\\$") + 2) + "$\n");
	    data.append(pony);
	    
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
	    
	    this.file = null;
	    return this.importPony();
	}
	finally
	{
	    System.setIn(stdin);
	}
    }
    
    
    /**
     * Export a pony to the file
     * 
     * @param  pony  The pony
     * 
     * @throws  IOException  On I/O error
     */
    public void exportPony(Pony pony) throws IOException
    {
	Color[] colours = new Color[256];
	boolean[] format = new boolean[9];
	Color background = null, foreground = null;
	
	for (int i = 0; i < 256; i++)
	{   Colour colour = new Colour(i);
	    colours[i] = new Color(colour.red, colour.green, colour.blue);
	}
	
	String resetPalette = this.submodule.initExport(colours);
	
	
	StringBuilder databuf = new StringBuilder();
	int curleft = 0, curright = 0, curtop = 0, curbottom = 0;
	Pony.Cell[][] matrix = pony.matrix;
	Pony.Meta[][][] metamatrix = pony.metamatrix;
	boolean[][] plains = this.submodule.getPlains();
	boolean[] PLAIN = plains[0];
	boolean[] P1PLAIN = plains[1];
	boolean[] P2PLAIN = plains[2];
	
	
	if ((pony.tags != null) || (pony.comment != null))
	    databuf.append("$$$\n");
	if (pony.tags != null)
	    for (String[] tag : pony.tags)
	    {
		databuf.append(tag[0].toUpperCase());
		databuf.append(": ");
		databuf.append(tag[1]);
		databuf.append("\n");
	    }
	if (pony.comment != null)
	{
	    if ((pony.tags != null) && (pony.tags.length != 0))
		databuf.append('\n');
	    String comment = '\n' + pony.comment.trim() + '\n';
	    while (comment.contains("\n$$$\n"))
		comment = comment.replace("\n$$$\n", "\n$$$(!)\n");
	    comment = comment.substring(1, comment.length() - 1);
	    databuf.append(comment);
	}
	if ((pony.tags != null) || (pony.comment != null))
	    databuf.append("\n$$$\n");
	
	
	if (this.ignoreballoon)
	    for (Pony.Meta[][] row : metamatrix)
		for (Pony.Meta[] cell : row)
		    if (cell != null)
			for (int i = 0, n = cell.length; i < n; i++)
			    if ((cell[i] != null) && (cell[i] instanceof Pony.Balloon))
				row[i] = null;
	
	if (this.ignorelink)
	    for (Pony.Cell[] row : matrix)
		for (int i = 0, n = row.length; i < n; i++)
		{   Pony.Cell cell;
		    if ((cell = row[i]) != null)
			if (this.ignorelink && ((cell.character == Pony.Cell.NNE_SSW) || (cell.character == Pony.Cell.NNW_SSE)))
			    row[i] = new Pony.Cell(' ', null, null, PLAIN);
			else
			{   Color back = ((cell.lowerColour == null) || (cell.lowerColour.getAlpha() < 112)) ? null : cell.lowerColour;
			    Color fore = ((cell.upperColour == null) || (cell.upperColour.getAlpha() < 112)) ? null : cell.upperColour;
			    row[i] = new Pony.Cell(cell.character, back, fore, cell.format); /* the alpha channel does not need to be set to 255 */
			}
	        }
	
	int[] margins = Common.changeMargins(pony, this.left, this.right, this.top, this.bottom);
	matrix = pony.matrix;
	metamatrix = pony.metamatrix;
	this.left = margins[0];
	this.right = margins[1];
	this.top = margins[2];
	this.bottom = margins[3];
	
	/*
	for (int y = 0; y < this.top; y++)
	{   Pony.Meta[][] metarow = metamatrix[y];
	    for (int x = 0, w = metarow.length; x < w; x++)
	    {   Pony.Meta[] metacell = metarow[x];
		for (int z = 0, d = metacell.length; z < d; z++)
		{   Pony.Meta metaelem;
		    if (((metaelem = metacell[z]) != null) && (metaelem instanceof Pony.Store))
			databuf.append("$" + (((Pony.Store)(metaelem)).name + "=" + ((Pony.Store)(metaelem)).value).replace("$", "\033$") + "$");
	}   }   }
	*/
	
	if (this.right != 0)
	{   int w = matrix[0].length, r = metamatrix[0].length - this.right;
	    Pony.Meta[] leftovers = new Pony.Meta[32];
	    for (int y = this.top, h = matrix.length - this.bottom; y < h; y++)
	    {
		int ptr = 0;
		Pony.Meta[][] metarow = metamatrix[y];
		
		for (int x = r; x <= w; x++)
		    if (metarow[x] != null)
			for (Pony.Meta meta : metarow[x])
			    if ((meta != null) && (meta instanceof Pony.Store))
			    {   if (ptr == leftovers.length)
				    System.arraycopy(leftovers, 0, leftovers = new Pony.Meta[ptr << 1], 0, ptr);
				leftovers[ptr++] = meta;
			    }
		
		if (ptr != 0)
		{   Pony.Meta[] metacell = metarow[r];
		    System.arraycopy(metacell, 0, metarow[r] = metacell = new Pony.Meta[metacell.length + ptr], 0, metacell.length - ptr);
		    System.arraycopy(leftovers, 0, metacell, metacell.length - ptr, ptr);
		}
		System.arraycopy(matrix[y], 0, matrix[y] = new Pony.Cell[w - this.right], 0, w - this.right);
		System.arraycopy(metarow, 0, metamatrix[y] = new Pony.Meta[w - this.right + 1][], 0, w - this.right + 1);
	    }
	}
	
	
	if (this.balloon >= 0)
	{   int _h = pony.height;
	    int _w = pony.width;
	    Pony.Cell[][] _m = pony.matrix;
	    Pony.Meta[][][] _mm = pony.metamatrix;
	    pony.height = matrix.length;
	    pony.width = matrix[0].length;
	    pony.matrix = matrix;
	    pony.metamatrix = metamatrix;
	    Common.insertBalloon(pony, this.balloon);
	    matrix = pony.matrix;
	    metamatrix = pony.metamatrix;
	    pony.height = _h;
	    pony.width = _w;
	    pony.matrix = _m;
	    pony.metamatrix = _mm;
	}
	
	
	int[] endings = null;
	if (this.even == false)
	{
	    int w = matrix[0].length;
	    endings = new int[matrix.length];
	    for (int y = 0, h = matrix.length; y < h; y++)
	    {
		Pony.Cell[] row = matrix[y];
		Pony.Meta[][] metarow = metamatrix[y];
		int cur = 0;
		mid:
		    for (int n = w - 1; cur <= n; cur++)
		    {
			boolean cellpass = true;
			Pony.Cell cell = row[n - cur];
			if (cell != null)
			    if ((cell.character != ' ') || (cell.lowerColour != null))
				if ((cell.character != Pony.Cell.PIXELS) || (cell.lowerColour != null) || (cell.upperColour != null))
				    cellpass = false;
			Pony.Meta[] meta = metarow[n - cur];
			if ((meta != null) && (meta.length != 0))
			{   for (int k = 0, l = meta.length; k < l; k++)
				if ((meta[k] != null) && ((meta[k] instanceof Pony.Store) == false))
				    if ((cellpass == false) || (meta[k] instanceof Pony.Balloon))
					break mid;
			}
			else
			    if (cellpass == false)
				break mid;
		    }
		endings[y] = w - cur;
	    }
	}
	
	
	Pony.Cell defaultcell = new Pony.Cell(Pony.Cell.PIXELS, null, null, PLAIN);
	for (int y = this.top, h = matrix.length - this.bottom; y < h; y++)
	{
	    Pony.Cell[] row = matrix[y];
	    Pony.Meta[][] metarow = metamatrix[y];
	    int ending = endings == null ? row.length : endings[y];
	    int balloonend = -1;
	    for (int x = 0, w = row.length; x <= w; x++)
	    {   Pony.Meta[] metacell = metarow[x];
		if (metacell != null)
		    for (int z = 0, d = metacell.length; z < d; z++)
		    {   Pony.Meta meta = metacell[z];
			if ((meta != null) && ((x >= this.left) || (meta instanceof Pony.Store)))
			{   Class<?> metaclass = meta.getClass();
			    if (metaclass == Pony.Store.class)
				databuf.append("$" + (((Pony.Store)meta).name + "=" + ((Pony.Store)meta).value).replace("$", "\033$") + "$");
			    else if (metaclass == Pony.Recall.class)
			    {   Pony.Recall recall = (Pony.Recall)meta;
				Color back = ((recall.backgroundColour == null) || (recall.backgroundColour.getAlpha() < 112)) ? null : recall.backgroundColour;
				Color fore = ((recall.foregroundColour == null) || (recall.foregroundColour.getAlpha() < 112)) ? null : recall.foregroundColour;
				databuf.append(this.submodule.applyColour(colours, background, foreground, format, background = back, foreground = fore, recall.format));
				databuf.append("$" + recall.name.replace("$", "\033$") + "$");
			    }
			    else if (metaclass == Pony.Combining.class)
			    {   Pony.Combining combining = (Pony.Combining)meta;
				databuf.append(this.submodule.applyColour(colours, background, foreground, format, background = combining.backgroundColour, foreground = combining.foregroundColour, combining.format));
				databuf.append(combining.character);
			    }
			    else if (metaclass == Pony.Balloon.class)
			    {   databuf.append(this.submodule.applyColour(colours, background, foreground, format, background = null, foreground = null, PLAIN));
				Pony.Balloon balloon = (Pony.Balloon)meta;
				if (balloon.left != null)
				{   int justification = balloon.minWidth != null ? balloon.justification & (Pony.Balloon.LEFT | Pony.Balloon.RIGHT) : Pony.Balloon.NONE;
				    switch (justification)
				    {	case Pony.Balloon.NONE:
					    char[] spaces = new char[balloon.left.intValue()];
					    Arrays.fill(spaces, ' ');
					    databuf.append(new String(spaces));
					    databuf.append("$balloon" + balloon.left.intValue());
					    break;
					case Pony.Balloon.LEFT:
					    databuf.append("$balloon" + balloon.left.intValue() + "l");
					    databuf.append(balloon.left.intValue() + balloon.minWidth.intValue() - 1);
					    break;
					case Pony.Balloon.RIGHT:
					    databuf.append("$balloon" + balloon.left.intValue() + "r");
					    databuf.append(balloon.left.intValue() + balloon.minWidth.intValue() - 1);
					    break;
					default:
					    databuf.append("$balloon" + balloon.left.intValue() + "c");
					    databuf.append(balloon.left.intValue() + balloon.minWidth.intValue() - 1);
					    break;
				}   }
				else if (balloon.minWidth != null)
				    databuf.append("$balloon" + balloon.minWidth.toString());
				else
				    databuf.append("$balloon");
				// KEYWORD: not supported in ponysay: balloon.top != null
				if (balloon.minHeight != null)
				    databuf.append("," + balloon.minHeight.toString());
				// KEYWORD: not supported in ponysay: balloon.maxWidth != null
				// KEYWORD: not supported in ponysay: balloon.maxHeight != null
				databuf.append("$");
				balloonend = 0;
		    }	}   }
		if ((x != w) && (x >= this.left) && (x < ending))
		{   Pony.Cell cell = row[x];
		    if (cell == null)
			cell = defaultcell;
		    if (cell.character >= 0)
		        if (balloonend < 0)
			{   databuf.append(this.submodule.applyColour(colours, background, foreground, format, background = cell.lowerColour, foreground = cell.upperColour, cell.format));
			    databuf.append(Common.utf32to16(cell.character));
			}
			else if (((cell.character == ' ') || (cell.character == ' ')) && (cell.lowerColour == null))
			    balloonend++;
			else
			{   if (balloonend >= 0)
			    {   databuf.append(this.submodule.applyColour(colours, background, foreground, format, background = null, foreground = this.spacesave ? foreground : null, PLAIN));
				for (int i = 0; i < balloonend; i++)
				    databuf.append(' ');
				balloonend = -1;
			    }
			    databuf.append(this.submodule.applyColour(colours, background, foreground, format, background = cell.lowerColour, foreground = cell.upperColour, cell.format));
			    databuf.append(Common.utf32to16(cell.character));
			}
		    else if (cell.character == Pony.Cell.NNW_SSE)
		    {   if (balloonend >= 0)
			{   databuf.append(this.submodule.applyColour(colours, background, foreground, format, background = null, foreground = null, PLAIN));
			    for (int i = 0; i < balloonend; i++)
				databuf.append(' ');
			    balloonend = -1;
			}
			databuf.append(this.submodule.applyColour(colours, background, foreground, format, background = null, foreground = null, PLAIN));
			databuf.append("$\\$");
		    }
		    else if (cell.character == Pony.Cell.NNE_SSW)
		    {   if (balloonend >= 0)
			{   databuf.append(this.submodule.applyColour(colours, background, foreground, format, background = null, foreground = null, PLAIN));
			    for (int i = 0; i < balloonend; i++)
				databuf.append(' ');
			    balloonend = -1;
			}
			databuf.append(this.submodule.applyColour(colours, background, foreground, format, background = null, foreground = null, PLAIN));
			databuf.append("$/$");
		    }
		    else if (cell.character == Pony.Cell.PIXELS)
			if (cell.lowerColour == null)
			    if (cell.upperColour == null)
			    {   databuf.append(this.submodule.applyColour(colours, background, foreground, format, background = null, foreground = this.spacesave ? foreground : null, PLAIN));
				if (balloonend >= 0)
				    balloonend++;
				else
				    databuf.append(' ');
			    }
			    else
			    {   if (balloonend >= 0)
				{   databuf.append(this.submodule.applyColour(colours, background, foreground, format, background = null, foreground = this.spacesave ? foreground : null, PLAIN));
				    for (int i = 0; i < balloonend; i++)
					databuf.append(' ');
				    balloonend = -1;
				}
				databuf.append(this.submodule.applyColour(colours, background, foreground, format, background = null, foreground = cell.upperColour, P1PLAIN));
				databuf.append('▀');
			    }
			else
			    if (cell.upperColour == null)
			    {   if (balloonend >= 0)
				{   databuf.append(this.submodule.applyColour(colours, background, foreground, format, background = null, foreground = this.spacesave ? foreground : null, PLAIN));
				    for (int i = 0; i < balloonend; i++)
					databuf.append(' ');
				    balloonend = -1;
				}
				databuf.append(this.submodule.applyColour(colours, background, foreground, format, background = null, foreground = cell.lowerColour, P1PLAIN));
				databuf.append('▄');
			    }
			    else if (cell.upperColour.equals(cell.lowerColour))
				if (this.zebra)
				{   if (balloonend >= 0)
				    {   databuf.append(this.submodule.applyColour(colours, background, foreground, format, background = null, foreground = this.spacesave ? foreground : null, PLAIN));
					for (int i = 0; i < balloonend; i++)
					    databuf.append(' ');
					balloonend = -1;
				    }
				    databuf.append(this.submodule.applyColour(colours, background, foreground, format, background = cell.lowerColour, foreground = cell.lowerColour, P2PLAIN));
				    databuf.append('▄');
				}
				else if (this.fullblocks)
				{   if (balloonend >= 0)
				    {   databuf.append(this.submodule.applyColour(colours, background, foreground, format, background = null, foreground = this.spacesave ? foreground : null, PLAIN));
					for (int i = 0; i < balloonend; i++)
					    databuf.append(' ');
					balloonend = -1;
				    }
				    databuf.append(this.submodule.applyColour(colours, background, foreground, format, background = this.spacesave ? background : cell.lowerColour, foreground = cell.lowerColour, P1PLAIN));
				    databuf.append('█');
				}
				else
				{   if (balloonend >= 0)
				    {   databuf.append(this.submodule.applyColour(colours, background, foreground, format, background = null, foreground = this.spacesave ? foreground : null, PLAIN));
					for (int i = 0; i < balloonend; i++)
					    databuf.append(' ');
					balloonend = -1;
				    }
				    databuf.append(this.submodule.applyColour(colours, background, foreground, format, background = cell.lowerColour, foreground = this.spacesave ? foreground : cell.lowerColour, PLAIN));
				    databuf.append(' ');
				}
			    else
			    {	if (balloonend >= 0)
				{   databuf.append(this.submodule.applyColour(colours, background, foreground, format, background = null, foreground = this.spacesave ? foreground : null, PLAIN));
				    for (int i = 0; i < balloonend; i++)
					databuf.append(' ');
				    balloonend = -1;
				}
				databuf.append(this.submodule.applyColour(colours, background, foreground, format, background = cell.upperColour, foreground = cell.lowerColour, P2PLAIN));
				databuf.append('▄');  //TODO (this.colourful && ¿can get better colour?) → flip
			    }
		}
	    }
	    databuf.append(this.submodule.applyColour(colours, background, foreground, format, background = null, foreground = null, format));
	    background = foreground = null;
	    format = new boolean[9];
	    databuf.append("\033[00m\n");
	}
	
	
	// for (int y = metamatrix.length - this.bottom, b = metamatrix.length; y < b; y++)
	// {   Pony.Meta[][] metarow = metamatrix[y];
	//     for (int x = 0, w = metarow.length; x < w; x++)
	//     {   Pony.Meta[] metacell = metarow[x];
	//         for (int z = 0, d = metacell.length; z < d; z++)
	//         {   Pony.Meta metaelem;
	//             if (((metaelem = metacell[z]) != null) && (metaelem instanceof Pony.Store))
	//                 databuf.append("$" + (((Pony.Store)(metaelem)).name + "=" + ((Pony.Store)(metaelem)).value).replace("$", "\033$") + "$");
	// }   }   }
	
	
	String data = databuf.toString();
	
	if (this.version == VERSION_COWSAY)
	{
	    String metadata = null;
	    if (data.startsWith("$$$\n"))
	    {
		metadata = data.substring(4);
		if (metadata.startsWith("$$$\n"))
		    metadata = null;
		else
		{   metadata = metadata.substring(0, metadata.indexOf("\n$$$\n") + 5);
		    data = data.substring(data.indexOf("\n$$$\n") + 5);
		    metadata = '#' + metadata.replace("\n", "\n#");
		}
	    }
	    String eop = "\nEOP";
	    while (data.contains(eop + '\n'))
		eop += 'P';
	    data = data.replace("$/$", "/").replace("$\\$", "${thoughts}");
	    while (data.contains("$balloon"))
	    {
		int start = data.indexOf("$balloon");
		int end = data.indexOf("$", start + 8);
		data = data.substring(0, start) + data.substring(end + 1);
	    }
	    data = "$the_cow = <<" + eop + ";\n" + data;
	    data += eop + '\n';
	    if (metadata != null)
		data = metadata + data;
	    if (this.utf8 == false)
		data = data.replace("▀", "\\N{U+2580}").replace("▄", "\\N{U+2584}");
	}
	else
	{   if (this.version < VERSION_METADATA)
	    {
		if (data.startsWith("$$$\n"))
		    data = data.substring(data.indexOf("\n$$$\n") + 5);
	    }
	    if (this.version < VERSION_HORIZONTAL_JUSTIFICATION)
	    {
		databuf = new StringBuilder();
		int pos = data.indexOf("\n$$$\n");
		pos += pos < 0 ? 1 : 5;
		databuf.append(data.substring(0, pos));
		StringBuilder dollarbuf = null;
		boolean esc = false;
		for (int i = 0, n = data.length(); i < n;)
		{
		    char c = data.charAt(i++);
		    if (dollarbuf != null)
		    {
			dollarbuf.append(c);
			if (esc || (c == '\033'))
			    esc ^= true;
			else if (c == '$')
			{
			    String dollar = dollarbuf.toString();
			    dollarbuf = null;
			    if (dollar.startsWith("$balloon") == false)
				databuf.append(dollar);
			    else
			    {   databuf.append("$balloon");
				dollar = dollar.substring(8);
				if      (dollar.contains("l"))  dollar = dollar.substring(dollar.indexOf('l') + 1);
				else if (dollar.contains("r"))  dollar = dollar.substring(dollar.indexOf('r') + 1);
				else if (dollar.contains("c"))  dollar = dollar.substring(dollar.indexOf('c') + 1);
				databuf.append(dollar);
			}   }
		    }
		    else if (c == '$')
			dollarbuf = new StringBuilder("$");
		    else
			databuf.append(c);
		}
		data = databuf.toString();
	    }
	}
	
	if (resetPalette != null)
	    data += resetPalette;
	if (this.escesc)
	    data = data.replace("\033", "\\e");
	
	OutputStream out = System.out;
	if (this.file != null)
	    out = new FileOutputStream(this.file); /* buffering is not needed, everything is written at once */
	out.write(data.getBytes("UTF-8"));
	out.flush();
	if (out != System.out)
	    out.close();
    }
    
    
    /**
     * Determine pony file format version for ponysay version string
     * 
     * @param   value  The ponysay version
     * @return         The pony file format version
     */
    private static int parseVersion(String value)
    {
	String[] strdots = value.split(".");
	int[] dots = new int[strdots.length < 10 ? 10 : strdots.length];
	for (int i = 0, n = strdots.length; i < n; i++)
	    dots[i] = Integer.parseInt(strdots[i]);
	
	if (dots[0] < 2)       return VERSION_COWSAY;
	if (dots[0] == 2)
	{   if (dots[1] == 0)  return VERSION_COWSAY;
	    if (dots[1] <= 8)  return VERSION_UNISAY;
	                       return VERSION_METADATA;
	}
	/* version 3.0 */      return VERSION_HORIZONTAL_JUSTIFICATION;
    }
    
}

