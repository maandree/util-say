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
	this.tty = flags.containsKey("tty") && flags.get("tty").toLowerCase().startsWith("y");
	this.colourful = this.tty && ((flags.containsKey("colourful") == false) || flags.get("colourful").toLowerCase().startsWith("y"));
	this.fullcolour = flags.containsKey("fullcolour") && flags.get("fullcolour").toLowerCase().startsWith("y");
	this.palette = (flags.containsKey("palette") == false) ? null : PonysayXterm.parsePalette(flags.get("palette").toUpperCase().replace("\033", "").replace("]", "").replace("P", ""));
    }
    
    
    
    /**
     * Output option: chroma weight, negative for sRGB distance
     */
    protected double chroma;
    
    /**
     * Output option: linux vt
     */
    protected boolean tty;
    
    /**
     * Output option: colourful tty
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
     * {@inheritDoc}
     */
    public String initExport(Color[] colours)
    {
	if (this.palette != null)
	    System.arraycopy(this.palette, 0, colours, 0, 16);
	else
	    this.palette = parsePalette("");
	
	StringBuilder resetPalette = null;
	if (this.tty)
	    if (this.colourful)
	    {   resetPalette = new StringBuilder();
		for (int i = 0; i < 16; i++)
		{   Colour colour = new Colour(i);
		    resetPalette.append("\033]P");
		    resetPalette.append("0123456789ABCDEF".charAt(i));
		    resetPalette.append("0123456789ABCDEF".charAt(colour.red >>> 4));
		    resetPalette.append("0123456789ABCDEF".charAt(colour.red & 15));
		    resetPalette.append("0123456789ABCDEF".charAt(colour.green >>> 4));
		    resetPalette.append("0123456789ABCDEF".charAt(colour.green & 15));
		    resetPalette.append("0123456789ABCDEF".charAt(colour.blue >>> 4));
		    resetPalette.append("0123456789ABCDEF".charAt(colour.blue & 15));
	    }   }
	    else
	    {   resetPalette = new StringBuilder();
		for (int i : new int[] { 7, 15 })
		{   Colour colour = new Colour(i);
		    resetPalette.append("\033]P");
		    resetPalette.append("0123456789ABCDEF".charAt(i));
		    resetPalette.append("0123456789ABCDEF".charAt(colour.red >>> 4));
		    resetPalette.append("0123456789ABCDEF".charAt(colour.red & 15));
		    resetPalette.append("0123456789ABCDEF".charAt(colour.green >>> 4));
		    resetPalette.append("0123456789ABCDEF".charAt(colour.green & 15));
		    resetPalette.append("0123456789ABCDEF".charAt(colour.blue >>> 4));
		    resetPalette.append("0123456789ABCDEF".charAt(colour.blue & 15));
	    }   }
	else if (this.fullcolour)
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
	{   if (this.tty)
	    {   Color colour = palette[0] = this.palette[0];
		rc.append("m\033]P0");
		rc.append("0123456789ABCDEF".charAt(colour.getRed() >>> 4));
		rc.append("0123456789ABCDEF".charAt(colour.getRed() & 15));
		rc.append("0123456789ABCDEF".charAt(colour.getGreen() >>> 4));
		rc.append("0123456789ABCDEF".charAt(colour.getGreen() & 15));
		rc.append("0123456789ABCDEF".charAt(colour.getBlue() >>> 4));
		rc.append("0123456789ABCDEF".charAt(colour.getBlue() & 15));
		rc.append("\033[49");
	    }
	    else
		rc.append(";49");
	}
	else if ((oldBackground == null) || (oldBackground.equals(newBackground) == false))
	    if (newBackground != null)
	    {
		if ((this.fullcolour && this.tty) == false)
		    colourindex1back = matchColour(newBackground, palette, 16, 256, this.chroma);
		if (this.tty || this.fullcolour)
		    colourindex2back = (this.colourful ? matchColour(this.fullcolour ? newBackground : palette[colourindex1back], this.tty ? this.palette : palette, 0, 8, this.chroma) : 7);
		else
		    colourindex2back = colourindex1back;
	    }
	
	if ((oldForeground != null) && (newForeground == null))
        {   if (this.tty)
	    {   Color colour = palette[7] = this.palette[7];
		rc.append("m\033]P7");
		rc.append("0123456789ABCDEF".charAt(colour.getRed() >>> 4));
		rc.append("0123456789ABCDEF".charAt(colour.getRed() & 15));
		rc.append("0123456789ABCDEF".charAt(colour.getGreen() >>> 4));
		rc.append("0123456789ABCDEF".charAt(colour.getGreen() & 15));
		rc.append("0123456789ABCDEF".charAt(colour.getBlue() >>> 4));
		rc.append("0123456789ABCDEF".charAt(colour.getBlue() & 15));
		rc.append("\033[39");
	    }
	    else
		rc.append(";39");
	}
	else if ((oldForeground == null) || (oldForeground.equals(newForeground) == false))
	    if (newForeground != null)
	    {
		if ((this.fullcolour && this.tty) == false)
		    colourindex1fore = matchColour(newForeground, palette, 16, 256, this.chroma);
		if (this.tty || this.fullcolour)
		{   int b = newFormat[0] ? 8 : 0;
		    colourindex2fore = (this.colourful ? matchColour(this.fullcolour ? newForeground : palette[colourindex1fore], this.tty ? this.palette : palette, b, b + 8, this.chroma) : 15);
		}
		else
		    colourindex2fore = colourindex1fore;
	    }
	
	if (colourindex2back != -1)
	    if (this.tty)
	    {   Color colour = palette[colourindex1back];
		rc.append("m\033]P");
		rc.append("0123456789ABCDEF".charAt(colourindex2back));
		rc.append("0123456789ABCDEF".charAt(colour.getRed() >>> 4));
		rc.append("0123456789ABCDEF".charAt(colour.getRed() & 15));
		rc.append("0123456789ABCDEF".charAt(colour.getGreen() >>> 4));
		rc.append("0123456789ABCDEF".charAt(colour.getGreen() & 15));
		rc.append("0123456789ABCDEF".charAt(colour.getBlue() >>> 4));
		rc.append("0123456789ABCDEF".charAt(colour.getBlue() & 15));
		rc.append("\033[4");
		rc.append(colourindex2back);
	    }
	    else if (this.fullcolour)
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
	    if (this.tty)
	    {   Color colour = palette[colourindex1fore];
		rc.append("m\033]P");
		rc.append("0123456789ABCDEF".charAt(colourindex2fore));
		rc.append("0123456789ABCDEF".charAt(colour.getRed() >>> 4));
		rc.append("0123456789ABCDEF".charAt(colour.getRed() & 15));
		rc.append("0123456789ABCDEF".charAt(colour.getGreen() >>> 4));
		rc.append("0123456789ABCDEF".charAt(colour.getGreen() & 15));
		rc.append("0123456789ABCDEF".charAt(colour.getBlue() >>> 4));
		rc.append("0123456789ABCDEF".charAt(colour.getBlue() & 15));
		rc.append("\033[3");
		rc.append(colourindex2fore & 7);
	    }
	    else if (this.fullcolour)
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
	if (this.tty && (colourindex2fore >= 0))
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
	for (int ptr = 0, n = value.length(); ptr < n; ptr += 7)
	{
	    int index = Integer.parseInt(value.substring(ptr + 0, ptr + 1), 16);
	    int red   = Integer.parseInt(value.substring(ptr + 1, ptr + 3), 16);
	    int green = Integer.parseInt(value.substring(ptr + 3, ptr + 5), 16);
	    int blue  = Integer.parseInt(value.substring(ptr + 5, ptr + 7), 16);
	    palette[index] = new Color(red, green, blue);
	}
	return palette;
    }
    
}

