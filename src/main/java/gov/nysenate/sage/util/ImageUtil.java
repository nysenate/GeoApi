package gov.nysenate.sage.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;

public abstract class ImageUtil
{
    private static Logger logger = LogManager.getLogger(ImageUtil.class);

    public static void saveResizedImage(String uri, String format, File path, int height)
    {
        Image image = getImage(uri);
        int origHeight = image.getHeight(null);
        int origWidth = image.getWidth(null);
        int width = (origWidth * height) / origHeight;
        saveResizedImage(image, format, path, width, height);
    }

    public static Image getImage(String uri)
    {
        try {
            logger.info("Retrieving " + uri);
            URL url = new URL(uri);
            return ImageIO.read(url);
        }
        catch (Exception ex) {
            logger.error("Failed to get image " + uri, ex);
        }
        return null;
    }

    public static void saveResizedImage(Image image, String format, File path, int width, int height)
    {
        try {
            logger.info("Resize image to " + width + " x " + height);
            BufferedImage bi = getResizedImage(image, width, height, true);
            ImageIO.write(bi, format, path);
            logger.info("Saved resized image");
        }
        catch (Exception ex) {
            logger.error("Failed to save resized image", ex);
        }
    }

    public static BufferedImage getResizedImage(Image originalImage, int scaledWidth, int scaledHeight, boolean preserveAlpha)
    {
        logger.info("Resizing...");
        int imageType = preserveAlpha ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage scaledBI = new BufferedImage(scaledWidth, scaledHeight, imageType);
        Graphics2D g = scaledBI.createGraphics();
        if (preserveAlpha) {
            g.setComposite(AlphaComposite.Src);
        }
        g.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null);
        g.dispose();
        return scaledBI;
    }
}
