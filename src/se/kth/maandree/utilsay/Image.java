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
import java.awt.Color;
import java.awt.image.*;
import java.util.*;
import javax.imageio.*;


/**
 * Non-terminal image support module
 * 
 * @author  Mattias Andrée, <a href="mailto:maandree@member.fsf.org">maandree@member.fsf.org</a>
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
	this.file = (this.file = flags.containsKey("file") ? flags.get("file") : null).equals("-") ? null : this.file;
	this.left = (flags.containsKey("left") == false) ? -1 : Integer.parseInt(flags.get("left"));
	this.right = (flags.containsKey("right") == false) ? -1 : Integer.parseInt(flags.get("right"));
	this.top = (flags.containsKey("top") == false) ? -1 : Integer.parseInt(flags.get("top"));
	this.bottom = (flags.containsKey("bottom") == false) ? -1 : Integer.parseInt(flags.get("bottom"));
	this.magnified = (flags.containsKey("magnified") == false) ? 2 : Integer.parseInt(flags.get("magnified"));
	this.encoded = flags.containsKey("encoded") && flags.get("encoded").toLowerCase().startsWith("y");
	this.balloon = this.encoded ? false : ((flags.containsKey("balloon") == false) || flags.get("balloon").toLowerCase().startsWith("y"));
	this.format = flags.containsKey("format") ? flags.get("format") : "png";
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
	BufferedImage image = ImageIO.read(new File(file));
	int width  = image.getWidth()  / this.magnified;
	int height = image.getHeight() / this.magnified;
	int div = this.magnified * this.magnified;
	
	Pony.Cell cell;
	Pony pony = new Pony(height >> 1, width, null, null);
	for (int y = 0; y < height; y += 2)
	    for (int x = 0; x < width; x++)
	    {
		int a = 0, r = 0, g = 0, b = 0;
		for (int yy = 0; yy < this.magnified; yy++)
		    for (int xx = 0; xx < this.magnified; xx++)
		    {   int argb = image.getRGB(x * this.magnified + xx, (y * 2) * this.magnified + yy);
			a += (argb >> 24) & 255;
			r += (argb >> 16) & 255;
			g += (argb >>  8) & 255;
			b +=  argb        & 255;
		    }
		a /= div; r /= div; g /= div; b /= div;
		pony.matrix[y][x] = cell = new Pony.Cell(Pony.Cell.PIXELS, new Color(a, r, g, b), null, null);
		
		if ((y * 2 + 2) * this.magnified <= image.getHeight())
		{
		    a = r = g = b = 0;
		    for (int yy = 0; yy < this.magnified; yy++)
			for (int xx = 0; xx < this.magnified; xx++)
			{   int argb = image.getRGB(x * this.magnified + xx, (y * 2 + 1) * this.magnified + yy);
			    a += (argb >> 24) & 255;
			    r += (argb >> 16) & 255;
			    g += (argb >>  8) & 255;
			    b +=  argb        & 255;
			}
		    a /= div; r /= div; g /= div; b /= div;
		    cell.lowerColour = new Color(a, r, g, b);
		}
		
		if (encoded && (cell.upperColour.getAlpha() == cell.lowerColour.getAlpha()))
		{   r = cell.upperColour.getRed();
		    g = cell.upperColour.getGreen();
		    b = cell.upperColour.getBlue();
		    int r2 = cell.upperColour.getRed();
		    int g2 = cell.upperColour.getGreen();
		    int b2 = cell.upperColour.getBlue();
		    switch (cell.upperColour.getAlpha())
		    {
			case 100:
			    if ((r == 0) && (g == 0) && (b == 255))
				pony.matrix[y][x] = new Pony.Cell(Pony.Cell.NNE_SSW, null, null, null);
			    else if ((r == 255) && (g == 0) && (b == 0))
				pony.matrix[y][x] = new Pony.Cell(Pony.Cell.NNW_SSE, null, null, null);
			    break;
			    
		        case 99:
			    boolean jl = (r & 128) == 128;
			    boolean jr = (g & 128) == 128;
			    int left = r & 127;
			    int minw = g & 127;
			    int maxw = b;
			    boolean jt = (r2 & 128) == 128;
			    boolean jb = (g2 & 128) == 128;
			    int top = r2 & 127;
			    int minh = g2 & 127;
			    int maxh = b2;
			    int justification = (jl ? Pony.Balloon.LEFT   : Pony.Balloon.NONE)
				              | (jr ? Pony.Balloon.RIGHT  : Pony.Balloon.NONE)
				              | (jt ? Pony.Balloon.TOP    : Pony.Balloon.NONE)
				              | (jb ? Pony.Balloon.BOTTOM : Pony.Balloon.NONE);
			    pony.matrix[y][x] = null;
			    pony.metamatrix = new Pony.Meta[] { new Pony.Balloon(
					      left == 0 ? null : new Integer(left), top  == 0 ? null : new Integer(top),
					      minw == 0 ? null : new Integer(minw), minh == 0 ? null : new Integer(minh),
					      maxw == 0 ? null : new Integer(maxw), maxh == 0 ? null : new Integer(maxh),
					      justification) };
			    break;
		}   }
	    }
	
	// TODO this.balloon
	
	return pony;
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

