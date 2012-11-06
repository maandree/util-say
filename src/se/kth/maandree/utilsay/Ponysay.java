/**
 * util-say — Utilities for cowsay and cowsay-like programs
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
import java.util.LinkedList;
import java.util.HashMap;
import java.awt.Color;


// This class should be tried to be keeped as optimised as possible for the
// lastest version of ponysay (only) without endangering its maintainability.


/**
 * Ponysay support module
 * 
 * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
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
	this.file = (this.file = flags.contains("file") ? flags.get("file") : null).equals("-") ? null : this.file;
	this.even = (flags.contains("even") == false) || flags.get("even").toLowerCase().startswith("y");
	this.tty = flags.contains("tty") && flags.get("tty").toLowerCase().startswith("y");
	this.fullblocks = flags.contains("fullblocks") ? flags.get("fullblocks").toLowerCase().startswith("y") : this.tty;
	this.spacesave = flags.contains("spacesave") && flags.get("spacesave").toLowerCase().startswith("y");
	this.zebra = flags.contains("zebra") && flags.get("zebra").toLowerCase().startswith("y");
	this.version = flags.contains("version") ? parseVersion(flags.get("version")) : VERSION_HORIZONTAL_JUSTIFICATION;
	this.utf8 = this.version > VERSION_COWSAY ? true : (flags.contains("utf8") && flags.get("utf8").toLowerCase().startswith("y"));
	this.fullcolour = flags.contains("fullcolour") && flags.get("fullcolour").toLowerCase().startswith("y");
	this.chroma = (flags.contains("chroma") == false) ? -1 : parseDouble(flags.get("chroma"));
	this.left = (flags.contains("left") == false) ? 2 : parseInteger(flags.get("left"));
	this.right = (flags.contains("right") == false) ? 0 : parseInteger(flags.get("right"));
	this.top = (flags.contains("top") == false) ? 3 : parseInteger(flags.get("top"));
	this.bottom = (flags.contains("bottom") == false) ? 1 : parseInteger(flags.get("bottom"));
	this.palette = (flags.contains("palette") == false) ? null : parsePalette(flags.get("palette").toUpperCase().replace("\033", "").replace("]", "").replace("P", ""));
	this.ignoreballoon = flags.contains("ignoreballoon") && flags.get("ignoreballoon").toLowerCase().startswith("y");
	this.ignorelink = flags.contains("ignorelink") ? flags.get("ignorelink").toLowerCase().startswith("y") : this.ignoreballoon;
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
     * Output option: linux vt
     */
    protected boolean tty;
    
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
     * Input/output option: colour palette
     */
    protected Color[] palette;
    
    /**
     * Output option: chroma weight, negative for sRGB distance
     */
    protected double chroma;
    
    /**
     * Input/output option: ponysay version
     */
    protected int version;
    
    /**
     * Output option: use utf8 encoding on pixels
     */
    protected boolean utf8;
    
    /**
     * Output option: do not limit to xterm 256 standard colours
     */
    protected boolean fullcolour;
    
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
     * Import the pony from file
     * 
     * @return  The pony
     */
    public Pony importPony()
    {
	if (this.version == VERSION_COWSAY)
	    return this.importCow();
	
	Color[] colours = new Color[256];
	boolean[] format = new boolean[9];
	Color background = null, foreground = null;
	
	for (int i = 0; i < 256; i++)
	{   Colour colour = new Colour(i);
	    colours[i] = new Color(colour.red, colour.green, colour.blue);
	}
	if (this.palette != null)
	    System.arraycopy(this.palette, 0, colours, 0, 16);
	
	InputStream in = System.in;
	if (this.file != null)
	    in = BufferedInputStream(new FileInputStream(this.file));
	
	boolean dollar = false;
	boolean escape = false;
	boolean csi = false;
	boolean osi = false;
	
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
		if (ptr == data.lenght)
		    System.arraycopy(data, 0, data = new byte[ptr << 1], 0, ptr);
		data[ptr++] = d;
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
		    {
			istag = false;
			name = line.substring(0, colon);
			value = line.substring(colon + 1);
			char c;
			for (int i = 0, n = name.length(); i < n; i++)
			    if ((c = name.charAt(i)) != ' ')
				if (('A' > c) || (c > 'Z'))
				    break block;
			istag = true;
		    }}
		if (istag)
		{
		    if (tags == null)
			tags = new String[32][];
		    else if (tagptr == tags.length)
			Systm.arraycopy(tags, 0, tags = new String[tagptr << 1], 0, tagptr);
		    tags[tagptr++] = new String[] {name.trim(), value.trim()};
		}
		else
		{
		    commentbuf.append(line);
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
		Systm.arraycopy(tags, 0, tags = new String[tagptr], 0, tagptr);
	}
	
	boolean[] plain = new boolean[9];
	
	for (int d = 0, stored = -1, c;;)
	{
	    if (unmetaptr > 0)
	    {   d = unmetabuf[3 - --unmetaptr];
		if (d == -1)
		    break;
	    }
	    else if ((d = stored) != -1)
		stored = -1;
	    else if ((d == in.read()) == -1)
		break;
	    
	    if (((c = d) & 0x80) == 0x80)
	    {   int n = 0;
		while ((c & 0x80) == 0x80)
		{   c <<= 1;
		    n++;
		}
		c = (c & 255) >> n;
		while (((d = in.read()) & 0xC0) == 0x80)
		    c = (c << 6) | (d & 0x3F);
		stored = d;
	    }
	    
	    if (dollar)
		if ((d == '\033') && !escape)
		    escape = true;
		else if ((d == '$') && !escape)
		{   dollar = false;
		    if (dollareql == -1)
		    {
			int[] _name = new int[ptr];
			System.arraycopy(buf, 0, _name, 0, _name.length);
			String name = utf32to16(_name);
			if (name.equals("\\"))
		        {   curwidth++;
			    items.add(new Pony.Cell(this.ignorelink ? ' ' : Pony.Cell.NNE_SSW, null, null, plain));
			}
			else if (name.equals("/"))
		        {   curwidth++;
			    items.add(new Pony.Cell(this.ignorelink ? ' ' : Pony.Cell.NNW_SSE, null, null, plain));
			}
			else if (name.startsWith("balloon") == false)
			    items.add(new Pony.Recall(name, foreground, background, format));
			else if (this.ignoreballoon == false)
			{   String[] parts = (name.substring("balloon".length()) + ",,,,,").split(",");
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
			items.add(new Pony.Recall(utf32to16(name), utf32to16(value)));
		    }
		    ptr = 0;
		    dollareql = -1;
		}
		else
		{   escape = false;
		    if (ptr == buf.length)
			System.arraycopy(buf, 0, buf = new int[ptr << 1], 0, buf);
		    if ((dollareql == -1) && (d == '='))
			dollareql = ptr;
		    buf[ptr++] = d;
		}
	    else if (escape)
		if (osi)
		    if (ptr > 0)
		    {   buf[ptr++ -1] = d;
			if (ptr == 8)
			{   ptr = 0;
			    osi = escape = false;
			    int index =             (ptr[0] < 'A') ? (ptr[0] & 15) : ((ptr[0] ^ '@') + 9);
			    int red =               (ptr[1] < 'A') ? (ptr[1] & 15) : ((ptr[1] ^ '@') + 9);
			    red = (red << 4) |     ((ptr[2] < 'A') ? (ptr[2] & 15) : ((ptr[2] ^ '@') + 9));
			    int green =             (ptr[3] < 'A') ? (ptr[3] & 15) : ((ptr[3] ^ '@') + 9);
			    green = (green << 4) | ((ptr[4] < 'A') ? (ptr[4] & 15) : ((ptr[4] ^ '@') + 9));
			    int blue =              (ptr[5] < 'A') ? (ptr[5] & 15) : ((ptr[5] ^ '@') + 9);
			    blue = (blue << 4) |   ((ptr[6] < 'A') ? (ptr[6] & 15) : ((ptr[6] ^ '@') + 9));
			    colours[index] = new Color(red, green, blue);
			}
		    }
		    else if (ptr < 0)
		    {   if (~ptr == buf.length)
			    System.arraycopy(buf, 0, buf = new int[~ptr << 1], 0, ~ptr);
			if (d == '\\')
			{   ptr = ~ptr;
			    ptr--;
			    if ((ptr > 8) && (buf[ptr] == '\e') && (buf[0] == ';'))
			    {   int[] _code = new int[ptr - 1];
				System.arraycopy(buf, 1, _code, 0, ptr - 1);
				String[] code = utf32to16(_code).split(";");
				if (code.length == 2)
				{   int index = Integer.parseInt(code[0]);
				    code = code[1].split("/");
				    if ((code.length == 3) && (code[0].startsWith("rgb:")))
				    {   code[0] = code[0].substring(4);
					int red   = Integer.parseInt(code[0], 16);
					int green = Integer.parseInt(code[1], 16);
					int blue  = Integer.parseInt(code[2], 16);
					colours[index] = new Color(red, green, blue);
			    }   }   }
			    ptr = 0;
			    osi = escape = false;
			}
			else
			{   buf[~ptr] = d;
			    ptr--;
			}
		    }
		    else if (d == 'P')  ptr = 1;
		    else if (d == '4')  ptr = ~0;
		    else
		    {   osi = escape = false;
			items.add(new Pony.Cell('\033', foreground, background, format));
			items.add(new Pony.Cell(']', foreground, background, format));
			items.add(new Pony.Cell(d, foreground, background, format));
		    }
		else if (csi)
		{   if (ptr == buf.length)
			System.arraycopy(buf, 0, buf = new int[ptr << 1], 0, ptr);
		    buf[ptr++] = d;
		    if ((('a' <= d) && (d <= 'z')) || (('A' <= d) && (d <= 'Z')) || (d == '~'))
		    {   csi = escape = false;
			ptr--;
			if (d == 'm')
			{   int[] _code = new int[ptr];
			    System.arraycopy(buf, 0, _code, 0, ptr);
			    String[] code = utf32to16(_code).split(";");
			    int xterm256 = 0;
			    boolean back = false;
			    for (String seg : code)
			    {   int value = Integer.parseInt(seg);
				if (xterm256 == 2)
				{   xterm256 = 0;
				    if (back)  background = colours[value];
				    else       foreground = colours[value];
				}
				else if (value == 0)
				{   for (int i = 0; i < 9; i++)
					format[i] = false;
				    background = foreground = null;
				}
				else if (xterm256 == 1)
				    xterm256 = value == 5 ? 2 : 0;
				else if (value < 10)
				    format[value - 1] = true;
				else if ((20 < value) && (value < 30))
				    format[value - 21] = false;
				else if (value == 39)   foreground = null;
				else if (value == 49)   background = null;
				else if (value < 38)    foreground = colours[value - 30];
				else if (value < 48)    background = colours[value - 40];
				else if (value == 38)   xterm256 = 1;
				else if (value == 48)   xterm256 = 1;
				if (xterm256 == 1)
				    back = value == 48;
			    }
			}
			ptr = 0;
		    }
		}
		else if (d == '[')
		{   csi = true;
		    ptr = 0;
		}
		else if (d == ']')
		    osi = true;
		else
		{   escape = false;
		    items.add(new Pony.Cell('\033', foreground, background, format));
		    items.add(new Pony.Cell(d, foreground, background, format));
		    curwidth += 2;
		}
	    else if (d == '\033')
		escape = true;
	    else if (d == '$')
		dollar = true;
	    else if (d == '\n')
	    {   if (width < curwidth)
		    width = curwidth;
		curwidth = 0;
		height = 0;
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
			items.add(new Pony.Cell(Pony.Cell.PIXELS, fore : foreground, background, format));
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
		    pony.matrix[y][x] = metacell;
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
		    pony.matrix[y][x] = metacell;
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
	    pony.matrix[y][x] = metacell;
	    metaptr = 0;
	}
	
	return pony;
    }
    
    
    /**
     * Import the pony from file using the cowsay format
     * 
     * @return  The pony
     */
    protected Pony importCow()
    {
	this.version++;
	InputStream stdin = System.in;
	try
	{
	    InputStream in = System.in;
	    if (this.file != null)
		in = BufferedInputStream(new FileInputStream(this.file));
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
		    data.append(line)
		    data.append('\n')
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
	    
	    final byte[] streamdata = data.toString().getBytes('UTF-8');
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
     */
    public void exportPony(Pony pony)
    {
	Color[] colours = new Color[256];
	boolean[] format = new boolean[9];
	Color background = null, foreground = null;
	
	for (int i = 0; i < 256; i++)
	{   Colour colour = new Colour(i);
	    colours[i] = new Color(colour.red, colour.green, colour.blue);
	}
	if (this.palette != null)
	    System.arraycopy(this.palette, 0, colours, 0, 16);
	
	
	StringBuilder databuf = new StringBuilder();
	int curleft = 0, curright = 0, curtop = 0, curbottom = 0;
	Pony.Cell[][] matrix = pony.matrix;
	Pony.Meta[][][] metamatrix = pony.metamatrix;
	
	
	if (this.ignoreballoon)
	    for (Pony.Meta[][] row : metamatrix)
		for (Pony.Meta[] cell : row)
		    if (cell != null)
			for (int i = 0, n = cell.length; i < n; i++)
			    if ((cell[i] != null) && (cell[i] instanceof Pony.Balloon))
				row[i] = null;
	
	if (this.ignorelink)
        {   boolean[] plain = new boolean[9];
	    for (Pony.Cell[] row : matrix)
		for (int i = 0, n = row.length; i < n; i++)
		{   Pony.Cell cell;
		    if ((cell = row[i]) != null)
			if ((cell.character == Pony.Cell.NNE_SSW) || (cell.character == Pony.Cell.NNW_SSE))
			    row[i] = new Pony.Cell(' ', null, null, plain);
	}       }
	
	
	if (this.left >= 0)
	{
	    int cur = 0;
	    outer:
	        for (int n = matrix[0].length; cur < n; cur++)
		    for (int j = 0, m = matrix.length; j < m; j++)
		    {
			boolean cellpass = true;
			Pony.Cell cell = matrix[j][cur];
			if (cell != null)
			    if ((cell.character != ' ') || (cell.background != null))
				if ((cell.character != Pony.Cell.PIXELS) || (cell.background != null) || (cell.foreground != null))
				    cellpass = false;
			if (cellpass == false)
			{   Pony.Meta[] meta = metamatrix[j][cur];
			    if ((meta != null) && (meta.length != 0))
			    {	for (int k = 0, l = meta.length; k < l; k++)
				    if ((meta[k] != null) && ((meta[k] instanceof Pony.Store) == false))
					break outer;
			    }
			    else
				break outer;
			}
		    }
	    this.left -= cur;
	}
	else this.left = 0;
	if (this.right >= 0)
	{
	    int cur = 0;
	    outer:
	        for (int n = matrix[0].length - 1; cur <= n; cur++)
		    for (int j = 0, m = matrix.length; j < m; j++)
		    {
			boolean cellpass = true;
			Pony.Cell cell = matrix[j][n - cur];
			if (cell != null)
			    if ((cell.character != ' ') || (cell.background != null))
				if ((cell.character != Pony.Cell.PIXELS) || (cell.background != null) || (cell.foreground != null))
				    cellpass = false;
			if (cellpass == false)
			{   Pony.Meta[] meta = metamatrix[j][n - cur];
			    if ((meta != null) && (meta.length != 0))
			    {	for (int k = 0, l = meta.length; k < l; k++)
				    if ((meta[k] != null) && ((meta[k] instanceof Pony.Store) == false))
					break outer;
			    }
			    else
				break outer;
			}
		    }
	    this.right -= cur;
	}
	else
	    this.right = 0;
	if (this.top >= 0)
	{
	    int cur = 0, m = matrix[0].length - this.right;
	    outer:
	        for (int n = matrix.length; cur < n; cur++)
		{   Pony.Cell[] row = matrix[cur];
		    Pony.Meta[][] metarow = metamatrix[cur];
		    for (int j = this.left; j < m; j++)
		    {
			boolean cellpass = true;
			Pony.Cell cell = row[j];
			if (cell != null)
			    if ((cell.character != ' ') || (cell.background != null))
				if ((cell.character != Pony.Cell.PIXELS) || (cell.background != null) || (cell.foreground != null))
				    cellpass = false;
			if (cellpass == false)
			{   Pony.Meta[] meta = metarow[j];
			    if ((meta != null) && (meta.length != 0))
			    {	for (int k = 0, l = meta.length; k < l; k++)
				    if ((meta[k] != null) && ((meta[k] instanceof Pony.Store) == false))
					break outer;
			    }
			    else
				break outer;
			}
		}   }
	    this.top -= cur;
	}
	else
	    this.top = 0;
	if (this.bottom >= 0)
	{
	    int cur = 0, m = matrix[0].length - this.right;
	    outer:
	        for (int n = matrix.length - 1 - this.top; cur <= n; cur++)
		{   Pony.Cell[] row = matrix[n - cur];
		    Pony.Meta[][] metarow = metamatrix[n - cur];
		    for (int j = this.left; j < m; j++)
		    {
			boolean cellpass = true;
			Pony.Cell cell = row[j];
			if (cell != null)
			    if ((cell.character != ' ') || (cell.background != null))
				if ((cell.character != Pony.Cell.PIXELS) || (cell.background != null) || (cell.foreground != null))
				    cellpass = false;
			if (cellpass == false)
			{   Pony.Meta[] meta = metarow[j];
			    if ((meta != null) && (meta.length != 0))
			    {	for (int k = 0, l = meta.length; k < l; k++)
				    if ((meta[k] != null) && ((meta[k] instanceof Pony.Store) == false))
					break outer;
			    }
			    else
				break outer;
			}
		}   }
	    this.bottom -= cur;
	}
	else
	    this.bottom = 0;
	
	
	if (this.left > 0)
	{
	    // TODO implement
	    this.left = 0;
	}
	else
	    this.left = -this.left;
	
	if (this.right > 0)
	{
	    // TODO implement
	    this.right = 0;
	}
	else
	    this.right = -this.right;
	
	if (this.top > 0)
	{
	    int h = matrix.length;
	    Pony.Cell[][] appendix = new Pony.Cell[this.top][matrix[0].length];
	    System.arraycopy(matrix, this.top, matrix = new Pony.Cell[][], 0, h);
	    System.arraycopy(matrix, 0, appendix, 0, this.top);
	    Pony.Meta[][][] metaappendix = new Pony.Cell[this.top][metamatrix[0].length][];
	    System.arraycopy(metamatrix, this.top, metamatrix = new Pony.Cell[][][], 0, h);
	    System.arraycopy(metamatrix, 0, metaappendix, 0, this.top);
	    this.top = 0;
	}
	else
	    this.top = -this.top;
	
	if (this.bottom > 0)
	{
	    int h = matrix.length;
	    Pony.Cell[][] appendix = new Pony.Cell[this.bottom][matrix[0].length];
	    System.arraycopy(matrix, 0, matrix = new Pony.Cell[][], 0, h);
	    System.arraycopy(matrix, h, appendix, 0, this.bottom);
	    Pony.Meta[][][] metaappendix = new Pony.Cell[this.bottom][metamatrix[0].length][];
	    System.arraycopy(metamatrix, 0, metamatrix = new Pony.Cell[][][], 0, h);
	    System.arraycopy(metamatrix, h, metaappendix, 0, this.bottom);
	    this.bottom = 0;
	}
	else
	    this.bottom = -this.bottom;
	
	
	for (int y = 0; y < this.top; y++)
	{   Pony.Meta[][] metarow = metamatrix[y];
	    for (int x = 0, w = metarow.length; x < w; x++)
	    {   Pony.Meta[] metacell = metarow[x];
		for (int z = 0, d = metacell.length; z < d; z++)
		{   Pony.Meta metaelem;
		    if (((metaelem = metacell[z]) != null) && (metaelem instanceof Pony.Store))
			databuf.append("$" + (((Pony.Store)(metaelem)).name + "=" + ((Pony.Store)(metaelem)).value).replace("$", "\033$") + "$");
	}   }   }
	
	
	if (this.right != 0)
	{   int w = matrix[0].length, r = metamatrix[0].length - this.right;
	    Pony.Meta[] leftovers = Pony.Meta[32];
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
	
	
	// TODO implement
	if (this.even == false)
	{
	}
	
	
	// TODO implement
	// boolean this.fullblocks;
	// boolean this.spacesave;
	// boolean this.zebra;
	// boolean this.tty;
	// double this.chroma;
	// boolean this.fullcolour;
	for (int y = this.top, h = matrix.length - this.bottom; y < h; y++)
	{
	    Pony.Cell[] row = matrix[y];
	    Pony.Meta[][] metarow = metamatrix[y];
	    for (int x = 0, w = row.length; x <= w; x++)
	    {   Pony.Meta[] metacell = metarow[row.length];
		if (metacell != null)
		    for (int z = 0, d = metacell.length; z < d; z++)
		    {   Pony.Meta meta = metacell[z];
			if ((meta != null) && ((x >= this.left) || (meta instanceof Pony.Store)))
			    ;
		    }
		if ((x != w) && (x >= this.left))
	        {   Pony.Cell cell = row[x];
		    ;
		}
	    }
	    databuf.append('\n');
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
		String metadata = data.substring(4);
		if (metadata.startsWith("$$$\n"))
		    metadata = null;
		else
		    metadata = metadata.substring(0, metadata.indexOf("\n$$$\n"));
		data = data.substring(data.indexOf("\n$$$\n") + 5);
		metadata = '#' + metadata.replace("\n", "\n#");
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
		for (int n = data.length; i < n;)
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
				data.append(dollar);
			    else
			    {   data.append("$balloon");
				dollar = dollar.substring(8);
				if      (dollar.contains("l"))  dollar = dollar,substring(dollar.indexOf('l') + 1);
				else if (dollar.contains("r"))  dollar = dollar,substring(dollar.indexOf('r') + 1);
				else if (dollar.contains("c"))  dollar = dollar,substring(dollar.indexOf('c') + 1);
				data.append(dollar);
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
	
	
	OutputStream out = System.out;
	if (this.file != null)
	    out = new FileOutputStream(this.file); /* buffering is not needed, everything is written at once */
	out.write(data.getBytes("UTF-8");
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
    
    /**
     * Parse double value
     * 
     * @param   value  String representation
     * @return         Raw representation, -1 if not a number
     */
    protected static double parseDouble(String value)
    {
	try
	{   return Double.parseDouble(value);
	}
	catch (Throwable err)
	{   return -1.0;
	}
    }
    
    /**
     * Parse integer value
     * 
     * @param   value  String representation
     * @return         Raw representation, -1 if not an integer
     */
    protected static int parseInteger(String value)
    {
	try
	{   return Integer.parseInt(value);
	}
	catch (Throwable err)
	{   return -1;
	}
    }
    
    /**
     * Parse palette
     * 
     * @param   value  String representation, without ESC, ] or P
     * @return         Raw representation
     */
    protected static Color[] parsePalette(String value)
    {
	String defvalue = "00000001AA0000200AA003AA550040000AA5AA00AA600AAAA7AAAAAA"
	                + "85555559FF5555A55FF55BFFFF55C5555FFDFF55FFE55FFFFFFFFFFF";
	Color[] palette = new Color[16];
	while (int ptr = 0, n = defvalue.length(); ptr < n; ptr += 7)
	{
	    int index = Integer.parseInt(defvalue.substring(ptr + 0, 1), 16);
	    int red   = Integer.parseInt(defvalue.substring(ptr + 1, 2), 16);
	    int green = Integer.parseInt(defvalue.substring(ptr + 3, 2), 16);
	    int blue  = Integer.parseInt(defvalue.substring(ptr + 5, 2), 16);
	    palette[index] = new Color(red, green, blue);
	}
	while (int ptr = 0, n = value.length(); ptr < n; ptr += 7)
	{
	    int index = Integer.parseInt(value.substring(ptr + 0, 1), 16);
	    int red   = Integer.parseInt(value.substring(ptr + 1, 2), 16);
	    int green = Integer.parseInt(value.substring(ptr + 3, 2), 16);
	    int blue  = Integer.parseInt(value.substring(ptr + 5, 2), 16);
	    palette[index] = new Color(red, green, blue);
	}
	return palette;
    }
    
    
    /**
     * Converts an integer array to a string with only 16-bit charaters
     * 
     * @param   ints  The int array
     * @return        The string
     */
    public static String utf32to16(final int... ints)
    {
	int len = ints.length;
	for (final int i : ints)
	    if (i > 0xFFFF)
		len++;
	    else if (i > 0x10FFFF)
		throw new RuntimeException("Be serious, there is no character above plane 16.");
	
	final char[] chars = new char[len];
	int ptr = 0;
	
	for (final int i : ints)
	    if (i <= 0xFFFF)
		chars[ptr++] = (char)i;
	    else
	    {
		/* 10000₁₆ + (H − D800₁₆) ⋅ 400₁₆ + (L − DC00₁₆) */
		
		int c = i - 0x10000;
		int L = (c & 0x3FF) + 0xDC00;
		int H = (c >>> 10) + 0xD800;
		
		chars[ptr++] = (char)H;
		chars[ptr++] = (char)L;
	    }
	
	return new String(chars);
    }
    
}

