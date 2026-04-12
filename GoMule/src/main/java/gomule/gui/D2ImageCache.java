/*******************************************************************************
 *
 * Copyright 2007 Randall
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
package gomule.gui;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import gomule.item.D2Item;
import gomule.item.D2dc6;

/**
 * @author Marco
 * <p>
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class D2ImageCache {
    private static HashMap sImages = new HashMap();
    private static HashMap sDC6Images = new HashMap();
    private static HashMap sIcon = new HashMap();

    public static Image getImage(String pImageName) {
        return getImageAbsolute("resources" + File.separator + pImageName);
    }

    private static Image getImageAbsolute(String pImageName) {
        if (sImages.containsKey(pImageName)) {
            return (Image) sImages.get(pImageName);
        }

        Image lImage = null;
        try {
            Image lLoadImage = ImageIO.read(new java.io.File(pImageName));
            new ImageIcon(lLoadImage);

            lImage = new BufferedImage(lLoadImage.getWidth(null), lLoadImage.getHeight(null), BufferedImage.TYPE_3BYTE_BGR);
            Graphics2D lGraphics = (Graphics2D) lImage.getGraphics();
            lGraphics.drawImage(lLoadImage, 0, 0, null);
            lGraphics.dispose();
        } catch (IOException pEx) {
            lImage = null;
        }
        sImages.put(pImageName, lImage);

        return lImage;
    }

    public static Icon getIcon(String pIconName) {
        return getIconAbsolute("resources" + File.separator + "icons" + File.separator + pIconName);
    }

    private static Icon getIconAbsolute(String pImageName) {
        if (sIcon.containsKey(pImageName)) {
            return (Icon) sIcon.get(pImageName);
        }

        Icon lIcon = null;
        try {
            Image lLoadImage = ImageIO.read(new java.io.File(pImageName));
            lIcon = new ImageIcon(lLoadImage);
        } catch (IOException pEx) {
            lIcon = null;
        }
        sIcon.put(pImageName, lIcon);

        return lIcon;
    }

    public static Image getDC6Image(D2Item pItem) {
        return getDC6Image(pItem.get_image(), pItem.InvTransformBase, pItem.InvTransform);
    }

    public static Image getDC6Image(String pImageName, String InvTransformBase, String InvTransform) {
        String gfxPath = "resources" + File.separator + "gfx" + File.separator;
        String lFileName = gfxPath + pImageName + ".dc6";
        String outFileName = gfxPath + pImageName;
        String pngFileName = pImageName;

        if(InvTransform.equals("")) {
            outFileName += ".dc6";
            pngFileName += ".png";
        }
        else {
            outFileName += "_" + InvTransformBase + InvTransform + ".dc6";
            pngFileName += "_" + InvTransformBase + InvTransform + ".png";
        }

        String pngFilePath = "images" + File.separator + pngFileName;

        if (sDC6Images.containsKey(outFileName)) {
            return (Image) sDC6Images.get(outFileName);
        }

        //try to get png file
        File pngfile = new File(pngFilePath);
        if(pngfile.exists()) {
            try {
                Image pngImage = ImageIO.read(pngfile);
                sDC6Images.put(outFileName, pngImage);
                return pngImage;
            }
            catch(IOException e) {}
        }

        //check if we get DC6 file. If not, try  base PNG image
        File dc6file = new File(lFileName);
        if(!dc6file.exists()) {
            System.err.println("Falling to ?. No PNG or DC6 for " + pngFileName);
            File pngfileF = new File("images" + File.separator + pImageName + ".png");
            if (!pngfileF.exists()) {
                pngfile = new File("images" + File.separator + "_fallback.png");
            }
            if (pngfileF.exists()) {
                try {
                    Image pngImage = ImageIO.read(pngfileF);
                    sDC6Images.put(outFileName, pngImage);
                    return pngImage;
                } catch (IOException e) {}
            }
        }

        //get DC6 image
        D2dc6 lD2S = new D2dc6(lFileName);
        Image lImage = lD2S.getSingleImage(InvTransformBase, InvTransform);
        sDC6Images.put(outFileName, lImage);

        //save DC6 iamge to PNG for future use
        if (!pngfile.exists()) {
            File out = new File(pngFilePath);
            try {
                ImageIO.write((BufferedImage)lImage, "png", out);
            }
            catch(IOException e) {

            }
        }

        return lImage;
    }


}
