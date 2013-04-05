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


/**
 * Module common functionallity
 * 
 * @author  Mattias Andrée, <a href="mailto:maandree@member.fsf.org">maandree@member.fsf.org</a>
 */
public class Common
{
    /**
     * Non-constructor
     */
    private Common()
    {
	assert false : "This class [Common] is not meant to be instansiated.";
    }
    // TODO padding should not move the balloon
    
    
    
    /**
     * Place a balloon in the top left of a {@link Pony} and create a link
     * 
     * @param  pony   The pony to edit
     * @param  space  The additional space at the top
     */
    public static void insertBalloon(Pony pony, int space)
    {
	int y = 0, x = 0, w = pony.width;
	outer:
	    for (int h = pony.height; y <= h; y++)
	    {   if (y == h)
		{   y = x = -1;
		    break;
		}
		for (x = 0; x < w; x++)
		{   Pony.Cell cell = pony.matrix[y][x];
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
	{   System.arraycopy(pony.matrix, 0, pony.matrix = new Pony.Cell[pony.height + 1 + space][], 1 + space, pony.height);
	    System.arraycopy(pony.metamatrix, 0, pony.metamatrix = new Pony.Meta[pony.height + 1 + space][][], 1 + space, pony.height);
	    for (int i = 0, mw = w + 1; i <= space; i++)
	    {   pony.matrix[i] = new Pony.Cell[w];
		pony.metamatrix[i] = new Pony.Meta[mw][];
	    }
	    pony.height += 1 + space;
	    y += 1 + space;
	    if (y > x)
		for (int i = 0, mw = w + 1, h = pony.height; i < h; i++)
		{   System.arraycopy(pony.matrix[i], 0, pony.matrix[i] = new Pony.Cell[w], 0, w);
		    System.arraycopy(pony.metamatrix[i], 0, pony.metamatrix[i] = new Pony.Meta[mw][], 0, mw);
		}
	    x -= y;
	    if (x < -1)
		x = -1;
	    
	    for (int i = 1; i < y; i++)
		pony.matrix[i][x + i] = new Pony.Cell(Pony.Cell.NNW_SSE, null, null, null);
	}
	else if ((pony.height == 0) || (w == 0))
	{   pony.height = pony.width = 1;
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
    
    
    /**
     * Change the margins in a {@link Pony}
     * 
     * @param   pony    The the pony, the attributes {@link Pony#matrix} and {@link Pony#metamatrix} but not {@link Pony#height} nor  {@link Pony#width} will be updated
     * @param   left    The left margin, negative for unmodified
     * @param   right   The right margin, negative for unmodified
     * @param   top     The top margin, negative for unmodified
     * @param   bottom  The bottom margin, negative for unmodified
     * @return          The update {@code {left, right, top, bottom}}
     */
    public static int[] changeMargins(Pony pony, int left, int right, int top, int bottom)
    {
	Pony.Cell[][] matrix = pony.matrix;
	Pony.Meta[][][] metamatrix = pony.metamatrix;
	
	if (left >= 0)
	{
	    int cur = 0;
	    outer:
	        for (int n = matrix[0].length; cur < n; cur++)
		    for (int j = 0, m = matrix.length; j < m; j++)
		    {
			boolean cellpass = true;
			Pony.Cell cell = matrix[j][cur];
			if (cell != null)
			    if ((cell.character != ' ') || (cell.lowerColour != null))
				if ((cell.character != Pony.Cell.PIXELS) || (cell.lowerColour != null) || (cell.upperColour != null))
				    cellpass = false;
			Pony.Meta[] meta = metamatrix[j][cur];
			if ((meta != null) && (meta.length != 0))
			{   for (int k = 0, l = meta.length; k < l; k++)
				if ((meta[k] != null) && ((meta[k] instanceof Pony.Store) == false))
				    if ((cellpass == false) || (meta[k] instanceof Pony.Balloon))
					break outer;
			}
			else
			    if (cellpass == false)
				break outer;
		    }
	    left -= cur;
	    // if (left < 0)
	    // {
	    //     int w = matrix[0].length;
	    //	   for (int j = 0, n = matrix.length; j < n; j++)
	    //	   {   System.arraycopy(matrix[j], 0, matrix[j] = new Pony.Cell[w - left], -left, w);
	    //	       System.arraycopy(metamatrix[j], 0, metamatrix[j] = new Pony.Meta[w + 1 - left][], -left, w + 1);
	    //	   }
	    //	   left = 0;
	    // }
	}
	else
	    left = 0;
	if (right >= 0)
	{
	    int cur = 0;
	    outer:
	        for (int n = matrix[0].length - 1; cur <= n; cur++)
		    for (int j = 0, m = matrix.length; j < m; j++)
		    {
			boolean cellpass = true;
			Pony.Cell cell = matrix[j][n - cur];
			if (cell != null)
			    if ((cell.character != ' ') || (cell.lowerColour != null))
				if ((cell.character != Pony.Cell.PIXELS) || (cell.lowerColour != null) || (cell.upperColour != null))
				    cellpass = false;
			Pony.Meta[] meta = metamatrix[j][n - cur];
			if ((meta != null) && (meta.length != 0))
			{   for (int k = 0, l = meta.length; k < l; k++)
				if ((meta[k] != null) && ((meta[k] instanceof Pony.Store) == false))
				    if ((cellpass == false) || (meta[k] instanceof Pony.Balloon))
					break outer;
			}
			else
			    if (cellpass == false)
				break outer;
		    }
	    right -= cur;
	    // if (right < 0)
	    // {
	    //     int w = matrix[0].length;
	    //     for (int j = 0, n = matrix.length; j < n; j++)
	    //     {   System.arraycopy(matrix[j], 0, matrix[j] = new Pony.Cell[w - right], 0, w);
	    //         System.arraycopy(metamatrix[j], 0, metamatrix[j] = new Pony.Meta[w + 1 - right][], 0, w + 1);
	    //     }
	    //     right = 0;
	    // }
	}
	else
	    right = 0;
	if (top >= 0)
	{
	    int cur = 0, m = Math.min(matrix[0].length + right, matrix[0].length);
	    outer:
	        for (int n = matrix.length; cur < n; cur++)
		{   Pony.Cell[] row = matrix[cur];
		    Pony.Meta[][] metarow = metamatrix[cur];
		    for (int j = Math.max(-left, 0); j < m; j++)
		    {
			boolean cellpass = true;
			Pony.Cell cell = row[j];
			if (cell != null)
			    if ((cell.character != ' ') || (cell.lowerColour != null))
				if ((cell.character != Pony.Cell.PIXELS) || (cell.lowerColour != null) || (cell.upperColour != null))
				    cellpass = false;
			Pony.Meta[] meta = metarow[j];
			if ((meta != null) && (meta.length != 0))
			{   for (int k = 0, l = meta.length; k < l; k++)
				if ((meta[k] != null) && ((meta[k] instanceof Pony.Store) == false))
				    if ((cellpass == false) || (meta[k] instanceof Pony.Balloon))
					break outer;
			}
			else
			    if (cellpass == false)
				break outer;
		}   }
	    top -= cur;
	    //if (top < 0)
	    // {
	    //     int w = matrix[0].length;
	    //     System.arraycopy(matrix, 0, matrix = new Pony.Cell[matrix.length - top][], -top, matrix.length + top);
	    //     System.arraycopy(new Pony.Cell[-top][w], 0, matrix, 0, -top);
	    //     System.arraycopy(metamatrix, 0, metamatrix = new Pony.Meta[metamatrix.length - top][][], -top, metamatrix.length + top);
	    //     System.arraycopy(new Pony.Meta[-top][w + 1][], 0, metamatrix, 0, -top);
	    //     top = 0;
	    // }
	}
	else
	    top = 0;
	if (bottom >= 0)
	{
	    int cur = 0, m = Math.min(matrix[0].length + right, matrix[0].length);
	    outer:
	    for (int n = matrix.length - 1 + top; cur <= n; cur++)
		if (n - cur < matrix.length)
		{   Pony.Cell[] row = matrix[n - cur];
		    Pony.Meta[][] metarow = metamatrix[n - cur];
		    for (int j = Math.max(-left, 0); j < m; j++)
		    {
			boolean cellpass = true;
			Pony.Cell cell = row[j];
			if (cell != null)
			    if ((cell.character != ' ') || (cell.lowerColour != null))
				if ((cell.character != Pony.Cell.PIXELS) || (cell.lowerColour != null) || (cell.upperColour != null))
				    cellpass = false;
			Pony.Meta[] meta = metarow[j];
			if ((meta != null) && (meta.length != 0))
			{   for (int k = 0, l = meta.length; k < l; k++)
				if ((meta[k] != null) && ((meta[k] instanceof Pony.Store) == false))
				    if ((cellpass == false) || (meta[k] instanceof Pony.Balloon))
					break outer;
			}
			else
			    if (cellpass == false)
				break outer;
		}   }
	    bottom -= cur;
	    // if (bottom < 0)
	    // {
	    //     int h = matrix.length;
	    //     System.arraycopy(matrix, 0, matrix = new Pony.Cell[matrix.length - bottom][], 0, matrix.length + bottom);
	    //     System.arraycopy(new Pony.Cell[-bottom][matrix[0].length], 0, matrix, h, -bottom);
	    //     System.arraycopy(metamatrix, 0, metamatrix = new Pony.Meta[metamatrix.length - bottom][][], 0, metamatrix.length + bottom);
	    //     System.arraycopy(new Pony.Meta[-bottom][metamatrix[0].length][], 0, metamatrix, h, -bottom);
	    //     bottom = 0;
	    // }
	}
	else
	    bottom = 0;
	
	
	if (left > 0)
	{   int w = matrix[0].length;
	    for (int y = 0, h = matrix.length; y < h; y++)
	    {
		System.arraycopy(matrix[y], 0, matrix[y] = new Pony.Cell[w + left], left, w);
		System.arraycopy(metamatrix[y], 0, metamatrix[y] = new Pony.Meta[w + 1 + left][], left, w + 1);
	    }
	    left = 0;
	}
	else
	    left = -left;
	
	if (right > 0)
	{   int w = matrix[0].length;
	    for (int y = 0, h = matrix.length; y < h; y++)
	    {
		System.arraycopy(matrix[y], 0, matrix[y] = new Pony.Cell[w + right], 0, w);
		System.arraycopy(metamatrix[y], 0, metamatrix[y] = new Pony.Meta[w + 1 + right][], 0, w + 1);
	    }
	    right = 0;
	}
	else
	    right = -right;
	
	if (top > 0)
	{
	    int h = matrix.length, w = matrix[0].length;
	    Pony.Cell[][] appendix = new Pony.Cell[top][w];
	    System.arraycopy(matrix, 0, matrix = new Pony.Cell[h + top][], top, h);
	    System.arraycopy(appendix, 0, matrix, 0, top);
	    Pony.Meta[][][] metaappendix = new Pony.Meta[top][w + 1][];
	    System.arraycopy(metamatrix, 0, metamatrix = new Pony.Meta[h + top][w + 1][], top, h);
	    System.arraycopy(metaappendix, 0, metamatrix, 0, top);
	    top = 0;
	}
	else
	    top = -top;
	
	if (bottom > 0)
	{
	    int h = matrix.length, w = matrix[0].length;
	    Pony.Cell[][] appendix = new Pony.Cell[bottom][w];
	    System.arraycopy(matrix, 0, matrix = new Pony.Cell[h + bottom][], 0, h);
	    System.arraycopy(appendix, 0, matrix, h, bottom);
	    Pony.Meta[][][] metaappendix = new Pony.Meta[bottom][w + 1][];
	    System.arraycopy(metamatrix, 0, metamatrix = new Pony.Meta[h + bottom][][], 0, h);
	    System.arraycopy(metaappendix, 0, metamatrix, h, bottom);
	    bottom = 0;
	}
	else
	    bottom = -bottom;
	
	pony.matrix = matrix;
	pony.metamatrix = metamatrix;
	return new int[] { left, right, top, bottom };
    }
    
    
    /**
     * Converts an integer array to a string with only 16-bit charaters
     * 
     * @param   ints  The int array
     * @return        The string
     */
    public static String utf32to16(final int... ints)
    {
	int len = ints.length;
	for (final int i : ints)
	    if (i > 0xFFFF)
		len++;
	    else if (i > 0x10FFFF)
		throw new RuntimeException("Be serious, there is no character above plane 16.");
	
	final char[] chars = new char[len];
	int ptr = 0;
	
	for (final int i : ints)
	    if (i <= 0xFFFF)
		chars[ptr++] = (char)i;
	    else
	    {
		/* 10000₁₆ + (H − D800₁₆) ⋅ 400₁₆ + (L − DC00₁₆) */
		
		int c = i - 0x10000;
		int L = (c & 0x3FF) + 0xDC00;
		int H = (c >>> 10) + 0xD800;
		
		chars[ptr++] = (char)H;
		chars[ptr++] = (char)L;
	    }
	
	return new String(chars);
    }
    
    /**
     * Parse double value
     * 
     * @param   value  String representation
     * @return         Raw representation, -1 if not a number
     */
    public static double parseDouble(String value)
    {
	try
	{   return Double.parseDouble(value);
	}
	catch (Throwable err)
	{   return -1.0;
	}
    }
    
    /**
     * Parse double value
     * 
     * @param   value         String representation
     * @param   defaultValue  Default value that will be used if that string starts with a ‘y’ or ‘Y’
     * @return                Raw representation, -1 if not a number
     */
    public static double parseDouble(String value, double defaultValue)
    {
	if (value.startsWith("y") || value.startsWith("Y"))
	    return defaultValue;
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
    public static int parseInteger(String value)
    {
	try
	{   return Integer.parseInt(value);
	}
	catch (Throwable err)
	{   return -1;
	}
    }
    
    /**
     * Parse integer value
     * 
     * @param   value         String representation
     * @param   defaultValue  Default value that will be used if that string starts with a ‘y’ or ‘Y’
     * @return                Raw representation, -1 if not an integer
     */
    public static int parseInteger(String value, int defaultValue)
    {
	if (value.startsWith("y") || value.startsWith("Y"))
	    return defaultValue;
	try
	{   return Integer.parseInt(value);
	}
	catch (Throwable err)
	{   return -1;
	}
    }
    
}

