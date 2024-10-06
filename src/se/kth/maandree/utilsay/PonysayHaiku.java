/**
 * util-say — Utilities for cowsay and cowsay-like programs
 *
 * Copyright © 2012, 2013  Mattias Andrée (m@maandree.se)
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

import java.awt.*;
import java.util.*;


/**
 * Haiku <tt>xterm</tt> submodule for {@link Ponysay}
 * 
 * @author  Mattias Andrée, <a href="mailto:m@maandree.se">m@maandree.se</a>
 */
public class PonysayHaiku extends PonysaySubmodule
{
    /**
     * Constructor
     * 
     * @param  flags  Flags passed to the module
     */
    public PonysayHaiku(HashMap<String, String> flags)
    {
	this.chroma = (flags.containsKey("chroma") == false) ? 1 : Common.parseDouble(flags.get("chroma"));
	this.colourful = (flags.containsKey("colourful") == false) || flags.get("colourful").toLowerCase().startsWith("y");
	this.fullcolour = flags.containsKey("fullcolour") && flags.get("fullcolour").toLowerCase().startsWith("y");
	this.palette = (flags.containsKey("palette") == false) ? null : PonysayXterm.parsePalette(flags.get("palette"));
    }
    
    
    
    /**
     * Output option: chroma weight, negative for sRGB distance
     */
    protected double chroma;
    
    /**
     * Output option: colourful TTY
     */
    protected boolean colourful;
    
    /**
     * Output option: do not limit to xterm 256 standard colours
     */
    protected boolean fullcolour;
    
    /**
     * Input/output option: colour palette
     */
    protected Color[] palette;
    
    /**
     * Auxiliary: whether the parsing is currectly in a CSI sequence
     */
    private boolean csi = false;
    
    /**
     * Auxiliary: whether the parsing is currectly in a OSI sequence
     */
    private boolean osi = false;
    
    /**
     * Auxiliary: parsing buffer
     */
    private int[] buf = new int[256];
    
    /**
     * Auxiliary: parsing buffer pointer
     */
    private int ptr = 0;
    
    /**
     * Auxiliary: parsing return array
     */
    private Object[] rcbuf = new Object[3];
    
    
    
