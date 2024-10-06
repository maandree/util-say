/**
 * util-say — Utilities for cowsay and cowsay-like programs
 *
 * Copyright © 2012, 2013  Mattias Andrée (m@maandree.se)
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
import java.util.*;


/**
 * Test module
 * 
 * @author  Mattias Andrée, <a href="mailto:m@maandree.se">m@maandree.se</a>
 */
public class Test
{
    /**
     * Constructor
     * 
     * @param  flags  Flags passed to the module
     */
    public Test(HashMap<String, String> flags)
    {
	// Do nothing
    }
    
    
    
    /**
     * Import a test pony
     * 
     * @return  The pony
     * 
     * @throws  IOException  On I/O error
     */
    public Pony importPony() throws IOException
    {
	int Y = 10, X = 20;
	Pony pony = new Pony(Y, X, "Test pony", new String[][] {{"PONY", "test"}});
	/**/
	for (int y = 0; y < Y; y++)
	    for (int x = 0; x < X; x++)
		if ((y < 5) ^ (x < 10))
		    if ((x & 2) == 2)
			pony.matrix[y][x] = new Pony.Cell(Pony.Cell.PIXELS, Color.BLUE, Color.RED, null);
		    else
			pony.matrix[y][x] = new Pony.Cell(Pony.Cell.PIXELS, Color.RED, Color.BLUE, null);
		else
		    if ((x & 2) == 2)
			pony.matrix[y][x] = new Pony.Cell(Pony.Cell.PIXELS, Color.GREEN, Color.YELLOW, null);
		    else
			pony.matrix[y][x] = new Pony.Cell(Pony.Cell.PIXELS, Color.YELLOW, Color.GREEN, null);
	/**/
	return pony;
    }
    
    
    /**
     * Test a pony against a test pony
     * 
     * @param  pony  The pony
     */
    public void exportPony(Pony pony)
    {
    }
    
}

