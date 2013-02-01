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

import java.awt.Color;


/**
 * I'm a pony!
 * 
 * @author  Mattias Andrée, <a href="mailto:maandree@member.fsf.org">maandree@member.fsf.org</a>
 */
public class Pony
{
    /**
     * Constructor
     * 
     * @param  heigth   The height of the pony, in lines (paired pixels)
     * @param  width    The width of the pony, in columns (pixels)
     * @param  comment  Metadata comment on pony, may be {@code null}
     * @param  tags     Metadata tags, one {key, value} format, may be {@code null}
     */
    public Pony(int height, int width, String comment, String[][] tags)
    {
	this.height = height;
	this.width = width;
	this.comment = comment;
	this.tags = tags;
	
	this.matrix = new Pony.Cell[height][width];
	this.metamatrix = new Pony.Meta[height][width + 1][];
    }
    
    
    
    /**
     * The height of the pony, in lines (paired pixels)
     */
    public int height;
    
    /**
     * The width of the pony, in columns (pixels)
     */
    public int width;
    
    /**
     * Metadata comment on pony, may be {@code null}
     */
    public String comment;
    
    /**
     * Metadata tags, one {key, value} format, may be {@code null}
     */
    public String[][] tags;
    
    /**
     * The cells in the pony
     */
    public Pony.Cell[][] matrix;
    
    /**
     * The metacells in the pony
     */
    public Pony.Meta[][][] metamatrix;
    
    
    
    /**
     * A charcter cell in the pony
     */
    public class Cell
    {
	/**
	 * Pixel pair
	 */
	public static final int PIXELS = -1;
	
	/**
	 * NNE–SSW link
	 */
	public static final int NNE_SSW = -2;
	
	/**
	 * NNW–SSE link
	 */
	public static final int NNW_SSE = -3;
	
	
	
	/**
	 * Constructor
	 * 
	 * @param  character    The character duo in UTF-32, if negative it will have a special meaning, for example pixels or boolean link
	 * @param  upperColour  The colour to apply to the upper pixed if a pixel, foreground colour if character, otherwise ignored, {@code null} is preferable for fully transparent
	 * @param  lowerColour  The colour to apply to the lower pixed if a pixel, background colour if character, otherwise ignored, {@code null} is preferable for fully transparent
	 * @param  format       Formatting to apply, nine booleans
	 */
	public Cell(int character, Color upperColour, Color lowerColour, boolean[] format)
	{
	    this.character = character;
	    this.upperColour = upperColour;
	    this.lowerColour = lowerColour;
	    System.arraycopy(format, 0, this.format = new boolean[9], 0, 9);
	}
	
	
	
	/**
	 * The character in UTF-32, if negative it will have a special meaning, for example pixels or boolean link
	 */
	public int character;
	
	/**
	 * The colour to apply to the upper pixed if a pixel, foreground colour if character, otherwise ignored, {@code null} is preferable for fully transparent
	 */
	public Color upperColour;
	
	/**
	 * The colour to apply to the lower pixed if a pixel, background colour if character, otherwise ignored, {@code null} is preferable for fully transparent
	 */
	public Color lowerColour;
	
	/**
	 * Formatting to apply, nine booleans
	 */
	public boolean[] format;
	
    }
    
    
    
    /**
     * Weird stuff going on before a cell
     */
    public interface Meta implements Meta
    {
	// Marker interface
    }
    
    
    
    /**
     * Combining character
     */
    public class Combining implements Meta
    {
	/**
	 * Constructor
	 * 
	 * @param  character         The character in UTF-32
	 * @param  foregroundColour  Foreground colour to apply to the character
	 * @param  backgroundColour  Background colour to apply to the character
	 * @param  format            Formatting to apply, nine booleans
	 */
	public Combining(int character, Color foregroundColour, Color backgroundColour, boolean[] format)
	{
	    this.character = character;
	    this.foregroundColour = foregroundColour;
	    this.backgroundColour = backgroundColour;
	    System.arraycopy(format, 0, this.format = new boolean[9], 0, 9);
	}
	
	
	
