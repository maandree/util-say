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
	this.file = (flags.containsKey("file") ? (this.file = flags.get("file")).equals("-") : true) ? null : this.file;
	this.balloon = this.encoded ? false : ((flags.containsKey("balloon") == false) || flags.get("balloon").toLowerCase().startsWith("y"));
	this.left = (flags.containsKey("left") == false) ? -1 : Integer.parseInt(flags.get("left"));
	this.right = (flags.containsKey("right") == false) ? -1 : Integer.parseInt(flags.get("right"));
	this.top = (flags.containsKey("top") == false) ? (this.balloon ? 3 : -1) : Integer.parseInt(flags.get("top"));
	this.bottom = (flags.containsKey("bottom") == false) ? -1 : Integer.parseInt(flags.get("bottom"));
	this.magnified = (flags.containsKey("magnified") == false) ? 2 : Integer.parseInt(flags.get("magnified"));
	this.encoded = flags.containsKey("encoded") && flags.get("encoded").toLowerCase().startsWith("y");
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
     * <p>Output option: top margin, negative for unmodified</p>
     * <p>Input option: Extra number of lines between the pony and balloon (must not be negative)</p>
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
     * 
     * @throws  IOException  On I/O error
     */
    public Pony importPony() throws IOException
    {
	BufferedImage image = ImageIO.read(new File(this.file));
	int width  = image.getWidth()  / this.magnified;
	int height = image.getHeight() / this.magnified;
	int div = this.magnified * this.magnified;
	
	Pony.Cell cell;
	Pony pony = new Pony(height >> 1, width, null, null);
	for (int y = 0; y < height - 1; y += 2)
	    for (int x = 0; x < width; x++)
	    {
		int a = 0, r = 0, g = 0, b = 0;
		for (int yy = 0; yy < this.magnified; yy++)
		    for (int xx = 0; xx < this.magnified; xx++)
		    {   int argb = image.getRGB(x * this.magnified + xx, y * this.magnified + yy);
			a += (argb >> 24) & 255;
			r += (argb >> 16) & 255;
			g += (argb >>  8) & 255;
			b +=  argb        & 255;
		    }
		a /= div; r /= div; g /= div; b /= div;
		pony.matrix[y >> 1][x] = cell = new Pony.Cell(Pony.Cell.PIXELS, a == 0 ? null : new Color(r, g, b, a), null, null);
		
		if ((y + 2) * this.magnified <= image.getHeight())
		{
		    a = r = g = b = 0;
		    for (int yy = 0; yy < this.magnified; yy++)
			for (int xx = 0; xx < this.magnified; xx++)
			{   int argb = image.getRGB(x * this.magnified + xx, (y + 1) * this.magnified + yy);
			    a += (argb >> 24) & 255;
			    r += (argb >> 16) & 255;
			    g += (argb >>  8) & 255;
			    b +=  argb        & 255;
			}
		    a /= div; r /= div; g /= div; b /= div;
		    cell.lowerColour = a == 0 ? null : new Color(r, g, b, a);
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
			    if ((r == 255) && (g == 0) && (b == 0))
				pony.matrix[y][x] = new Pony.Cell(Pony.Cell.NNW_SSE, null, null, null);
			    else if ((r == 0) && (g == 0) && (b == 255))
				pony.matrix[y][x] = new Pony.Cell(Pony.Cell.NNE_SSW, null, null, null);
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
			    pony.metamatrix[y][x] = new Pony.Meta[] { new Pony.Balloon(
					            left == 0 ? null : new Integer(left), top  == 0 ? null : new Integer(top),
						    minw == 0 ? null : new Integer(minw), minh == 0 ? null : new Integer(minh),
						    maxw == 0 ? null : new Integer(maxw), maxh == 0 ? null : new Integer(maxh),
						    justification) };
			    break;
		}   }
	    }
	
	if (this.balloon)
	{   int y = 0, x = 0;
	    outer:
	        for (int h = pony.height; y <= h; y++)
		{   if (y == h)
		    {   y = x = -1;
			break;
		    }
		    for (x = 0; x < width; x++)
		    {   cell = pony.matrix[y][x];
			int character = cell == null ? ' ' : cell.character;
			if (character >= 0)
			{   if ((character != ' ') && (character != ' '))
				break outer;
			}
			else if (character == Pony.Cell.PIXELS)
			    if ((cell.upperColour != null) && (cell.lowerColour != null))
				break outer;
		}   }
	    
	    if (y >= 0)
	    {	System.arraycopy(pony.matrix, 0, pony.matrix = new Pony.Cell[pony.height + 1 + this.top][], 1 + this.top, pony.height);
		System.arraycopy(pony.metamatrix, 0, pony.metamatrix = new Pony.Meta[pony.height + 1 + this.top][][], 1 + this.top, pony.height);
		for (int i = 0, w = pony.width, mw = pony.width + 1; i <= this.top; i++)
		{   pony.matrix[i] = new Pony.Cell[w];
		    pony.metamatrix[i] = new Pony.Meta[mw][];
		}
		pony.height += 1 + this.top;
		y += 1 + this.top;
		if (y > x)
		    for (int i = 0, my = y + 1, w = pony.width, mw = pony.width + 1, h = pony.height; i <= h; i++)
		    {	System.arraycopy(pony.matrix[i], 0, pony.matrix[i] = new Pony.Cell[y], 0, w);
			System.arraycopy(pony.metamatrix[i], 0, pony.metamatrix[i] = new Pony.Meta[my][], 0, mw);
		    }
		x -= y;
		
		for (int i = 1; i < y; i++)
		    pony.matrix[i][x + i] = new Pony.Cell(Pony.Cell.NNW_SSE, null, null, null);
	    }
	    else if ((pony.height == 0) || (pony.width == 0))
	    {	pony.height = pony.width = 1;
		pony.matrix = new Pony.Cell[1][1];
		pony.metamatrix = new Pony.Meta[1][1][];
	    }
	    
	    Pony.Balloon speechballoon = new Pony.Balloon(null, null, new Integer(Math.max(x, 5)), null, null, null, Pony.Balloon.NONE);
	    if (pony.metamatrix[0][0] == null)
		pony.metamatrix[0][0] = new Pony.Meta[] { speechballoon };
	    else
	    {   System.arraycopy(pony.metamatrix[0][0], 0, pony.metamatrix[0][0] = new Pony.Meta[pony.metamatrix[0][0].length + 1], 1, pony.metamatrix[0][0].length - 1);
		pony.metamatrix[0][0][0] = speechballoon;
	    }
	}
	
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

