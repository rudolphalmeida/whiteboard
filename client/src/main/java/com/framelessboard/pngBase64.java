package com.framelessboard;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Base64;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;


public class pngBase64 {



    public String pngToString(String filePath) throws FileNotFoundException{
        String encodedString = null;
        try{
            byte[] fileContent = FileUtils.readFileToByteArray(new File(filePath));
            encodedString = Base64.getEncoder().encodeToString(fileContent);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw e;
        } catch (IOException e) {
            e.printStackTrace();
        }
        //byte[] decodedBytes = Base64.getDecoder().decode(encodedBytes);
        //System.out.println("decodedBytes " + new String(decodedBytes));
        return encodedString;
    }

    public void decodeToImage(String imageString, String filePath) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(imageString);
            FileUtils.writeByteArrayToFile(new File(filePath), decodedBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main( String[] args ){

        pngBase64 png = new pngBase64();
        String test = null;
        try{
            test = png.pngToString("C:/Users/IF/Pictures/wallhaven.png");
            System.out.println(test);
            png.decodeToImage(test, "C:/Users/IF/Pictures/test.png");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
}
