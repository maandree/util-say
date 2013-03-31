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

import java.awt.*;
import java.util.*;


/**
 * <tt>xterm-256color</tt> submodule for {@link Ponysay}
 * 
 * @author  Mattias Andrée, <a href="mailto:maandree@member.fsf.org">maandree@member.fsf.org</a>
 */
public class PonysayXterm extends PonysaySubmodule
{
    /**
     * Constructor
     * 
     * @param  flags  Flags passed to the module
     */
    public PonysayXterm(HashMap<String, String> flags)
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
        
    // KEYWORD when colourlabs supports convertion from sRGB, enabled preceptional distance
    
    
    
    /**
     * {@inheritDoc}
     */
    public void initImport(Color[] colours)
    {
	if (this.palette != null)
	    System.arraycopy(this.palette, 0, colours, 0, 16);
	else
	    this.palette = PonysayXterm.("");
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
	}   }
	
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
	    rc.append(";49");
	else if ((oldBackground == null) || (oldBackground.equals(newBackground) == false))
	    if (newBackground != null)
	    {
		colourindex1back = matchColour(newBackground, palette, 16, 256, this.chroma);
		if (this.fullcolour)
		    colourindex2back = (this.colourful ? matchColour(this.fullcolour ? newBackground : palette[colourindex1back], this.palette, 0, 8, this.chroma) : 7);
		else
		    colourindex2back = colourindex1back;
	    }
	
	if ((oldForeground != null) && (newForeground == null))
	    rc.append(";39");
	else if ((oldForeground == null) || (oldForeground.equals(newForeground) == false))
	    if (newForeground != null)
	    {
		colourindex1fore = matchColour(newForeground, palette, 16, 256, this.chroma);
		if (this.fullcolour)
		{   int b = newFormat[0] ? 8 : 0;
		    colourindex2fore = (this.colourful ? matchColour(this.fullcolour ? newForeground : palette[colourindex1fore], this.palette, b, b + 8, this.chroma) : 15);
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
		rc.append(colourindex2fore);
	    }
	    else
	    {   rc.append(";38;5;");
		rc.append(colourindex2fore);
	    }
	if (this.fullcolour && (colourindex2fore >= 0))
	    newFormat[0] = (colourindex2fore & 8) == 8;
	
	for (int i = 0; i < 9; i++)
	    if (newFormat[i] ^ oldFormat[i])
		if (newFormat[i])
		{   rc.append(";");
		    rc.append(i);
		}
		else
		{   rc.append(";2");
		    rc.append(i);
		}
	
	String _rc = rc.toString();
	if (_rc.isEmpty())
	    return "";
	return ("\033[" + _rc.substring(1)).replace("\033[\033]", "\033]") + "m";
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
		if ((c != '\033') && (c != ']') && (c != 'P'))
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
	String defvalue = "00000001AA0000200AA003AA550040000AA5AA00AA600AAAA7AAAAAA"
	                + "85555559FF5555A55FF55BFFFF55C5555FFDFF55FFE55FFFFFFFFFFF";
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
	{   int index, red, green, blue;
	    if (val.charAt(ptr) == '/')
	    {   String v = val.substring(ptr + 1, val.indexOf('\\', ptr));
		ptr = v.length() + 2;
		String[] vs = v.split("/");
		index = Integer.parseInt(vs[0], 10);
		red   = Integer.parseInt(vs[1], 16);
		green = Integer.parseInt(vs[2], 16);
		blue  = Integer.parseInt(vs[3], 16);
	    }
	    else
	    {	index = Integer.parseInt(val.substring(ptr + 0, ptr + 1), 16);
		red   = Integer.parseInt(val.substring(ptr + 1, ptr + 3), 16);
		green = Integer.parseInt(val.substring(ptr + 3, ptr + 5), 16);
		blue  = Integer.parseInt(val.substring(ptr + 5, ptr + 7), 16);
		ptr += 7;
	    }
	    palette[index] = new Color(red, green, blue);
	}
	return palette;
    }
    
}

