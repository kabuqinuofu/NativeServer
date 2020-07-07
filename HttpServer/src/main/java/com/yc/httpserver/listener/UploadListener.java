package com.yc.httpserver.listener;

import java.io.File;

public interface UploadListener {

    public void upLoadSuccess(File file);

    public void upLoadFailure(Exception e);

}
