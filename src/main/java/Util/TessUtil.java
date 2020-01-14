package Util;

import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.Word;
import net.sourceforge.tess4j.util.ImageHelper;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.Paths;

public class TessUtil {

    public static Point getDropPositionCoords(BufferedImage mapImg, String location) {
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath(Paths.get("src", "main", "resources", "trainedData").toAbsolutePath().toString());
        tesseract.setTessVariable("user_defined_dpi", "300");
        Rectangle rect = new Rectangle();

        mapImg = ImageHelper.getScaledInstance(mapImg, 1000, 1000);
        try {
            System.out.println(tesseract.doOCR(mapImg));
        } catch (TesseractException e) {
            e.printStackTrace();
        }
//        for (Word word : tesseract.getWords(mapImg, ITessAPI.TessPageIteratorLevel.RIL_WORD)) {
//            if (word.getText().equalsIgnoreCase(location))
//            rect = word.getBoundingBox();
//            break;
//        }
        return rect.getLocation();
    }
}
