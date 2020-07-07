package com.yc.httpserver;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class AssetsUtils {

    /**
     * 读取assets文件转InputStream
     */
    public static InputStream getAssetsToInp(String assetsName) {
        try {
            return HttpServerUtils.getAppContext().getAssets().open(assetsName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 读取assets文件内容
     */
    public static String getAssetsToString(String assetsName) {
        try {
            return FFileUtils.readInp(HttpServerUtils.getAppContext().getAssets().open(assetsName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取assets下所有文件路径，assets下的文件目录不能有.(点) 符号
     */
    public static Map<String, String> getAssetsLs(String assetsPath, String fileNameFilter) {
        Map<String, String> maps = new HashMap<>();
        try {
            String[] assetsList = HttpServerUtils.getAppContext().getAssets().list(assetsPath);
            if (assetsList != null)
                for (String fileName : assetsList) {
                    if (!fileName.contains(".")) {
                        getFiles(assetsPath, fileName, maps, fileNameFilter);
                    } else {
                        if (!fileName.matches(fileNameFilter)) {
                            maps.put(fileName, getAssetsPath(assetsPath) + fileName);
                        }
                    }
                }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return maps;
    }

    private static void getFiles(String assetsPath, String file, Map<String, String> maps, String fileNameFilter) {
        try {
            String[] assetsList = HttpServerUtils.getAppContext().getAssets().list(getAssetsPath(assetsPath) + file);
            if (assetsList != null)
                for (String fileName : assetsList) {
                    if (!fileName.contains(".")) {
                        getFiles(assetsPath, file + "/" + fileName, maps, fileNameFilter);
                    } else {
                        if (!fileName.matches(fileNameFilter)) {
                            maps.put(fileName, getAssetsPath(assetsPath) + file + "/" + fileName);
                        }
                    }
                }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getAssetsPath(String assetsPath) {
        if (assetsPath == null || "".equals(assetsPath)) {
            return "";
        } else {
            return assetsPath + "/";
        }
    }

}