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
 * Uncooked pony module
 * 
 * @author  Mattias Andrée, <a href="mailto:m@maandree.se">m@maandree.se</a>
 */
public class Raw
{
    /**
     * Constructor
     * 
     * @param  flags  Flags passed to the module
     */
    public Raw(HashMap<String, String> flags)
    {
	this.file = (flags.containsKey("file") ? (this.file = flags.get("file")).equals("-") : true) ? null : this.file;
    }
    
    
    
    /**
     * Input/output option: pony file
     */
    protected String file;
    
    
    
    /**
     * Import the pony from file
     * 
     * @return  The pony
     * 
     * @throws  IOException  On I/O error
     */
    public Pony importPony() throws IOException
    {
	return null;
    }
    
    
    /**
     * Export a pony to the file
     * 
     * @param  pony  The pony
     * 
     * @throws  IOException  On I/O error
     */
    public void exportPony(Pony pony) throws IOException
    {
	OutputStream out = null;
	try
	{   out = this.file == null ? System.out : new BufferedOutputStream(new FileOutputStream(this.file));
	    
	    print(pony.height, out);
	    print(pony.width, out);
	    print(pony.comment, out);
	    
	    if (pony.tags == null)
		out.write('-');
	    else
	    {   out.write('{');
		for (String[] tag : pony.tags)
		{   if (tag == null)
		    {   out.write('-');
			continue;
		    }
		    out.write('(');
		    for (String s : tag)
			print(s, out);
		    out.write(')');
		}
		out.write('}');
	    }
	    
	    if (pony.matrix == null)
		out.write('-');
	    else
	    {   out.write('{');
		for (Pony.Cell[] row : pony.matrix)
		{   if (row == null)
		    {   out.write('-');
			continue;
		    }
		    out.write('[');
		    for (Pony.Cell cell : row)
		    {   if (cell == null)
			{   out.write('-');
			    continue;
			}
			print(cell.character, out);
			print(cell.upperColour, out);
			print(cell.lowerColour, out);
			print(cell.format, out);
		    }
		    out.write(']');
		}
		out.write('}');
	    }
	    
	    if (pony.metamatrix == null)
		out.write('-');
	    else
	    {   out.write('{');
		for (Pony.Meta[][] row : pony.metamatrix)
		{   if (row == null)
		    {   out.write('-');
			continue;
		    }
		    out.write('[');
		    for (Pony.Meta[] cell : row)
		    {   if (cell == null)
			{   out.write('-');
			    continue;
			}
			out.write('(');
			for (Pony.Meta meta : cell)
			    if (meta == null)
				out.write('-');
			    else if (meta instanceof Pony.Combining)
			    {	out.write('T');
				Pony.Combining m = (Pony.Combining)meta;
				print(m.character, out);
				print(m.foregroundColour, out);
				print(m.backgroundColour, out);
				print(m.format, out);
			    }
			    else if (meta instanceof Pony.Recall)
			    {	out.write('$');
				Pony.Recall m = (Pony.Recall)meta;
				print(m.name, out);
				print(m.foregroundColour, out);
				print(m.backgroundColour, out);
				print(m.format, out);
			    }
			    else if (meta instanceof Pony.Store)
			    {	out.write('=');
				Pony.Store m = (Pony.Store)meta;
				print(m.name, out);
				print(m.value, out);
			    }
			    else if (meta instanceof Pony.Balloon)
			    {	out.write('B');
				Pony.Balloon m = (Pony.Balloon)meta;
				print(m.left, out);
				print(m.top, out);
				print(m.minWidth, out);
				print(m.minHeight, out);
				print(m.maxWidth, out);
				print(m.maxHeight, out);
				print(m.justification, out);
			    }
			out.write(')');
		    }
		    out.write(']');
		}
		out.write('}');
	    }
	    
	    out.flush();
	}
	finally
        {   if ((out != null) && (out != System.out))
		try
		{   out.close();
		}
		catch (Throwable ignore)
		{   // Ignore
	}	}
    }
    
    
    private void print(byte value, OutputStream out) throws IOException
    {
	if ((value >>> 4) != 0)
	{   out.write((value & 15) + 'a');
	    value >>>= 4;
	}
	out.write((value & 15) + 'A');
    }
    
    private void print(int value, OutputStream out) throws IOException
    {
	if (value < 0)
	{   out.write('/');
	    value = ~value;
	}
	while ((value >>> 4) != 0)
	{   out.write((value & 15) + 'a');
	    value >>>= 4;
	}
	out.write((value & 15) + 'A');
    }
    
    private void print(Integer value, OutputStream out) throws IOException
    {
	if (value == null)
	    out.write('-');
	else
	    print(value.intValue(), out);
    }
    
    private void print(String value, OutputStream out) throws IOException
    {
	if (value == null)
	{   out.write(1);
	    return;
	}
	for (byte c : value.getBytes("UTF-8"))
	    if ((c == 0) || (c == 1))
	    {   out.write(192);
		out.write(128 | c);
	    }
	    else
		out.write(c);
	out.write(0);
    }
    
    private void print(boolean[] value, OutputStream out) throws IOException
    {
	if (value == null)
	{   out.write('-');
	    return;
	}
	if (value.length == 0)
	{   out.write('@');
	    return;
	}
	_if:{
	    if (value.length <= 18)
	    {	for (boolean b : value)
		    if (b)
			break _if;
		out.write(value.length + '-');
		return;
	    }}
	for (int i = 0, n = value.length; i < n; i += 4)
	{
	    int c = Math.max(n - i, 4);
	    int v = 0;
	    for (int j = 0; j < c; j++)
		v |= value[i + j] ? (1 << c) : 0;
	    if      (c == 4)  v += 'a';
	    else if (c == 3)  v += 'q';
	    else if (c == 2)  v += 'y';
	    else if (c == 1)  v += ']';
	    if (i + 4 < n)
		v += 0x20;
	    out.write(v);
	}
    }
    
    private void print(Color value, OutputStream out) throws IOException
    {
	if (value == null)
	{   out.write('-');
	    return;
	}
	print(value.getAlpha(), out);
	print(value.getRed(), out);
	print(value.getGreen(), out);
	print(value.getBlue(), out);
    }
    
}