	/**
	 * The character in UTF-32
	 */
	public int character;
	
	/**
	 * Foreground colour to apply to the character
	 */
	public Color foregroundColour;
	
	/**
	 * Background colour to apply to the character
	 */
	public Color backgroundColour;
	
	/**
	 * Formatting to apply, nine booleans
	 */
	public boolean[] format;
	
    }
    
    
    /**
     * Variable usage
     */
    public class Recall implements Meta
    {
	/**
	 * Constructor
	 * 
	 * @param  name              The name of the variable
	 * @param  foregroundColour  Foreground colour to apply to the region
	 * @param  backgroundColour  Background colour to apply to the region
	 * @param  format            Formatting to apply, nine booleans
	 */
	public Recall(String name, Color foregroundColour, Color backgroundColour, boolean[] format)
	{
	    this.name = name;
	    this.foregroundColour = foregroundColour;
	    this.backgroundColour = backgroundColour;
	    System.arraycopy(format, 0, this.format = new boolean[9], 0, 9);
	}
	
	
	
	/**
	 * The name of the variable
	 */
	public String name;
	
	/**
	 * Foreground colour to apply to the region
	 */
	public Color foregroundColour;
	
	/**
	 * Background colour to apply to the region
	 */
	public Color backgroundColour;
	
	/**
	 * Formatting to apply, nine booleans
	 */
	public boolean[] format;
	
    }
    
    
    /**
     * Variable storing
     */
    public class Store implements Meta
    {
	/**
	 * Constructor
	 * 
	 * @param  name   The name of the variable
	 * @parma  value  The value of the variable
	 */
	public Store(String name, String value)
	{
	    this.name = name;
	    this.value = value;
	}
	
	
	
	/**
	 * The name of the variable
	 */
	public String name;
	
	/**
	 * The value of the variable
	 */
	public String value;
	
    }
    
    
    /**
     * Balloon
     */
    public class Balloon implements Meta
    {
	/**
	 * No justification applied
	 */
	public static final int NONE = 0;
	
	/**
	 * Left justification, fill to right
	 */
	public static final int LEFT = 1;
	
	/**
	 * Right justification, fill to left
	 */
	public static final int RIGHT = 2;
	
	/**
	 * Centre justification, center the balloon
	 */
	public static final int CENTRE = 3;
	
	/**
	 * Top vertical justification, fill down
	 */
	public static final int TOP = 4;
	
	/**
	 * Bottom vertical justification, fill up
	 */
	public static final int BOTTOM = 8;
	
	/**
	 * Middle vertical justification, center vertically
	 */
	public static final int MIDDLE = 12;
	
	
	
	/**
	 * Constructor
	 * 
	 * @param  left           Extra left offset
	 * @param  top            Extra top offset
	 * @param  minWidth       The minimum width of the balloon
	 * @param  minHeight      The minimum height of the balloon
	 * @param  maxWidth       The minimum width of the balloon
	 * @param  maxHeight      The maximum height of the balloon
	 * @param  justification  Balloon placement justification
	 */
	public Balloon(Integer left, Integer top, Integer minWidth, Integer minHeight, Integer maxWidth, Integer maxHeigth, int justification)
	{
	    this.left = left;
	    this.top = top;
	    this.minWidth = minWidth;
	    this.minHeight = minHeight;
	    this.maxWidth = maxWidth;
	    this.maxHeight = maxHeight;
	    this.justification = justification;
	}
	
	
	
	/**
	 * Extra left offset
	 */
	public Integer left;
	
	/**
	 * Extra top offset
	 */
	public Integer top;
	
	/**
	 * The minimum width of the balloon
	 */
	public Integer minWidth;
	
	/**
	 * The minimum height of the balloon
	 */
	public Integer minHeigth;
	
	/**
	 * The maximum width of the balloon
	 */
	public Integer maxWidth;
	
	/**
	 * The maximum height of the balloon
	 */
	public Integer maxHeigth;
	
	/**
	 * Balloon placement justification
	 */
	public int justification;
	
    }
    
}

