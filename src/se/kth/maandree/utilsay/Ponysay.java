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
import java.util.HashMap;
import java.awt.Color;


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
	this.zebra = flags.contains("zebra") ? flags.get("zebra").toLowerCase().startswith("y") : (this.tty == false);
	this.version = flags.contains("version") ? parseVersion(flags.get("version")) : VERSION_HORIZONTAL_JUSTIFICATION;
	this.utf8 = this.version > VERSION_COWSAY ? true : (flags.contains("utf8") && flags.get("utf8").toLowerCase().startswith("y"));
	this.fullcolour = flags.contains("fullcolour") && flags.get("fullcolour").toLowerCase().startswith("y");
	this.chroma = (flags.contains("chroma") == false) ? -1 : parseDouble(flags.get("chroma"));
	this.left = (flags.contains("left") == false) ? 2 : parseInteger(flags.get("left"));
	this.right = (flags.contains("right") == false) ? 0 : parseInteger(flags.get("right"));
	this.top = (flags.contains("top") == false) ? 3 : parseInteger(flags.get("top"));
	this.bottom = (flags.contains("bottom") == false) ? 1 : parseInteger(flags.get("bottom"));
	this.palette = (flags.contains("palette") == false) ? null : parsePalette(flags.get("palette").toUpperCase().replace("\033", "").replace("]", "").replace("P", ""));
    }
    
    
    
    /**
     * Input/output option: pony file
     */
    protected String file;
    
    /**
     * Output option: pad right side
     */
    protected boolean even;
    
    /**
     * Output option: linux vt
     */
    protected boolean tty;
    
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
	
	return null; // TODO implement
    }
    
    /**
     * Import the pony from file using the cowsay format
     * 
     * @return  The pony
     */
    protected Pony importCow()
    {
	return null; // TODO implement
    }
    
    
    /**
     * Export a pony to the file
     * 
     * @param  pony  The pony
     */
    public void exportPony(Pony pony)
    {
	// TODO implement
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
    
}

