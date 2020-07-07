package com.yc.httpserver;

import android.content.Context;

import com.yc.httpserver.annotation.RequestMapping;
import com.yc.httpserver.listener.UploadListener;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class NativeServerManager {

    private HttpServer httpServer;
    private int port = 8080;//端口号
    private UploadListener mUploadListener;
    private static NativeServerManager nativeServerManager;
    private String resDir = "";//静态资源目录，默认空 为assets根目录
    private String saveDir = "WifiTransfer";//文件保存默认目录
    private String fileNameFilter = ".*xml";//文件过滤 默认过滤xml文件
    private String indexName = "index.html"; // 设置index名称
    private boolean allowCross = false;//是否允许跨站
    private Map<String, String> fileMaps = new HashMap<>();//获取所有js，css等静态文件
    private Map<String, Method> methodMaps = new HashMap<>();//获取所有带RequestMapping注解的method

    private NativeServerManager(Context context, Class... serverCls) {
        HttpServerUtils.init(context.getApplicationContext());
        clsMethods(serverCls);
    }

    public static NativeServerManager init(Context context, Class... serverCls) {
        if (nativeServerManager == null) {
            synchronized (NativeServerManager.class) {
                if (nativeServerManager == null) {
                    nativeServerManager = new NativeServerManager(context, serverCls);
                }
            }
        }
        return nativeServerManager;
    }

    /**
     * 根据所提供的类，获取所有带RequestMapping注解的方法
     */
    private void clsMethods(Class... serverCls) {
        for (Class cls : serverCls) {
            Method[] declaredMethods = cls.getDeclaredMethods();
            for (Method method : declaredMethods) {
                if (method.isAnnotationPresent(RequestMapping.class)) {
                    RequestMapping reqValue = method.getAnnotation(RequestMapping.class);
                    methodMaps.put(reqValue.value(), method);
                }
            }
        }
    }

    /**
     * 启动服务
     */
    public void startServer() {
        startServer(NanoHTTPD.SOCKET_READ_TIMEOUT);
    }

    public void startServer(int timeout) {
        if (httpServer == null) {
            fileMaps.putAll(StaticResUtils.getFiles(resDir, fileNameFilter));
            try {
                httpServer = new HttpServer(port, getSaveDir(), getUploadListener());
                httpServer.start(timeout);
            } catch (IOException e) {
                e.printStackTrace();
                if (getUploadListener() != null) {
                    getUploadListener().upLoadFailure(new Exception("启动服务失败!"));
                }
            }
        }
    }

    /**
     * 关闭服务
     */
    public void stopServer() {
        if (httpServer != null) {
            if (httpServer.isAlive()) {
                httpServer.stop();
            }
        }
    }

    /**
     * 设置端口号
     */
    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    /**
     * 设置静态资源目录
     */
    public void setResDir(String resDir) {
        this.resDir = resDir;
    }

    public String getResDir() {
        return resDir;
    }

    /**
     * 获取所有资源文件
     */
    public Map<String, String> getFilels() {
        return fileMaps;
    }

    /**
     * 获取所有带RequestMapping注解的方法
     */
    public Map<String, Method> getMethods() {
        return methodMaps;
    }

    /**
     * 设置首页html
     */
    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public String getIndexName() {
        return indexName;
    }

    /**
     * 设置文件过滤
     */
    public void setFilterName(String... filterNames) {
        StringBuilder sb = new StringBuilder();
        for (String filterName : filterNames) {
            sb.append(".*" + filterName + "|");
        }
        fileNameFilter = sb.toString().substring(0, sb.length() - 1);
    }

    /**
     * 设置是否允许ajax请求跨站
     */
    public void setAllowCross(boolean allowCross) {
        this.allowCross = allowCross;
    }

    public boolean isAllowCross() {
        return allowCross;
    }

    public static NativeServerManager getFHttpManager() {
        return nativeServerManager;
    }

    public String getSaveDir() {
        return saveDir;
    }

    public void setSaveDir(String saveDir) {
        this.saveDir = saveDir;
    }

    public UploadListener getUploadListener() {
        return mUploadListener;
    }

    public void setUploadListener(UploadListener listener) {
        this.mUploadListener = listener;
    }

}