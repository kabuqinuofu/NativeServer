function Check(){
    this.isValidFile = fIsValidFile;
    function fIsValidFile(fileName) {
        if (null == fileName)
            return false;
        var ext = fileName.substr(fileName.lastIndexOf(".") + 1).toLocaleLowerCase();
        console.log("ext = " + ext);
        if (ext == 'epub' || ext == 'caj' || ext == 'pdf' || ext == 'kdh' || ext == 'teb' || ext == 'nh' /*|| ext == 'txt'*/)
            return true;
        return false;
    }
    this.xmlRequest = getxhr();
    this.upload = upladFile;
    var fileCache = new Array();
    function getxhr()
    {
        //获取ajax对象
        var xhr = null;
        try
        {
            xhr = new XDomainRequest();
        }
        catch(e)
        {
            try
            {
                xhr = new XMLHttpRequest();
            }
            catch(e)
            {
                try
                {
                    xhr = new ActiveXObject("Msxml2.XMLHTTP");
                }
                catch(e)
                {
                    xhr = new ActiveXObject("Microsoft.XMLHTTP");
                }
            }
        }
        return xhr;
    }
    function upladFile(url,file,callback) {
        new Upload(getxhr(),url,file,callback).upload;
    }
}
function Upload(xhr,url,file,callback) {
    var timeStamp = new Date().getTime();
    var list = document.getElementById('wifi_file_list');
    this.upload = upload();
    //上传文件方法
    function upload() {

        getMd5(file,calculate);
    }
    function calculate(md5) {
        var form = new FormData(); // FormData 对象
        form.append("file", file); // 文件对象
        console.log("md5 = " + md5);
        xhr.open("post", url, true); //post方式，url为服务器请求地址，true 该参数规定请求是否异步处理。
        xhr.setRequestHeader("fileName",encodeURI(file.name));
        xhr.setRequestHeader("md5",md5);
        xhr.onbeforeunload = beforeupload;
        xhr.onreadystatechange = readystatechange;
        // xhr.onload = uploadComplete; //请求完成
        xhr.onerror =  uploadFailed; //请求失败
        xhr.upload.onprogress = progressFunction;//【上传进度调用方法实现】
        xhr.upload.onloadstart = start;
        xhr.upload.onload = uploadComplete;
        xhr.upload.onerror = uploadFailed;
        xhr.send(form); //开始上传，发送form数据
    }
    function readystatechange(){
            if(xhr.readyState == 4 && xhr.status == 200){
                var b = xhr.responseText;
                try{
                    var result = JSON.parse(b);
                    if (null != result){
                        result.result == 'exist';
                        cancelOrFailed();
                        alert(file.name + ' 已存在');
                    }
                }catch (e) {

                }
            }
        }
    //上传进度实现方法，上传过程中会频繁调用该方法
    function progressFunction(evt) {
        var size = document.getElementById(timeStamp + "size");
        console.log("total = " + evt.total + "/" + evt.loaded + "," + (timeStamp + "size"));
        var len = evt.total;
        var loaded = evt.loaded;
        var unit = "B";
        if (null != size){
            var lenDis = len;
            if (len > 1024){
                lenDis = len/1024;
                unit = "K";
                if (lenDis > 1024){
                    lenDis = lenDis/1024;
                    unit = "M";
                    if (lenDis > 1024){
                        lenDis = lenDis/1024;
                        unit = "G";
                    }
                }
            }
            var sizeDis = lenDis.toFixed(2) + unit;
            size.innerHTML = sizeDis;
            console.log("sizeDis = " + sizeDis);
        }
        var progress = loaded/len*100;
        var operate = document.getElementById(timeStamp + "text_progess");
        var progressBar = document.getElementById(timeStamp + "progress");
        operate.innerHTML = progress.toFixed(1) + "%";
        progressBar.style.width = parseInt(progressBar.parentElement.offsetWidth * progress
        /100) + "px";
    }
    //上传成功响应
    function uploadComplete(evt) {
        //服务断接收完文件返回的结果
        console.log("uploadComplete");
        var span = document.getElementById(timeStamp + "text_progess");
        if (null != span){
            span.className = "oper_cell_txt upload_complete";
            span.innerHTML = "完成";
        }
        console.log("uploadComplete imgid = " + timeStamp + "img_cancel");
        var img = document.getElementById(timeStamp + "img_cancel");
        if (null != img){
            img.removeEventListener('click',cancleUploadFile);
            console.log("uploadComplete remove click");
        }
        if (null != callback){
            callback();
        }
    }
    //上传失败
    function uploadFailed() {
        cancelOrFailed();
    }
    //取消上传
    function cancleUploadFile(){
        xhr.abort();
        cancelOrFailed();
    }
    function cancelOrFailed() {
        console.log("cancelOrFailed");
        var span = document.getElementById(timeStamp + "text_progess");
        if (null != span){
            span.className = "oper_cell_txt upload_cancel";
            span.innerHTML = "上传失败";
        }
        var img = document.getElementById(timeStamp + "img_cancel");
        if (null != img){
            img.removeEventListener('click',cancleUploadFile);
        }
        var itemTable = document.getElementById(timeStamp + "item_table");
        if (null != itemTable){
            itemTable.className = "item_table item_table_failed";
        }
        if (null != callback){
            callback();
        }
    }
    function beforeupload() {
        console.log('before upload');
    }
    function start() {
        console.log('start');
        var tr = document.createElement("div");
        var tr_parent = document.createElement("div");
        var item_table = document.createElement("div");
        var tb_name = document.createElement("div");
        var tb_name_in_div = document.createElement('div');
        var tb_size = document.createElement("div");
        var tb_operate = document.createElement("div");
        var div_operate = document.createElement('div');
        var span_operate = document.createElement('span');
        var img_operate = document.createElement('img');
        var item_progress = document.createElement("div");
        tr.className = "tr";
        tr_parent.className = "tr_parent";
        item_table.className = "item_table";
        item_table.id = timeStamp + "item_table";
        tb_name.className = "table_td tb_name";
        tb_size.className = "table_td tb_size";
        tb_operate.className = "table_td tb_operate";
        item_progress.className = "item_progress";
        item_progress.id = timeStamp + "progress";
        tb_name_in_div.innerHTML = file.name;
        tb_size.id = timeStamp + "size";
        // tb_size.innerHTML = "30.0M";
        tb_operate.id = timeStamp + "operate";
        // span_operate.innerHTML = "传输中";
        img_operate.src = '../img/circle_delete.png';
        span_operate.id=timeStamp + "text_progess";
        img_operate.id=timeStamp + "img_cancel";
        span_operate.className = "oper_cell_txt uploading";
        div_operate.className = "oper_cell";
        img_operate.addEventListener('click',cancleUploadFile,false);
        div_operate.appendChild(span_operate);
        div_operate.appendChild(img_operate);
        tb_operate.appendChild(div_operate);
        tr.appendChild(tr_parent);
        tr_parent.appendChild(item_table);
        tr_parent.appendChild(item_progress);
        tb_name.appendChild(tb_name_in_div);
        item_table.appendChild(tb_name);
        item_table.appendChild(tb_size);
        item_table.appendChild(tb_operate);
        list.appendChild(tr);
    }

}