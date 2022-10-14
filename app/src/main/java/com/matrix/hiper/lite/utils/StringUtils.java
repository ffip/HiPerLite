package com.matrix.hiper.lite.utils;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

public class StringUtils {

    public static String getStringFromFile(String path) {
        try {
            FileInputStream inputStream = new FileInputStream(path);
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            inputStream.close();
            return new String(bytes);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static void writeFile(String path,String string) {
        try {
            String parent = new File(path).getParent();
            FileUtils.createDirectory(parent);
            FileUtils.createFile(path);
            FileWriter fileWriter = new FileWriter(path);
            fileWriter.write(string);
            fileWriter.close();
        }
        catch (IOException e) {
            e.printStackTrace();
            Log.e("writeFile",e.toString());
        }
    }

}
