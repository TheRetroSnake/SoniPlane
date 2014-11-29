package gs.soni.plane.util;

import gs.soni.plane.project.map;

import java.awt.*;

public class impl {

    public interface paletteImpl {

        // input: direct string to the palette file
        // output: Color[line][entry]
        public Color[][] load(String input);    // load palette

        // input: Color[line][entry]
        // output: byte array of data to save a file
        public byte[] save(Color[][] input);    // save palette

        // input ColorEntry color (color to be converted)
        // output: floating point to closest valid color
        public int grid(int color);

        // (int value is member of normal format of colors on original system (Check MD source for example of function))
        public int convert(int in);           // convert Color value to int value

    }

    public interface tileImpl {

        // returns tile width
        public int GetWidth();

        // returns tile height
        public int GetHeight();

        // input: direct string to the tile file
        // output: int[tileID][data]
        public int[][] load(String file);

        // input: int[tileID][data]
        // output: byte array of data to save a file
        public byte[] save(int[][] input);    // save palette
    }

    public interface mapImpl {

        // input: direct string to the tile file
        // output: gs.soni.plane.project.map array of mappings
        public map[] load(String input);

        // input: gs.soni.plane.project.map array of mappings
        // output: byte array of data to save a file, offset of tiles
        public byte[] save(map[] input, int offset);
    }
}
