function Scan(){
    var scan = document.getElementById('fake_scan');
    var real_scan = document.getElementById("scan");
    var textField = document.getElementById('textfield');
    var init = function () {
        if (null != scan){
            scan.addEventListener("click",onFakeScan,false);
        }
        if (null != real_scan){
            real_scan.addEventListener("change",onChange,false);
        }
        var slid = document.getElementById('drage_file');
        /*拖拽的目标对象------ document 监听drop 并防止浏览器打开客户端的图片*/
        document.ondragover = function (e) {
            e.preventDefault();  //只有在ondragover中阻止默认行为才能触发 ondrop 而不是 ondragleave
        };
        document.ondrop = function (e) {
            e.preventDefault();  //阻止 document.ondrop的默认行为  *** 在新窗口中打开拖进的图片
        };
        /*拖拽的源对象----- 客户端的一张图片 */
        /*拖拽目标对象-----div#container  若图片释放在此元素上方，则需要在其中显示*/
        slid.ondragover = function (e) {
            e.preventDefault();
        };
        slid.ondrop = function (e) {
            console.log(e.dataTransfer);
//        chrome 此处的显示有误
            var list = e.dataTransfer.files;
            for (var i = 0; i < list.length; i++) {
                var f = list[i];
                console.log(f.name);
                textField.value = f.name;
                checkUpload(f);
            }
        };
        // uploadTipPos();
    }();
    function uploadTipPos() {
        var tip = document.getElementById('up_format');
        if (null != tip && null != scan){
            var left = scan.offsetLeft;
            var width = scan.offsetWidth;
            var tipLeft = tip.offsetLeft;
            var tipWidth = tip.offsetWidth;
            console.log("scan(" + left + "," + width + "),tip(" + tipLeft + "," + tipWidth + ")");
        }
    }
    function onFakeScan() {
        console.log("click fake scan");
        if (null != real_scan)
            real_scan.click();
    }
    function onChange(e) {
        if (null != textField){
            textField.value = this.files[0].name;
            if (null != this.files){
                for (var i = 0; i < this.files.length; ++i){
                    console.log("files[" + i + "] = " + this.files[i].name);
                    var res = checkFile.isValidFile(textField.value);
                    console.log("res = " + res);
                    checkUpload(this.files[i],function () {
                        console.log('upload file callbakc');
                        e.target.value = '';
                    });
                }
            }
        }
    }
    function checkUpload(file,callback) {
        var res = checkFile.isValidFile(file.name);
        console.log("res = " + res);
        if (res){
            checkFile.upload("/file",file,callback);
        }else{
            alert("仅支持EPUB、PDF、CAJ格式！");
        }
    }
}
var checkFile;
window.onload = function () {
    new Scan();
    checkFile = new Check();
}