package com.matrix.hiper.lite.utils;

import java.io.File;
import java.io.IOException;

public class FileUtils {

    public static boolean createDirectory(String path) {
        if (!new File(path).exists()){
            return new File(path).mkdirs();
        }
        return true;
    }

    public static boolean createFile(String path) throws IOException {
        if (!new File(path).exists()){
            return new File(path).createNewFile();
        }
        return true;
    }

    public static boolean deleteDirectory(String path) {
        try{
            File dirFile = new File(path);
            if (!dirFile.exists()) {
                return true;
            }
            if (dirFile.isFile()) {
                return dirFile.delete();
            }
            File[] files = dirFile.listFiles();
            if(files == null) {
                return false;
            }
            for (File file : files) {
                deleteDirectory(file.toString());
            }
            return dirFile.delete();
        } catch(Exception e) {
            return false;
        }
    }

}
