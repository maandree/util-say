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

import java.awt.*;
import java.util.*;


/**
 * Platform submodule interface for {@link Ponysay}
 * 
 * @author  Mattias Andrée, <a href="mailto:maandree@member.fsf.org">maandree@member.fsf.org</a>
 */
public abstract class PonysaySubmodule
{
    /**
     * Initialise the import
     * 
     * @param  colours  The supermodule's colour palette
     */
    public abstract void initImport(Color[] colours);
    
    /**
     * Initialise the export and return a string used to reset the colour palette
     * 
     * @param   colours  The supermodule's colour palette
     * @return           String to print to reset the colour palette
     */
    public abstract String initExport(Color[] colours);
    
    /**
     * Get ANSI colour sequence to append to the output
     * 
     * @param  palette        The current colour palette
     * @param  oldBackground  The current background colour
     * @param  oldForeground  The current foreground colour
     * @param  oldFormat      The current text format
     * @param  newBackground  The new background colour
     * @param  newForeground  The new foreground colour
     * @param  newFormat      The new text format
     */
    public abstract String applyColour(Color[] palette, Color oldBackground, Color oldForeground, boolean[] oldFormat, Color newBackground, Color newForeground, boolean[] newFormat);
    
    /**
     * Parse escape sequences
     * 
     * @param   c           The current character in the parsing
     * @param   background  The current background colour
     * @param   foreground  The current foreground colour
     * @param   format      The current format, may be updated in-place
     * @param   colours     The current palette, may be updated in-place
     * @return              The background colour, the foreground colour and state: {@code {background, foreground, Boolean.TRUE|Boolean.FALSE}},
     *                      wheere the state is true as along as the escape parsing has not completed
     */
    public abstract Object[] parseEscape(int c, Color background, Color foreground, boolean[] format, Color[] colours);
    
    
    
    /**
     * Colour CIELAB value cache
     */
    private static ThreadLocal<HashMap<Color, double[]>> labMap = new ThreadLocal<HashMap<Color, double[]>>();
    
    /**
     * Chroma weight using in {@link #labMap}
     */
    private static ThreadLocal<Double> labMapWeight = new ThreadLocal<Double>();
    
    
    
    /**
     * Get the closest matching colour
     * 
     * @param   colour        The colour to match
     * @param   palette       The palette for which to match
     * @param   paletteStart  The beginning of the usable part of the palette
     * @param   paletteEnd    The exclusive end of the usable part of the palette
     * @param   chromaWeight  The chroma weight, negative for sRGB distance
     * @return                The index of the closest colour in the palette
     */
    protected static int matchColour(Color colour, Color[] palette, int paletteStart, int paletteEnd, double chromaWeight)
    {
	if (chromaWeight < 0.0)
	{
	    int bestI = paletteStart;
	    int bestD = 4 * 256 * 256;
	    for (int i = paletteStart; i < paletteEnd; i++)
	    {
		int ðr = colour.getRed()   - palette[i].getRed();
		int ðg = colour.getGreen() - palette[i].getGreen();
		int ðb = colour.getBlue()  - palette[i].getBlue();
		
		int ð = ðr*ðr + ðg*ðg + ðb*ðb;
		if (bestD > ð)
		{   bestD = ð;
		    bestI = i;
		}
	    }
	    return bestI;
	}
	
	Double _chroma = labMapWeight.get();
	HashMap<Color, double[]> _labMap = ((_chroma == null) || (_chroma.doubleValue() != chromaWeight)) ? null : labMap.get();
	if (_labMap == null)
	{   labMap.set(_labMap = new HashMap<Color, double[]>());
	    labMapWeight.set(new Double(chromaWeight));
	}
	
	double[] lab = _labMap.get(colour);
	/* if (lab == null) */ // FIXME Why does this not work!?
	    _labMap.put(colour, lab = Colour.toLab(colour.getRed(), colour.getGreen(), colour.getBlue(), chromaWeight));
	double L = lab[0], a = lab[1], b = lab[2];
	
	int bestI = -1;
	double bestD = 0.0;
	Color p;
	for (int i = paletteStart; i < paletteEnd; i++)
	{
	    double[] tLab = _labMap.get(p = palette[i]);
	    /* if (tLab == null) */ // FIXME Why does this not work!?
		_labMap.put(colour, tLab = Colour.toLab(p.getRed(), p.getGreen(), p.getBlue(), chromaWeight));
	    double ðL = L - tLab[0];
	    double ða = a - tLab[1];
	    double ðb = b - tLab[2];
	    
	    double ð = ðL*ðL + ða*ða + ðb*ðb;
	    if ((bestD > ð) || (bestI < 0))
	    {   bestD = ð;
		bestI = i;
	    }
	}
	
	return bestI;
    }
}

