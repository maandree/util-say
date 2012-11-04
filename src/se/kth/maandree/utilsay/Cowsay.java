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


/**
 * Cowsay support module
 * 
 * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
 */
public class Cowsay extends Ponysay
{
    /**
     * Constructor
     * 
     * @param  flags  Flags passed to the module
     */
    public Cowsay(HashMap<String, String> flags)
    {
	super(Cowsay.modifyFlags(flags));
    }
    
    
    
    /**
     * Import the pony from file
     * 
     * @return  The pony
     */
    protected Pony importCow()
    {
	return null; // TODO implement
    }
    
    
    /**
     * Modify the flags to fit this module
     * 
     * @param   flag  The flags
     * @return        The flags
     */
    private static HashMap<String, String> modifyFlags(HashMap<String, String> flags)
    {
	flags.put("version", "0.1");
	return flags;
    }
    
}

