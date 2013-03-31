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
	this.left = (flags.containsKey("left") == false) ? -1 : Common.parseInteger(flags.get("left"));
	this.right = (flags.containsKey("right") == false) ? -1 : Common.parseInteger(flags.get("right"));
	this.top = (flags.containsKey("top") == false) ? (this.balloon ? 3 : -1) : Common.parseInteger(flags.get("top"));
	this.bottom = (flags.containsKey("bottom") == false) ? -1 : Common.parseInteger(flags.get("bottom"));
	this.magnified = (flags.containsKey("magnified") == false) ? 2 : Common.parseInteger(flags.get("magnified"));
	this.encoded = flags.containsKey("encoded") && flags.get("encoded").toLowerCase().startsWith("y");
	this.format = flags.containsKey("format") ? flags.get("format") : null;
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
     * Input/output option: insert balloon into the image
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
	BufferedImage image = ImageIO.read(new BufferedInputStream(this.file == null ? System.in : new FileInputStream(this.file)));
	int width  = image.getWidth()  / this.magnified;
	int height = image.getHeight() / this.magnified;
	int div = this.magnified * this.magnified;
	
	Pony.Cell cell;
	Pony pony = new Pony((height >> 1) + (height & 1), width, null, null);
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
	    Common.insertBalloon(pony, this.top);
	
	return pony;
    }
    
    
    /**
     * Export a pony to the file
     * 
     * @param  pony  The pony
     * 
     * @throws  IOException  On image export error
     */
    public void exportPony(Pony pony) throws IOException
    {
	BufferedImage img = new BufferedImage(pony.width * this.magnified, (pony.height << 1) * this.magnified, BufferedImage.TYPE_INT_ARGB);
	Color TRANSPARENT = new Color(0, 0, 0, 0);
	
	Common.changeMargins(pony, this.left, this.right, this.top, this.bottom);
	
	int h = pony.height, w = pony.width;
	for (int y = 0; y < h; y++)
	{   Pony.Cell[] row = pony.matrix[y];
	    Pony.Meta[][] metarow = pony.metamatrix[y];
	    x_loop: for (int x = 0; x <= w; x++)
	    {   Pony.Meta[] metacell = metarow[x];
		if (metacell != null)
		    for (Pony.Meta meta : metacell)
			if (meta.getClass() == Pony.Recall.class)
			    System.err.println("\033[01;31mutil-say: warning: ignoring recall in image, no way to parse in an image module\033[00m");
			else if (meta.getClass() == Pony.Combining.class)
			    System.err.println("\033[01;31mutil-say: warning: cannot include text in images\033[00m");
			else if (this.encoded && (meta.getClass() == Pony.Balloon.class))
			{
			    Pony.Balloon balloon = (Pony.Balloon)meta;
			    int r = 0, g = 0, b = 0, r2 = 0, g2 = 0, b2 = 0, j = balloon.justification;
			    if ((j & Pony.Balloon.LEFT)   != 0)  r  |= 128;
			    if ((j & Pony.Balloon.RIGHT)  != 0)  g  |= 128;
			    if ((j & Pony.Balloon.TOP)    != 0)  r2 |= 128;
			    if ((j & Pony.Balloon.BOTTOM) != 0)  g2 |= 128;
			    r |= balloon.left == null ? 0 : (balloon.left.intValue() & 127);
			    g |= balloon.minWidth == null ? 0 : (balloon.minWidth.intValue() & 127);
			    b |= balloon.maxWidth == null ? 0 : (balloon.maxWidth.intValue() & 255);
			    r2 |= balloon.top == null ? 0 : (balloon.top.intValue() & 127);
			    g2 |= balloon.minHeight == null ? 0 : (balloon.minHeight.intValue() & 127);
			    b2 |= balloon.maxHeight == null ? 0 : (balloon.maxHeight.intValue() & 255);
			    Color upper = new Color(r, g, b, 99);
			    Color lower = new Color(r2, g2, b2, 99);
			    int _x = x * this.magnified;
			    int uy = (y << 1) * this.magnified;
			    int ly = ((y << 1) | 1) * this.magnified;
			    int u = upper.getRGB(), l = lower.getRGB();
			    for (int my = 0; my < this.magnified; my++)
				for (int mx = 0; mx < this.magnified; mx++)
				{   img.setRGB(_x + mx, uy + my, u);
				    img.setRGB(_x + mx, ly + my, l);
				}
			    // TODO add warning for if cells should override this
			    continue x_loop;
			}
		        /*else if (meta.getClass() == Pony.Store.class)
			    ; // Do nothing*/
		
		if (x != w)
		{
		    Pony.Cell cell = row[x];
		    Color upper, lower;
		    if (cell == null)
			upper = lower = TRANSPARENT;
		    else
		    {   boolean whitespace = (cell.character == ' ') || (cell.character == ' ');
			upper = whitespace ? cell.lowerColour : cell.upperColour;
			lower = cell.lowerColour;
			whitespace |= cell.character == Pony.Cell.PIXELS;
			if (cell.character == Pony.Cell.NNW_SSE)
			    upper = lower = this.encoded ? new Color(255, 0, 0, 100) : TRANSPARENT;
			else if (cell.character == Pony.Cell.NNE_SSW)
			    upper = lower = this.encoded ? new Color(0, 0, 255, 100) : TRANSPARENT;
			else
			{   if (!whitespace)
				System.err.println("\033[01;31mutil-say: warning: cannot include text in images\033[00m");
			    if ((upper == null) || !whitespace)  upper = TRANSPARENT;
			    if ((lower == null) || !whitespace)  lower = TRANSPARENT;
		    }	}
		    int _x = x * this.magnified;
		    int uy = (y << 1) * this.magnified;
		    int ly = ((y << 1) | 1) * this.magnified;
		    int u = upper.getRGB(), l = lower.getRGB();
		    for (int my = 0; my < this.magnified; my++)
			for (int mx = 0; mx < this.magnified; mx++)
			{   img.setRGB(_x + mx, uy + my, u);
			    img.setRGB(_x + mx, ly + my, l);
			}
	}   }   }
	
	if ((pony.comment != null) && (pony.comment.length() != 0))
	    System.err.println("\033[01;31mutil-say: warning: not capable of exporting metadata to images\033[00m");
	else if ((pony.tags != null) && (pony.tags.length != 0))
	    System.err.println("\033[01;31mutil-say: warning: not capable of exporting metadata to images\033[00m");
	
	String fmt = this.format;
	if (fmt == null)
	    if (this.file == null)
		fmt = "png";
	    else
	    {   fmt = this.file.contains("/") ? this.file.substring(this.file.lastIndexOf("/") + 1) : this.file;
		fmt = this.file.contains(".") ? this.file.substring(this.file.lastIndexOf(".") + 1) : "png";
	    }
	ImageIO.write(img, fmt, new BufferedOutputStream(this.file == null ? System.out : new FileOutputStream(this.file)));
    }
    
}

