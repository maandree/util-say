//####################################################################################
//##  The following code is pasted from TWT, but the class is made package private  ##
//##  and the package has been changed to  se.kth.maandree.utilsay                  ##
//##  toLab(...) as also been publicised.                                           ##
//####################################################################################


/**
 * TWT — Terminal Window Toolkit, a free pure Java terminal toolkit.
 * Copyright (C) 2011 Mattias Andrée <m@maandree.se>
 *
 * This library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.kth.maandree.utilsay;


/**
 * Terminal colour class
 * 
 * @author  Mattias Andrée, <a href="mailto:m@maandree.se">m@maandree.se</a>
 */
public class Colour
{
    /**
     * Possible colour intensitivities on mixed colours
     */
    public static final int[] COLOUR_INTENSITIVITY = {0, 95, 135, 175, 215, 255};
    
    /**
     * Possible intensitivities on grey colours, excluding the mixed colours' intensitivities
     */
    public static final int[] GREY_EXTRA_INTENSITIVITY = {8, 18, 28, 38, 48, 58, 68, 78, 88, 98, 108, 118, 128,
                                                          138, 148, 158, 168, 178, 188, 198, 208, 218, 228, 238};
    
    /**
     * Possible intensitivities on mixed colours, including the mixed colours' intensitivities
     */
    public static final int[] GREY_FULL_INTENSITIVITY = {0, 8, 18, 28, 38, 48, 58, 68, 78, 88, 95, 98, 108, 118, 128, 135,
                                                         138, 148, 158, 168, 175, 178, 188, 198, 208, 218, 215, 228, 238, 255};
    
    
    
    /**
     * <p>Constructor</p>
     * <p>
     *     Selects the colour by index
     * </p>
     *
     * @param  index  The colour's index [0–255]
     */
    @SuppressWarnings("hiding")
    public Colour(final byte index)
    {
        final int[] I = COLOUR_INTENSITIVITY;
	
        int i = index;  if (i < 0)  i += 1 << 8;
	
        if ((this.index = i) < 16)
        {
            this.systemColour = true;
            this.red   = new int[] {  0, 128,   0, 128,   0, 128,   0, 192,
                                    128, 255,   0, 255,   0, 255,   0, 255} [i];
            this.green = new int[] {  0,   0, 128, 128,   0,   0, 128, 192,
                                    128,   0, 255, 255,   0,   0, 255, 255} [i];
            this.blue  = new int[] {  0,   0,   0,   0, 128, 128, 128, 192,
                                    128,   0,   0,   0, 255, 255, 255, 255} [i];
        }
        else
        {
            this.systemColour = false;
            if (i < 232)
            {
                final int j = i - 16, b, g;
                this.blue  = COLOUR_INTENSITIVITY[b = j % 6];
                this.green = COLOUR_INTENSITIVITY[g = ((j - b) / 6) % 6];
                this.red   = COLOUR_INTENSITIVITY[(j - b - g * 6) / (6 * 6)];
            }
            else
                this.red = this.green = this.blue = (i - 232) * 10 + 8;
        }
    }
    
    /**
     * <p>Constructor</p>
     * <p>
     *     Selects the colour the closest the a proper terminal colour
     * </p>
     *
     * @param  red    The red   intensity [0–255]
     * @param  green  The green intensity [0–255]
     * @param  blue   The blue  intensity [0–255]
     */
    @SuppressWarnings("hiding")
    public Colour(final byte red, final byte green, final byte blue)
    {
        final int[] I = COLOUR_INTENSITIVITY;
	
        int r = red  ;  if (r < 0)  r += 1 << 8;
        int g = green;  if (g < 0)  g += 1 << 8;
        int b = blue ;  if (b < 0)  b += 1 << 8;
	
        int d, ð, dr, db, dg; dr = db = dg = 0;
	
        int ir = -1, ig = -1, ib = -1, ii = -1;
	
        d = 500; for (int cr : I) if (d > (ð = Math.abs(cr - r))) {d = ð; dr = cr; ir++;} else break;
        d = 500; for (int cg : I) if (d > (ð = Math.abs(cg - g))) {d = ð; dg = cg; ig++;} else break;
        d = 500; for (int cb : I) if (d > (ð = Math.abs(cb - b))) {d = ð; db = cb; ib++;} else break;
	
        d = (dr - r)*(dr - r) + (dg - g)*(dg - g) + (db - b)*(db - b);
	
        for (int gr = 8; gr <= 238; gr += 10)
        {
            int ðr = gr - r;
            int ðg = gr - g;
            int ðb = gr - b;
	    
            ð = ðr*ðr + ðg*ðg + ðb*ðb;
	    
            if (d > ð)
            {
                d = ð;
                dr = gr;
                dg = gr;
                db = gr;
                ii = (gr - 8) / 10;
            }
        }
        
        this.systemColour = false;
        this.red   = dr;
        this.green = dg;
        this.blue  = db;
        this.index = ii < 0 ? (16 + (ir * 6 + ig) * 6 + ib) : ii + 232;
    }
    
