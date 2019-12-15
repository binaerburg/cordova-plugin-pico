package com.palette.picoio.demo;

import com.palette.picoio.color.LAB;
import com.palette.picoio.color.Swatch;

import java.util.ArrayList;
import java.util.List;

/**
 * This class provides a mock list of colors to demonstrate matching.
 */
public class ExampleColorDbProvider
{
    private ExampleColorDbProvider()
    {
        throw new AssertionError();
    }

    public static List<Swatch> getSampleColors()
    {
        List<Swatch> swatches = new ArrayList<>();

        swatches.add(new Swatch("Orange", "A", new LAB(62.966542929419, 58.1009869314696, 66.6022417932114)));
        swatches.add(new Swatch("Red", "B", new LAB(44.9512479211058, 42.9550636736008, 34.5789150804718)));
        swatches.add(new Swatch("Blue", "C", new LAB(46.4326794509501, -4.91999247990771, -46.1209053819398)));
        swatches.add(new Swatch("Green", "D", new LAB(69.0594692695158, -32.2304850310249, 53.5603525426951)));
        swatches.add(new Swatch("Grey", "E", new LAB(31.5130852290379, 2.28639746047626, 0.592541444296391)));
        swatches.add(new Swatch("Black", "F", new LAB(25.1042112878437, 0.292026771056303, 2.40698876603797)));
        swatches.add(new Swatch("Purple", "G", new LAB(38.3519350654045, 12.1653856460046, -11.7184394987654)));
        swatches.add(new Swatch("Brown", "H", new LAB(33.1952918599344, 6.05497130641686, 4.63902305326908)));

        return swatches;
    }
}
