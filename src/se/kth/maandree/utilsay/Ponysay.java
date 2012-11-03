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
     * Constructor
     * 
     * @param  flags  Flags passed to the module
     */
    public Ponysay(HashMap<String, String> flags)
    {
	this.even = (flags.contains("even") == false) || flags.get("even").toLowerCase().startswith("y");
	this.tty = flags.contains("tty") && flags.get("tty").toLowerCase().startswith("y");
	this.zebra = flags.contains("zebra") ? flags.get("zebra").toLowerCase().startswith("y") : (this.tty == false);
	this.version = flags.contains("version") ? flags.get("version") : "3.0";
	this.file = flags.contains("file") ? flags.get("file") : null;
	this.fullcolour = flags.contains("fullcolour") && flags.get("fullcolour").toLowerCase().startswith("y");
	this.chroma = (flags.contains("chroma") == false) ? -1 : parseDouble(flags.get("chroma"));
	this.left = (flags.contains("left") == false) ? 2 : parseInteger(flags.get("left"));
	this.right = (flags.contains("right") == false) ? 0 : parseInteger(flags.get("right"));
	this.top = (flags.contains("top") == false) ? 0 : parseInteger(flags.get("top"));
	this.bottom = (flags.contains("bottom") == false) ? 1 : parseInteger(flags.get("bottom"));
	this.palette = (flags.contains("palette") == false) ? null : parsePalette(flags.get("palette").toUpperCase().replace("\033", "").replace("]", "").replace("P", ""));
    }
    
    
    
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
     * Output option: colour palette
     */
    protected Color[] palette;
    
    /**
     * Input/optput option: chroma weight, negative for sRGB distance
     */
    protected double chroma;
    
    /**
     * Output option: ponysay version
     */
    protected String version;
    
    /**
     * Input/optput option: pony file
     */
    protected String file;
    
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