    /**
     * <p>Constructor</p>
     * <p>
     *     Selects the colour by index
     * </p>
     *
     * @param  index  The colour's index [0–255]
     */
    @SuppressWarnings("hiding")
    public Colour(final int index)
    {
        this((byte)index);
    }

    /**
     * <p>Constructor</p>
     * <p>
     *     Selects the colour the closest the a proper terminal colour
     * </p>
     *
     * @param  red    The red   intensity [0–255]
     * @param  green  The green intensity [0–255]
     * @param  blue   The blue  intensity [0–255]
     */
    @SuppressWarnings("hiding")
    public Colour(final int red, final int green, final int blue)
    {
        this((byte)red, (byte)green, (byte)blue);
    }
    
    /**
     * <p>Constructor</p>
     * <p>
     *     Selects the colour the closest the a proper terminal colour
     * </p>
     *
     * @param  red           The red   intensity [0–255]
     * @param  green         The green intensity [0–255]
     * @param  blue          The blue  intensity [0–255]
     * @param  chromaWeight  The weight of chromaticity [0–∞[, 1 is unweighted
     */
    @SuppressWarnings("hiding")
    public Colour(final int red, final int green, final int blue, final double chromaWeight)
    {
	if ((labs == null) || (chromaWeight != lastCW))
	{
	    if (labs == null)
		labs = new double[240][];
	    for (int b = 0; b < 6; b++)
		for (int g = 0; g < 6; g++)
		    for (int r = 0; r < 6; r++)
			labs[r * 36 + g * 6 + b] = toLab(COLOUR_INTENSITIVITY[r],
							 COLOUR_INTENSITIVITY[g],
							 COLOUR_INTENSITIVITY[b],
							 chromaWeight);
	    
	    for (int s = 0; s < 24; s++)
		labs[216 + s] = toLab(GREY_EXTRA_INTENSITIVITY[s],
				      GREY_EXTRA_INTENSITIVITY[s],
				      GREY_EXTRA_INTENSITIVITY[s],
				      chromaWeight);
	}
	
	final double[] lab = toLab(red, green, blue, chromaWeight);
	final double L = lab[0], a = lab[1], b = lab[2];
	
	double d = -100.;
	int best = 0;
	
        for (int i = 0; i < 240; i++)
        {
	    final double[] tLab = labs[i];
            double ðL = L - tLab[0];
            double ða = a - tLab[1];
            double ðb = b - tLab[2];
	    
            double ð = ðL*ðL + ða*ða + ðb*ðb;
	    
            if ((d > ð) || (d < -50.))
            {
                d = ð;
		best = i;
            }
        }
	
	final Colour that = new Colour(best + 16);
	
	this.red   = that.red;
	this.green = that.green;
	this.blue  = that.blue;
	this.index = that.index;
	this.systemColour = that.systemColour;
    }
    
    
    
    /**
     * The red intensity [0–255]
     */
    public final int red;
    
    /**
     * The green intensity [0–255]
     */
    public final int green;
    
    /**
     * The blue intensity [0–255]
     */
    public final int blue;
    
    /**
     * The colour's index [0–255]
     */
    public final int index;
    
