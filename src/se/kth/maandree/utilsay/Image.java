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
import java.awt.Color;
import java.awt.image.*;
import javax.imageio.*;


/**
 * Non-terminal image support module
 * 
 * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
 */
public class Image
{
    /**
     * Constructor
     * 
     * @param  flags  Flags passed to the module
     */
    public Image(HashMap<String, String> flags)
    {
	this.file = (this.file = flags.contains("file") ? flags.get("file") : null).equals("-") ? null : this.file;
	this.left = (flags.contains("left") == false) ? -1 : Integer.parseInt(flags.get("left"));
	this.right = (flags.contains("right") == false) ? -1 : Integer.parseInt(flags.get("right"));
	this.top = (flags.contains("top") == false) ? -1 : Integer.parseInt(flags.get("top"));
	this.bottom = (flags.contains("bottom") == false) ? -1 : Integer.parseInt(flags.get("bottom"));
	this.magnified = (flags.contains("magnified") == false) ? 2 : Integer.parseInt(flags.get("magnified"));
	this.encoded = flags.contains("encoded") && flags.get("encoded").toLowerCase().startsWith("y");
	this.balloon = this.encoded ? false : ((flags.contains("balloon") == false) || flags.get("balloon").toLowerCase().startsWith("y"));
	this.format = flags.contains("format") ? flags.get("format") : "png";
    }
    
    
    
    /**
     * Input/output option: pony file
     */
    protected String file;
    
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
     * Input/output option: pixel magnification
     */
    protected int magnified;
    
    /**
     * Input/output option: balloon encoding in the image
     */
    protected boolean encoded;
    
    /**
     * Input option: insert balloon into the image
     */
    protected boolean balloon;
    
    /**
     * Output option: image file format
     */
    protected String format;
    
    
    
    /**
     * Import the pony from file
     * 
     * @return  The pony
     */
    public Pony importPony()
    {
	return null
    }
    
    
    /**
     * Export a pony to the file
     * 
     * @param  pony  The pony
     */
    public void exportPony(Pony pony)
    {
    }
    
}

