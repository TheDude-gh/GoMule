/*******************************************************************************
 *
 * Copyright 2007 Andy Theuninck & Randall
 *
 * This file is part of gomule.
 *
 * gomule is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * gomule is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * gomlue; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 *
 ******************************************************************************/

package gomule.util;

import java.util.LinkedHashMap;
import java.util.Map;


// loads a d2-style palette
public class D2Palette {
    static private D2Palette p = null;
    private int[] colors;

    private short[][][] remapColors = new short[9][21][256];

    private D2Palette() {
        String s = java.io.File.separator;
        D2BitReader br = new D2BitReader("resources" + s + "palette.dat");
        colors = new int[256];
        for (int i = 0; i < 256; i++) {
            //int blue = (int)br.read(8);
            //int green = (int)br.read(8);
            //int red = (int)br.read(8);
            //colors[i] = (red << 16) + (green << 8) + (blue << 0);
            colors[i] = ((int) br.read(24));
        }

        //also load remap tables
        this.RemapTables();
    }

    private void RemapTables() {
        String s = java.io.File.separator;

        LinkedHashMap<Integer, String> remapTableFiles = new LinkedHashMap<>();

        remapTableFiles.put(2, "grey2.dat");
        remapTableFiles.put(5, "greybrown.dat");
        remapTableFiles.put(8, "invgreybrown.dat");

        for (Map.Entry<Integer, String> en : remapTableFiles.entrySet()) {
            int num = en.getKey();
            String remapTableFile = en.getValue();

            D2BitReader br = new D2BitReader("resources" + s + remapTableFile);
            int rtables_num = br.get_length() / 256; //remap tables count

            rtables_num = Math.max(rtables_num, 21); //maximum we can fit into array

            //remap tables each have 256 bytes
            for (int r = 0; r < rtables_num; r++) {
                for (int i = 0; i < 256; i++) {
                    this.remapColors[num][r][i] = br.getByte();
                }
            }
        }
    }

    static public void D2PaletteInit() {
        if (p == null) {
            //System.err.println("PALETTE INIT!!!");
            p = new D2Palette();
        }
    }

    static public int get_color(int code) {
        if(p == null) p = new D2Palette();
        return p.get(code);
    }

    private int get(int code) {
        return colors[code];
    }

    public static short GetRemapIndex(int num, int table, int index) {
        return p.GetRemapIndexP(num, table, index);
    }

    private short GetRemapIndexP(int num, int table, int index) {
        return this.remapColors[num][table][index];
    }

}
