package com.yc.httpserver;

import android.os.Environment;

import com.yc.httpserver.listener.UploadListener;

import org.apache.commons.fileupload.util.Streams;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileManager {

    private String saveDir;
    private UploadListener mUploadListener;

    public FileManager(String saveDir, UploadListener listener) {
        this.saveDir = saveDir;
        this.mUploadListener = listener;
    }

    public boolean ensureDirectoryExists() {

        String externalStorageState = Environment.getExternalStorageState();

        if (!Environment.MEDIA_MOUNTED.equals(externalStorageState)) {
            if (mUploadListener != null)
                mUploadListener.upLoadFailure(new Exception("External storage is not mounted"));
            return false;
        }

        File uploadDirectory = getUploadDirectory();

        if (!uploadDirectory.exists() && !uploadDirectory.mkdirs()) {
            if (mUploadListener != null)
                mUploadListener.upLoadFailure(new Exception("Failed to create directory"));
            return false;
        }

        return true;
    }

    public void saveFile(InputStream inStream, String filename) throws IOException {
        File destination = new File(getUploadDirectory(), filename);
        OutputStream outStream = new FileOutputStream(destination);
        Streams.copy(inStream, outStream, true);

        if (mUploadListener != null)
            mUploadListener.upLoadSuccess(destination);
    }

    private File getUploadDirectory() {
        return HttpServerUtils.getAppContext().getExternalFilesDir(saveDir);
    }

}
