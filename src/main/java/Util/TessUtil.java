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

    public static String getDropPositionCoords(BufferedImage mapImg, String location) {
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath(Paths.get("src", "main", "resources", "trainedData").toAbsolutePath().toString());
        tesseract.setTessVariable("user_defined_dpi", "500");
        tesseract.setTessVariable("tessedit_char_whitelist", "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        mapImg = ImageHelper.convertImageToBinary(mapImg);
        tesseract.setPageSegMode(11);
        Rectangle rect = new Rectangle();

        String results = "";
        try {
            results = tesseract.doOCR(mapImg);
            System.out.println(results);

        } catch (TesseractException e) {
            e.printStackTrace();
        }
//        for (Word word : tesseract.getWords(mapImg, ITessAPI.TessPageIteratorLevel.RIL_WORD)) {
//            if (word.getText().equalsIgnoreCase(location))
//            rect = word.getBoundingBox();
//            break;
//        }
        return results;
    }
}
