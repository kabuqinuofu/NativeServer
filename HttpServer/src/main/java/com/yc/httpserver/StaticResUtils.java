package com.yc.httpserver;

import java.io.InputStream;
import java.util.Map;

/**
 * 静态资源文件处理
 */
public class StaticResUtils {

    /**
     * 获取静态文件列表
     */
    public static Map<String, String> getFiles(String resDir, String fileNameFilter) {
        if (resDir.startsWith("/")) {
            return FFileUtils.getFileLs(resDir, fileNameFilter);
        } else {
            return AssetsUtils.getAssetsLs(resDir, fileNameFilter);
        }
    }

    /**
     * 获取文件流
     */
    public static InputStream getFileInp(String filePath) {
        if (filePath.startsWith("/")) {
            return FFileUtils.file2Inp(filePath);
        } else {
            return AssetsUtils.getAssetsToInp(filePath);
        }
    }

}