    /**
     * Whether the colour is a system colour
     */
    public final boolean systemColour;
    
    
    /**
     * The static colours converted to CIELAB
     */
    private static double[][] labs = null;
    
    
    /**
     * The chroma weight used when creating {@link #labs}
     */
    private static double lastCW = 0;
    
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        return this.index;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o)
    {
        if ((o == null) || !(o instanceof Colour))
            return false;
	
        return ((Colour)o).index == this.index;
    }
    
    
    /**
     * Converts from sRGB to CIELAB
     * 
     * @param   red           The red   intensity [0–255]
     * @param   green         The green intensity [0–255]
     * @param   blue          The blue  intensity [0–255]
     * @param   chromaWeight  The weight of chromaticity [0–∞[, 1 is unweighted
     * @return                {L*, a*, b}
     */
    public static double[] toLab(final int red, final int green, final int blue, final double chromaWeight)
    {
        int ir = red  ;  if (ir < 0)  ir += 1 << 8;
        int ig = green;  if (ig < 0)  ig += 1 << 8;
        int ib = blue ;  if (ib < 0)  ib += 1 << 8;
	
	double r = ir / 255.;  r = r <= 0.4045 ? r / 12.92 : Math.pow((r + 0.055) / 1.055, 2.4);
	double g = ig / 255.;  g = g <= 0.4045 ? g / 12.92 : Math.pow((g + 0.055) / 1.055, 2.4);
	double b = ib / 255.;  b = b <= 0.4045 ? b / 12.92 : Math.pow((b + 0.055) / 1.055, 2.4);
	
	double x = (0.4124564 * r + 0.3575761 * g + 0.1804375 * b) / 0.95047;
	double y = (0.2126729 * r + 0.7151522 * g + 0.0721750 * b);
	double z = (0.0193339 * r + 0.1191920 * g + 0.9503041 * b) / 1.08883;
	
	x = x > 0.00885642 ? Math.pow(x, 1. / 3.) : (7.78 + 703. / 99900.) * x + 0.1379310;
	y = y > 0.00885642 ? Math.pow(y, 1. / 3.) : (7.78 + 703. / 99900.) * y + 0.1379310;
	z = z > 0.00885642 ? Math.pow(z, 1. / 3.) : (7.78 + 703. / 99900.) * z + 0.1379310;
	
	final double rcL = 116 * y - 16;
	final double rca = 500 * (x - y) * chromaWeight;
	final double rcb = 200 * (y - z) * chromaWeight;
	
	return new double[] {rcL, rca, rcb};
    }
    
    
    
    /**
     * System colour initialisation index counter
     */
    private static byte sciic = 0;
    
    
    /**
     * System colour: black
     */
    public static final Colour SYSTEM_BLACK   = new Colour(sciic++);
    
    /**
     * System colour: medium red
     */
    public static final Colour SYSTEM_RED     = new Colour(sciic++);
    
    /**
     * System colour: medium green
     */
    public static final Colour SYSTEM_GREEN   = new Colour(sciic++);
    
    /**
     * System colour: medium yellow, dark orange or brown
     */
    public static final Colour SYSTEM_YELLOW  = new Colour(sciic++);
    
    /**
     * System colour: medium blue
     */
    public static final Colour SYSTEM_BLUE    = new Colour(sciic++);
    
    /**
     * System colour: medium magenta or medium lilac
     */
    public static final Colour SYSTEM_MAGENTA = new Colour(sciic++);
    
    /**
     * System colour: medium cyan or medium turquoise
     */
    public static final Colour SYSTEM_CYAN    = new Colour(sciic++);
    
    /**
     * System colour: dark grey
     */
    public static final Colour SYSTEM_GREY    = new Colour(sciic++);
    
    /**
     * System colour: light grey
     */
    public static final Colour SYSTEM_INTENSIVE_BLACK   = new Colour(sciic++);
    
    /**
     * System colour: light red
     */
    public static final Colour SYSTEM_INTENSIVE_RED     = new Colour(sciic++);
    
    /**
     * System colour: light green
     */
    public static final Colour SYSTEM_INTENSIVE_GREEN   = new Colour(sciic++);
    
    /**
     * System colour: light yellow or medium orange
     */
    public static final Colour SYSTEM_INTENSIVE_YELLOW  = new Colour(sciic++);
    
    /**
     * System colour: light blue
     */
    public static final Colour SYSTEM_INTENSIVE_BLUE    = new Colour(sciic++);
    
    /**
     * System colour: light magenta or light lilac
     */
    public static final Colour SYSTEM_INTENSIVE_MAGENTA = new Colour(sciic++);
    
    /**
     * System colour: light cyan or light turquoise
     */
    public static final Colour SYSTEM_INTENSIVE_CYAN    = new Colour(sciic++);
    
    /**
     * System colour: white
     */
    public static final Colour SYSTEM_INTENSIVE_GREY    = new Colour(sciic++);
    
    
    /**
     * System independent colour: pitch black
     */
    public static final Colour PURE_BLACK   = new Colour(0, 0, 0);
    
    /**
     * System independent colour: medium red
     */
    public static final Colour PURE_RED     = new Colour(175, 0, 0);
    
    /**
     * System independent colour: medium green
     */
    public static final Colour PURE_GREEN   = new Colour(0, 175, 0);
    
    /**
     * System independent colour: medium yellow
     */
    public static final Colour PURE_YELLOW  = new Colour(175, 175, 0);
    
    /**
     * System independent colour: medium blue
     */
    public static final Colour PURE_BLUE    = new Colour(0, 0, 175);
    
    /**
     * System independent colour: medium magenta
     */
    public static final Colour PURE_MAGENTA = new Colour(175, 0, 175);
    
    /**
     * System independent colour: medium cyan
     */
    public static final Colour PURE_CYAN    = new Colour(0, 175, 175);
    
    /**
     * System independent colour: dark grey
     */
    public static final Colour PURE_GREY    = new Colour(198, 192, 192);
    
    /**
     * System independent colour: light grey
     */
    public static final Colour PURE_INTENSIVE_BLACK   = new Colour(127, 128, 128);
    
    /**
     * System independent colour: light red
     */
    public static final Colour PURE_INTENSIVE_RED     = new Colour(255, 0, 0);
    
    /**
     * System independent colour: light green
     */
    public static final Colour PURE_INTENSIVE_GREEN   = new Colour(0, 255, 0);
    
    /**
     * System independent colour: light yellow
     */
    public static final Colour PURE_INTENSIVE_YELLOW  = new Colour(255, 255, 0);
    
    /**
     * System independent colour: light blue
     */
    public static final Colour PURE_INTENSIVE_BLUE    = new Colour(0, 0, 255);
    
    /**
     * System independent colour: light magenta
     */
    public static final Colour PURE_INTENSIVE_MAGENTA = new Colour(255, 0, 255);
    
    /**
     * System independent colour: light cyan
     */
    public static final Colour PURE_INTENSIVE_CYAN    = new Colour(0, 255, 255);
    
    /**
     * System independent colour: pure white
     */
    public static final Colour PURE_INTENSIVE_GREY    = new Colour(0, 255, 255);
    
}

