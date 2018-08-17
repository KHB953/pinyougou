package com.pinyougou.shop.controller;

import entity.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import util.FastDFSClient;


/**
 * 文件上传Controller
 * @author hongbin
 */
@RestController
public class UploadController {

    /**
     * 文件上传服务器地址
     */
    @Value("${FILE_SERVER_URL}")
    private String FILE_SERVER_URL;

    @RequestMapping("/upload")
    public Result upload(MultipartFile file){

        //1 获取文件的扩展名
        String originalFilename = file.getOriginalFilename();
        String extName = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);

        try {
            //创建FastDFS客户端
            FastDFSClient fastDFSClient = new FastDFSClient("classpath:config/fdfs_client.conf");
            //执行文件上传
            String path = fastDFSClient.uploadFile(file.getBytes(), extName);
            // 返回path表示url 再拼接ip地址
            String url =FILE_SERVER_URL+path;
            return new Result(true,url);

        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "上传失败");
        }
    }

}
