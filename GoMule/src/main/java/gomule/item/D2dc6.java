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

package gomule.item;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;

import gomule.util.D2BitReader;
import gomule.util.D2Palette;

public class D2dc6 {

    private BufferedImage b;
    private D2BitReader br;
//    private String filename;

    public D2dc6(String f) {
//        filename = f;
        br = new D2BitReader(f);
    }

    public void load_file() {
        if (b == null) {
            return;
        }

        D2Palette.D2PaletteInit();

        br.set_byte_pos(0);
        br.skipBits(128);
        int directions = (int) br.read(32);
        int frames = (int) br.read(32);
        br.skipBits(32 * directions * frames);
//        int flip = (int) br.read(32);
        br.skipBits(32);
//        int width = (int) br.read(32);
        br.skipBits(32);
        int height = (int) br.read(32);
//        int offset_x = (int) br.read(32);
        br.skipBits(32);
//        int offset_y = (int) br.read(32);
        br.skipBits(32);
        br.skipBits(64);
//        int length = (int) br.read(32);
        br.skipBits(32);
        int x = 0;
        int y = height - 1;
        while (y >= 0) {
            int current = (int) br.read(8);
            if (current == 0x80) {
                x = 0;
                y--;
            } else if (current > 0x80) {
                x += (current - 0x80);
            } else {
                while (current-- > 0) {
                    // set the pixel
                    // @ (x,y), color br.read(8)
                    b.setRGB(x++, y, D2Palette.get_color((int) br.read(8)));
                }
            }
        }
    }

    public Image getSingleImage(String InvTransformBase, String InvTransform) {
//        System.err.println( "***** " + br.getFileName() + "*****: " + Color.black.getRGB() );
        br.set_byte_pos(0);
        br.skipBits(128);
        int directions = (int) br.read(32);
        int frames = (int) br.read(32);
        br.skipBits(32 * directions * frames);
//        int flip = (int) br.read(32);
        br.skipBits(32);
        int width = (int) br.read(32);
        int height = (int) br.read(32);
//        int offset_x = (int) br.read(32);
        br.skipBits(32);
//        int offset_y = (int) br.read(32);
        br.skipBits(32);
        br.skipBits(64);
//        int length = (int) br.read(32);
        br.skipBits(32);
        int x = 0;
        int y = height - 1;

        short sInvTransBase = 0;
        if(!InvTransformBase.equals("")) {
             sInvTransBase = Short.parseShort(InvTransformBase);
        }
        short tableIndex = this.GetTableIndex(InvTransform);
        boolean remap = ((sInvTransBase == 2 || sInvTransBase == 5 || sInvTransBase == 8) && tableIndex >= 0);

        D2Palette.D2PaletteInit();
        BufferedImage lImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        while (y >= 0) {
            int current = (int) br.read(8);
            if (current == 0x80) {
                x = 0;
                y--;
            } else if (current > 0x80) {
                x += (current - 0x80);
            } else {
                while (current-- > 0) {
                    // set the pixel
                    // @ (x,y), color br.read(8)
                    int colorIndex = (int) br.read(8);

                    if(remap) {
                        colorIndex = D2Palette.GetRemapIndex(sInvTransBase, tableIndex, colorIndex);
                    }

                    int lColor = D2Palette.get_color(colorIndex);

                    Color lTest = new Color(lColor);
                    lImage.setRGB(x++, y, new Color(lTest.getRed(), lTest.getGreen(), lTest.getBlue(), 255).getRGB());
                }
            }
        }

        return lImage;
    }

    private short GetTableIndex(String InvTransformTable) {
        short tindex = -1;
        switch(InvTransformTable) {
            case "whit": tindex = 1; break;
            case "lgry": tindex = 2; break;
            case "dgry": tindex = 3; break;
            case "blac": tindex = 4; break;
            case "lblu": tindex = 5; break;
            case "dblu": tindex = 6; break;
            case "cblu": tindex = 7; break;
            case "lred": tindex = 8; break;
            case "dred": tindex = 9; break;
            case "cred": tindex = 10; break;
            case "lgrn": tindex = 11; break;
            case "dgrn": tindex = 12; break;
            case "cgrn": tindex = 13; break;
            case "lyel": tindex = 14; break;
            case "dyel": tindex = 15; break;
            case "lgld": tindex = 16; break;
            case "dgld": tindex = 17; break;
            case "lpur": tindex = 18; break;
            case "dpur": tindex = 19; break;
            case "oran": tindex = 20; break;
            case "bwht": tindex = 21; break;
            default: return -1;
        }
        tindex--; //decrement, because array starts from zero
        return tindex;
    }

    public void set_image(BufferedImage bi) {
        b = bi;
    }

    public void test() {

    }


}