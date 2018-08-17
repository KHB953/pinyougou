// 文件上传服务层
app .service("uploadService",function ($http ) {
    this.uploadFile=function () {
        var formData = new FormData();
        //文件框中name必须是file
        formData.append("file",file.files[0]);

        // anjularjs对于post和get请求默认的Content-Type header 是application/json。
        // 通过设置‘Content-Type’: undefined，这样浏览器会帮我们把Content-Type 设置为 multipart/form-data.

        return $http({
            method:'POST',
            url:"../upload.do",
            data: formData,
            headers: {'Content-Type':undefined},
            transformRequest: angular.identity

        });
    }
});