    /**
     * {@inheritDoc}
     */
    public boolean[][] getPlains()
    {
	boolean bold = this.fullcolour;
	return new boolean[][] {
	    {false, false, false, false, false, false, false, false, false},
	    {bold, false, false, false, false, false, false, false, false, false},
	    {bold, false, false, false, false, false, false, false, false}};
    }
    
    
    /**
     * {@inheritDoc}
     */
    public void initImport(Color[] colours)
    {
	if (this.palette != null)
	    System.arraycopy(this.palette, 0, colours, 0, 16);
	else
	    this.palette = parsePalette("");
    }
    
    
    /**
     * {@inheritDoc}
     */
    public String initExport(Color[] colours)
    {
	if (this.palette != null)
	    System.arraycopy(this.palette, 0, colours, 0, 16);
	else
	    this.palette = parsePalette("");
	
	StringBuilder resetPalette = null;
	if (this.fullcolour)
	{   resetPalette = new StringBuilder();
	    if (this.colourful)
		for (int i = 0; i < 16; i++)
		{   Colour colour = new Colour(i);
		    resetPalette.append("\033]4;");
		    resetPalette.append(i);
		    resetPalette.append(";rgb:");
		    resetPalette.append("0123456789ABCDEF".charAt(colour.red >>> 4));
		    resetPalette.append("0123456789ABCDEF".charAt(colour.red & 15));
		    resetPalette.append('/');
		    resetPalette.append("0123456789ABCDEF".charAt(colour.green >>> 4));
		    resetPalette.append("0123456789ABCDEF".charAt(colour.green & 15));
		    resetPalette.append('/');
		    resetPalette.append("0123456789ABCDEF".charAt(colour.blue >>> 4));
		    resetPalette.append("0123456789ABCDEF".charAt(colour.blue & 15));
		    resetPalette.append("\033\\");
		}
	    else
		for (int i : new int[] { 7, 15 })
		{   Colour colour = new Colour(i);
		    resetPalette.append("\033]4;");
		    resetPalette.append(i);
		    resetPalette.append(";rgb:");
		    resetPalette.append("0123456789ABCDEF".charAt(colour.red >>> 4));
		    resetPalette.append("0123456789ABCDEF".charAt(colour.red & 15));
		    resetPalette.append('/');
		    resetPalette.append("0123456789ABCDEF".charAt(colour.green >>> 4));
		    resetPalette.append("0123456789ABCDEF".charAt(colour.green & 15));
		    resetPalette.append('/');
		    resetPalette.append("0123456789ABCDEF".charAt(colour.blue >>> 4));
		    resetPalette.append("0123456789ABCDEF".charAt(colour.blue & 15));
		    resetPalette.append("\033\\");
	}	}
	
	return resetPalette == null ? null : resetPalette.toString();
    }
    
    
    /**
     * {@inheritDoc}
     */ // TODO cache colour matching
    public String applyColour(Color[] palette, Color oldBackground, Color oldForeground, boolean[] oldFormat, Color newBackground, Color newForeground, boolean[] newFormat)
    {
	StringBuilder rc = new StringBuilder();
	
	int colourindex1back = -1, colourindex2back = -1;
	int colourindex1fore = -1, colourindex2fore = -1;
	
	if ((oldBackground != null) && (newBackground == null))
	    rc.append(";40");
	else if ((oldBackground == null) || (oldBackground.equals(newBackground) == false))
	    if (newBackground != null)
	    {
		colourindex1back = matchColour(newBackground, palette, 16, 256, this.chroma);
		if (this.fullcolour)
		    colourindex2back = this.colourful ? matchColour(this.fullcolour ? newBackground : palette[colourindex1back], this.palette, 1, 6, this.chroma) : 7;
		else
		    colourindex2back = colourindex1back;
	    }
	
	if ((oldForeground != null) && (newForeground == null))
	    rc.append(";37");
	else if ((oldForeground == null) || (oldForeground.equals(newForeground) == false))
	    if (newForeground != null)
	    {
		colourindex1fore = matchColour(newForeground, palette, 16, 256, this.chroma);
		if (this.fullcolour)
		{   int s = ((newFormat.length > 9) && newFormat[9]) ? 0 : (newFormat[0] ? 9 : 1);
		    int e = ((newFormat.length > 9) && newFormat[9]) ? 16 : (s + 5);
		    colourindex2fore = this.colourful ? matchColour(this.fullcolour ? newForeground : palette[colourindex1fore], this.palette, s, e, this.chroma) : 15;
		    if (((colourindex2fore == 0) && (newBackground == null)) || (colourindex2fore == colourindex2back))
			colourindex2fore ^= 8;
		}
		else
		    colourindex2fore = colourindex1fore;
	    }
	
	if (colourindex2back != -1)
	    if (this.fullcolour)
	    {   Color colour = newBackground;
		rc.append("m\033]4;");
		rc.append(colourindex2back);
		rc.append(";rgb:");
		rc.append("0123456789ABCDEF".charAt(colour.getRed() >>> 4));
		rc.append("0123456789ABCDEF".charAt(colour.getRed() & 15));
		rc.append('/');
		rc.append("0123456789ABCDEF".charAt(colour.getGreen() >>> 4));
		rc.append("0123456789ABCDEF".charAt(colour.getGreen() & 15));
		rc.append('/');
		rc.append("0123456789ABCDEF".charAt(colour.getBlue() >>> 4));
		rc.append("0123456789ABCDEF".charAt(colour.getBlue() & 15));
		rc.append("\033\\\033[4");
		rc.append(colourindex2back);
		palette[colourindex2back] = colour;
	    }
	    else if (colourindex2back < 16)
	    {   rc.append(";4");
		rc.append(colourindex2back);
	    }
	    else
	    {   rc.append(";48;5;");
		rc.append(colourindex2back);
	    }
	
	if (colourindex2fore != -1)
	    if (this.fullcolour)
	    {   Color colour = newForeground;
		rc.append("m\033]4;");
		rc.append(colourindex2fore);
		rc.append(";rgb:");
		rc.append("0123456789ABCDEF".charAt(colour.getRed() >>> 4));
		rc.append("0123456789ABCDEF".charAt(colour.getRed() & 15));
		rc.append('/');
		rc.append("0123456789ABCDEF".charAt(colour.getGreen() >>> 4));
		rc.append("0123456789ABCDEF".charAt(colour.getGreen() & 15));
		rc.append('/');
		rc.append("0123456789ABCDEF".charAt(colour.getBlue() >>> 4));
		rc.append("0123456789ABCDEF".charAt(colour.getBlue() & 15));
		rc.append("\033\\\033[3");
		rc.append(colourindex2fore & 7);
		palette[colourindex2fore] = colour;
	    }
	    else if (colourindex2fore < 16)
	    {   rc.append(";3");
		rc.append(colourindex2fore & 7);
	    }
	    else
	    {   rc.append(";38;5;");
		rc.append(colourindex2fore);
	    }
	
	boolean temp = newFormat[0];
	newFormat[0] = (colourindex2fore == -1) ? oldFormat[0] : ((8 <= colourindex2fore) && (colourindex2fore < 16));
	for (int i = 0; i < 9; i++)
	    if (newFormat[i] ^ oldFormat[i])
		if ((oldFormat[i] = newFormat[i]))
		{   rc.append(";0");
		    rc.append(i + 1);
		}
		else
		{   rc.append(";2");
		    rc.append(i + 1);
		}
	newFormat[0] = temp;
	
	String _rc = rc.toString();
	if (_rc.isEmpty())
	    return "";
	return ("\033[" + _rc.substring(1)).replace("\033[\033]", "\033]") + "m";
    }
    
    
    /**
     * {@inheritDoc}
     */
    public Object[] parseEscape(int c, Color background, Color foreground, boolean[] format, Color[] colours)
    {
	boolean escape = true;
	if (osi)
	    if (this.ptr > 0)
	    {   this.buf[this.ptr++ - 1] = c;
		if (this.ptr == 8)
		{   this.ptr = 0;
		    osi = escape = false;
		    int index =             (this.buf[0] <= '9') ? (this.buf[0] & 15) : ((this.buf[0] & 15) + 9);
		    int red =               (this.buf[1] <= '9') ? (this.buf[1] & 15) : ((this.buf[1] & 15) + 9);
		    red = (red << 4) |     ((this.buf[2] <= '9') ? (this.buf[2] & 15) : ((this.buf[2] & 15) + 9));
		    int green =             (this.buf[3] <= '9') ? (this.buf[3] & 15) : ((this.buf[3] & 15) + 9);
		    green = (green << 4) | ((this.buf[4] <= '9') ? (this.buf[4] & 15) : ((this.buf[4] & 15) + 9));
		    int blue =              (this.buf[5] <= '9') ? (this.buf[5] & 15) : ((this.buf[5] & 15) + 9);
		    blue = (blue << 4) |   ((this.buf[6] <= '9') ? (this.buf[6] & 15) : ((this.buf[6] & 15) + 9));
		    colours[index] = new Color(red, green, blue);
		}
	    }
	    else if (this.ptr < 0)
	    {   if (~this.ptr == this.buf.length)
		    System.arraycopy(this.buf, 0, this.buf = new int[~this.ptr << 1], 0, ~this.ptr);
		if (c == '\\')
		{   this.ptr = ~this.ptr;
		    this.ptr--;
		    if ((this.ptr > 8) && (this.buf[this.ptr] == '\033') && (this.buf[0] == ';'))
		    {   int[] _code = new int[this.ptr - 1];
			System.arraycopy(this.buf, 1, _code, 0, this.ptr - 1);
			String[] code = Common.utf32to16(_code).split(";");
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
		    this.ptr = 0;
		    osi = escape = false;
		}
		else
		{   this.buf[~this.ptr] = c;
		    this.ptr--;
		}
	    }
	    else if (c == 'P')  this.ptr = 1;
	    else if (c == '4')  this.ptr = ~0;
	    else
	    {   osi = escape = false;
		/*items.add(new Pony.Cell('\033', foreground, background, format));
		items.add(new Pony.Cell(']', foreground, background, format));
		items.add(new Pony.Cell(c, foreground, background, format));*/
		System.err.println("\033[01;31mutil-say: warning: bad escape sequence: OSI 0x" + Integer.toString(c) + "\033[00m");
	    }
	else if (csi)
	{   if (this.ptr == this.buf.length)
		System.arraycopy(this.buf, 0, this.buf = new int[this.ptr << 1], 0, this.ptr);
	    this.buf[this.ptr++] = c;
	    if ((('a' <= c) && (c <= 'z')) || (('A' <= c) && (c <= 'Z')) || (c == '~'))
	    {   csi = escape = false;
		this.ptr--;
		if (c == 'm')
		{   int[] _code = new int[this.ptr];
		    System.arraycopy(this.buf, 0, _code, 0, this.ptr);
		    String[] code = Common.utf32to16(_code).split(";");
		    int xterm256 = 0;
		    boolean back = false;
		    int forei = -2;
		    for (String seg : code)
		    {   int value = Integer.parseInt(seg);
			if (xterm256 == 2)
			{   xterm256 = 0;
			    if (back)  background = colours[value];
			    else       forei = value;
			}
			else if (value == 0)
			{   for (int i = 0; i < 9; i++)
				format[i] = false;
			    background = null;
			    forei = -1;
			}
			else if (xterm256 == 1)
			    xterm256 = value == 5 ? 2 : 0;
			else if (value < 10)
			    format[value - 1] = true;
			else if ((20 < value) && (value < 30))
			    format[value - 21] = false;
			else if (value == 37)   forei = -1; /* Haiku uses 37 instead instead of 39 */
			else if (value == 40)   background = null; /* Haiku uses 40 instead instead of 49 */
			else if (value == 38)   xterm256 = 1;
			else if (value == 48)   xterm256 = 1;
			else if (value < 38)    forei = value - 30;
			else if (value < 48)    background = colours[value - 40];
			else if ((90 <= value) && (value < 98))
			    background = colours[value - 90 + 8];
			else if ((100 <= value) && (value < 108))
			    forei = value - 100 + 8;
			if (xterm256 == 1)
			    back = value == 48;
		    }
		    if (forei == -1)
			foreground = null;
		    else if (forei >= 0)
			if ((forei < 8) && format[0])
			    foreground = colours[forei | 8];
			else
			    foreground = colours[forei];
		}
		this.ptr = 0;
	    }
	}
	else if (c == '[')
	{   csi = true;
	    this.ptr = 0;
	}
	else if (c == ']')
	    osi = true;
	else
	{   escape = false;
	    /*items.add(new Pony.Cell('\033', foreground, background, format));
	      items.add(new Pony.Cell(c, foreground, background, format));*/
	    System.err.println("\033[01;31mutil-say: warning: bad escape sequence: ESC 0x" + Integer.toString(c, 16) + "\033[00m");
	}
	
	this.rcbuf[0] = background;
	this.rcbuf[1] = foreground;
	this.rcbuf[2] = escape ? Boolean.TRUE : Boolean.FALSE;
	return this.rcbuf;
    }
    
    
    
    /**
     * Parse palette
     * 
     * @param   value  String representation, without ESC, ] or P
     * @return         Raw representation
     */
    public static Color[] parsePalette(String value)
    {
	String val = null;
	{   int ptr = 0;
	    char[] buf = new char[value.length()];
	    for (int i = 0, n = buf.length; i < n; i++)
	    {   char c = value.charAt(i);
		if ((c != '\033') && (c != ']'))
		    if (c == ';')
			if ((i > 0) && (buf[ptr - 1] == '4'))
			    buf[ptr - 1] = '/';
			else
			    buf[ptr++] = '/';
		    else if (c == ':')
			ptr -= 3;
		    else
			buf[ptr++] = c;
	    }
	    val = (new String(buf, 0, ptr)).toUpperCase();
	}
	String defvalue = "00000001CD0000200CD003CDCD0041E90FF5CD00CD600CDCD7E5E5E5"
	                + "84C4C4C9FF0000A00FF00BFFFF00C4682B4DFF00FFE00FFFFFFFFFFF";
	Color[] palette = new Color[16];
        for (int ptr = 0, n = defvalue.length(); ptr < n; ptr += 7)
	{
	    int index = Integer.parseInt(defvalue.substring(ptr + 0, ptr + 1), 16);
	    int red   = Integer.parseInt(defvalue.substring(ptr + 1, ptr + 3), 16);
	    int green = Integer.parseInt(defvalue.substring(ptr + 3, ptr + 5), 16);
	    int blue  = Integer.parseInt(defvalue.substring(ptr + 5, ptr + 7), 16);
	    palette[index] = new Color(red, green, blue);
	}
	for (int ptr = 0, n = val.length(); ptr < n;)
	{   String v = val.substring(ptr + 1, val.indexOf('\\', ptr));
	    ptr = v.length() + 2;
	    String[] vs = v.split("/");
	    int index = Integer.parseInt(vs[0], 10);
	    int red   = Integer.parseInt(vs[1], 16);
	    int green = Integer.parseInt(vs[2], 16);
	    int blue  = Integer.parseInt(vs[3], 16);
	    palette[index] = new Color(red, green, blue);
	}
	return palette;
    }
    
}

