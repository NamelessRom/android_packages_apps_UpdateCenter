package org.namelessrom.updatecenter.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

/**
 * Created by alex on 22.06.14.
 */
public class FileUtils {

    public static void writeToFile(final File file, final String content) throws Exception {
        if (file == null) throw new Exception("File is null!");
        if (file.exists()) {
            file.delete();
        }
        file.getParentFile().mkdirs();

        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            fw = new FileWriter(file);
            bw = new BufferedWriter(fw);
            bw.write(content);
        } finally {
            if (bw != null) bw.close();
            if (fw != null) fw.close();
        }
    }

    public static String readFromFile(final File file) throws Exception {
        if (file == null) throw new Exception("File is null!");

        StringBuilder sb = new StringBuilder();
        FileReader fr = null;
        BufferedReader br = null;
        try {
            fr = new FileReader(file);
            br = new BufferedReader(fr);
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
        } finally {
            if (br != null) br.close();
            if (fr != null) fr.close();
        }

        return sb.toString();
    }

}
