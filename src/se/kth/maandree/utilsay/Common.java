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
		for (int i = 0, my = y + 1, mw = w + 1, h = pony.height; i <= h; i++)
		{   System.arraycopy(pony.matrix[i], 0, pony.matrix[i] = new Pony.Cell[y], 0, w);
		    System.arraycopy(pony.metamatrix[i], 0, pony.metamatrix[i] = new Pony.Meta[my][], 0, mw);
		}
	    x -= y;
	    
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
}


