package com.dzcx.core.log.utils;


import android.text.TextUtils;
import android.util.Log;

import com.dzcx.core.log.bean.MsgModel;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by chen3 on 2017/9/14.
 */
public class LogFileUtils {

    public static String copy(String origin, String destDir, String fileName) {
        File originFile = new File(origin);
        if (originFile.exists()) {
            FileInputStream  fis = null;
            FileOutputStream fos = null;
            try {
                fis = new FileInputStream(originFile);
                String filePath = destDir + "/" + fileName;
                fos = new FileOutputStream(filePath);
                int len = 0;
                byte[] buf = new byte[4 * 1024];
                while ((len = fis.read(buf)) != -1) {
                    fos.write(buf, 0, len);
                }
                fos.flush();
                return filePath;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fis != null) fis.close();
                    if (fos != null) fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 把log日志写入到文件中
     *
     * @param list     log的集合
     * @param dirPath  保存文件的文件夹的路径
     * @param fileName 保存的文件的名字
     * @return 保存的文件的路径，如果保存失败为null
     */
    public static String writeLogToFile(ArrayList<MsgModel> list, String dirPath, String fileName) {
        FileWriter writer = null;
        BufferedWriter bufferedWriter = null;
        try {
            File file = new File(dirPath, fileName);
            writer = new FileWriter(file);
            bufferedWriter = new BufferedWriter(writer);
            if (list == null || list.size() == 0) {
                bufferedWriter.write("no data for this taxi");
                bufferedWriter.newLine();
            } else {
                for (MsgModel model : list) {
                    bufferedWriter.write(model.toString());
                    bufferedWriter.newLine();
                }
            }
            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedWriter != null) bufferedWriter.close();
                if (writer != null) writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 压缩文件,单个文件的压缩；如果压缩成功会删除源文件
     *
     * @param filePath
     * @return
     */
    public static String compressToZip(String filePath) {
        return compressToZip(filePath, null);
    }

    /**
     * 压缩文件
     *
     * @param files         需要压缩的文件
     * @param dirPath       保存的路径
     * @param storeFileName 保存的文件名
     * @return
     */
    public static String compressToZip(File[] files, String dirPath, String storeFileName) {
        File outFile = new File(dirPath, (TextUtils.isEmpty(storeFileName) ? System.currentTimeMillis() : storeFileName) + ".zip");
        try {
            FileOutputStream fos = new FileOutputStream(outFile);
            ZipOutputStream zos = new ZipOutputStream(fos);
            BufferedOutputStream bos = new BufferedOutputStream(zos);
            for (File file : files) {
                Log.i("chen", "开始压缩文件---->" + file.getName() + ",length:" + file.length());
                zos.putNextEntry(new ZipEntry(file.getName()));
                FileInputStream     fis = new FileInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(fis);
                int length = 0;
                byte[] buffer = new byte[1024];
                while ((length = bis.read(buffer)) != -1) {
                    bos.write(buffer, 0, length);
                }
                bis.close();
                fis.close();
                bos.flush();
            }
            zos.closeEntry();
            Log.i("chen", "压缩文件成功1");
            bos.close();
            Log.i("chen", "压缩文件成功2");
            zos.close();
            Log.i("chen", "压缩文件成功3");
            return outFile.getAbsolutePath();
        } catch (Exception e) {
//            ALog.i("chen","压缩失败："+e.getMessage()+","+e.getClass());
        }
        return null;
    }

    /**
     * 压缩文件
     *
     * @param filePath 需要压缩的文件
     * @param dirPath  压缩到那个目录，如果为空 则压缩到压缩文件所在的文件夹
     * @return
     */
    public static String compressToZip(String filePath, String dirPath) {
        File   file   = new File(filePath);
        String parent = TextUtils.isEmpty(dirPath) ? file.getParent() : dirPath;
        return compressToZip(new File[]{file}, parent, file.getName().split("\\.")[0]);
    }

}
