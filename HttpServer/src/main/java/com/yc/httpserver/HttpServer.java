package com.yc.httpserver;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.yc.httpserver.annotation.RequestBody;
import com.yc.httpserver.annotation.RequestParam;
import com.yc.httpserver.annotation.ResponseBody;
import com.yc.httpserver.listener.UploadListener;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;

import java.lang.annotation.Annotation;
import java.util.Map;

import fi.iki.elonen.NanoFileUpload;
import fi.iki.elonen.NanoHTTPD;

public class HttpServer extends NanoHTTPD {

    private FileManager mFileManager;

    public HttpServer(int port, String saveDir, UploadListener listener) {
        super(port);
        mFileManager = new FileManager(saveDir, listener);
    }

    @Override
    public Response serve(IHTTPSession session) {
        Method method = session.getMethod();
        if (Method.POST.equals(method)) {
            if (mFileManager.ensureDirectoryExists()) {
                NanoFileUpload upload = new NanoFileUpload(new DiskFileItemFactory());
                try {
                    FileItemIterator fileItemIterator = upload.getItemIterator(session);
                    String fileName;
                    while (fileItemIterator.hasNext()) {
                        FileItemStream item = fileItemIterator.next();
                        if (!item.isFormField()) {
                            fileName = item.getName();
                            mFileManager.saveFile(item.openStream(), fileName);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Response response = newFixedLengthResponse(Response.Status.REDIRECT,
                    NanoHTTPD.MIME_HTML, "");
            response.addHeader("Location", "/");
            return response;
        }

        String file_name = session.getUri().substring(1);
        if (TextUtils.isEmpty(file_name)) {
            file_name = NativeServerManager.getFHttpManager().getIndexName();
        }
        Response response = getResources(file_name);
        return response == null ? responseData(session, file_name) : response;
    }

    /**
     * 获取静态资源
     */
    private Response getResources(String file_name) {
        int dot = file_name.lastIndexOf('/');
        file_name = dot >= 0 ? file_name.substring(dot + 1) : file_name;
        String filePath = NativeServerManager.getFHttpManager().getFilels().get(file_name);
        if (filePath != null) {
            return NanoHTTPD.newChunkedResponse(Response.Status.OK, getMimeTypeForFile(file_name), StaticResUtils.getFileInp(filePath));//这代表任意的二进制数据传输。
        }
        return null;
    }

    /**
     * 解析注解文件
     */
    private Response responseData(IHTTPSession session, String file_name) {
        Response response;
        Object[] objects = null;
        try {
            Map<String, java.lang.reflect.Method> methods = NativeServerManager.getFHttpManager().getMethods();
            java.lang.reflect.Method method = methods.get(file_name);
            if (method != null) {
                method.setAccessible(true); //允许修改反射属性
                Class cla = method.getDeclaringClass();//获取该方法所在的类
                Object obj = cla.newInstance();//实例化类
                Class<?>[] parameterTypes = method.getParameterTypes(); //获得方法所有参数的类型
                if (parameterTypes.length > 0) {
                    objects = new Object[parameterTypes.length];
                    Map<String, String> sessionMap = session.getParms();//获取请求参数
                    Annotation[][] parameterAnnotations = method.getParameterAnnotations();//获取方法参数里的注解
                    for (int i = 0; i < parameterAnnotations.length; i++) {
                        if (parameterTypes[i] == IHTTPSession.class) {
                            objects[i] = session;
                        } else if (parameterTypes[i] == Map.class) {
                            objects[i] = sessionMap;
                        } else {
                            Annotation parameterAnnotation = parameterAnnotations[i][0];//获取参数中的第一个注解。所以每个参数只能只有一个注解
                            if (parameterAnnotation.annotationType() == RequestBody.class) {//返回对象
                                byte[] buf = new byte[(int) ((HTTPSession) session).getBodySize()];
                                session.getInputStream().read(buf, 0, buf.length);
                                objects[i] = new Gson().fromJson(new String(buf), parameterTypes[i]);
                            } else if (parameterAnnotation.annotationType() == RequestParam.class) {//返回指定param
                                objects[i] = dataConversion(parameterTypes[i], sessionMap, (RequestParam) parameterAnnotation);
                            }
                        }
                    }
                }
                response = responseBody(method.getReturnType(), method.invoke(obj, objects), method.isAnnotationPresent(ResponseBody.class));
            } else {
                response = newFixedLengthResponse(Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, file_name + " Not Found");
            }
        } catch (Exception e) {
            response = newFixedLengthResponse(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, e.getMessage());
        }

        //下面是跨域的参数（因为一般要和h5联调，所以最好设置一下）
        if (NativeServerManager.getFHttpManager().isAllowCross()) {
            response.addHeader("Access-Control-Allow-Headers", "Content-Type, Accept, token, Authorization, " +
                    "X-Auth-Token,X-XSRF-TOKEN,Access-Control-Allow-Headers");
            response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, HEAD");
            response.addHeader("Access-Control-Allow-Credentials", "true");
            response.addHeader("Access-Control-Allow-Origin", "*");
            response.addHeader("Access-Control-Max-Age", "" + 42 * 60 * 60);
        }

        return response;
    }

    /**
     * param数据转换
     */
    private Object dataConversion(Class parameterTypes, Map<String, String> sessionMap, RequestParam requestParam) {
        Object object;

        switch (parameterTypes.getName()) {
            case "int":
                object = Integer.parseInt(sessionMap.get(requestParam.value()));
                break;
            case "double":
                object = Double.parseDouble(sessionMap.get(requestParam.value()));
                break;
            case "float":
                object = Float.parseFloat(sessionMap.get(requestParam.value()));
                break;
            case "long":
                object = Long.parseLong(sessionMap.get(requestParam.value()));
                break;
            case "boolean":
                object = Boolean.parseBoolean(sessionMap.get(requestParam.value()));
                break;
            default:
                object = sessionMap.get(requestParam.value());
                break;
        }
        return object;
    }

    /**
     * response 数据处理
     */
    private Response responseBody(Class objCls, Object responseObj, boolean hasAnnotation) {
        Response response = null;
        String bodyStr = "";
        if (objCls == Response.class) {
            response = (Response) responseObj;
        } else {
            if (!hasAnnotation) {
                bodyStr = String.valueOf(responseObj);
                if (bodyStr.endsWith(".html")) {
                    response = getResources(bodyStr);
                } else {
                    response = NanoHTTPD.newFixedLengthResponse(Response.Status.OK, "application/octet-stream", bodyStr);
                }
            } else {
                bodyStr = new Gson().toJson(responseObj, objCls);
                response = NanoHTTPD.newFixedLengthResponse(Response.Status.OK, "application/octet-stream", bodyStr);
            }
        }
        return response;
    }

}