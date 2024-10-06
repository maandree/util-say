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
import java.util.*;


/**
 * <p><tt>cat<tt> printable pony module</p>
 * <p>
 *   Note that the <tt>cat</tt>-printablilty may be disrupted by stores and recalls!
 * </p>
 * 
 * @author  Mattias Andrée, <a href="mailto:m@maandree.se">m@maandree.se</a>
 */
public class Cat extends Ponysay
{
    /**
     * Constructor
     * 
     * @param  flags  Flags passed to the module
     */
    public Cat(HashMap<String, String> flags)
    {
	super(Cat.modifyFlags(flags));
    }
    
    
    /**
     * Modify the flags to fit this module
     * 
     * @param   flag  The flags
     * @return        The flags
     */
    private static HashMap<String, String> modifyFlags(HashMap<String, String> flags)
    {
	flags.put("ignoreballoon", "y");
	flags.put("ignorelink", "y");
	flags.put("balloon", "-");
	flags.put("version", "2.8");
	return flags;
    }
    
}

