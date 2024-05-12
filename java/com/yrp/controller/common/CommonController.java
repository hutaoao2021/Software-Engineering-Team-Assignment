package com.yrp.controller.common;

import com.yrp.common.Code;
import com.yrp.common.R;
import com.yrp.exception.SystemException;
import com.yrp.service.OssService;
import com.yrp.utils.InputStreamUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.UUID;

/**
 * 1、文件上传
 * 2、文件下载
 */
@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {
//    @Value("${file.upload.localPath}")
//    private String localPath;

    @Autowired
    OssService ossService;



    /*
    // http://localhost/common/upload
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) {
        //file是一个临时文件，需要转存到指定位置，否则本次请求完成后临时文件会删除
        String originalFilename = file.getOriginalFilename();
        // 重新命名
        //  1、获取后缀
        //  2、获取随机数uuid
        String uuid = UUID.randomUUID().toString();
        //  3、重新拼接
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFilename = uuid + suffix;
        //  4、主机上创建目录（如果已经有目录，则不用创建）
        File dir = new File(localPath);
        if (dir == null) {
            dir.mkdirs();
        }
        // 5、将临时文件转存到指定位置
        try {
            file.transferTo(new File(localPath + newFilename));
        } catch (IOException e) {
            throw new SystemException(Code.SYSTEM_ERR, "文件上传失败，请稍后重试");
        }
        return R.success(newFilename);
    }
    */


    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) {
        // 判断上传文件的大小
        if (file.getSize() > 1024 * 1024 * 5) {
            return R.error("Size of file can exceed 5 MB");
        }
        //file是一个临时文件，需要转存到指定位置，否则本次请求完成后临时文件会删除
        String originalFilename = file.getOriginalFilename();
        // 重新命名
        //  1、获取后缀
        //  2、获取随机数uuid
        String uuid = UUID.randomUUID().toString();
        //  3、重新拼接
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        // 3.1 判断文件的格式
//        if (!"jpg,jpeg,JPG,PNG,gif,png".toUpperCase().contains(suffix.toUpperCase())) {
//            return R.error("文件格式错误，只需要上传jpg,jpeg,gif,png！");
//        }
        // 4、将文件上传到阿里云oss，接收返回的url路径
        String url = ossService.uploadFile(file);
        log.info("url = {}", url);
        return R.success(url);
    }
    /**
     * 文件下载到阿里云oss
     *
     * @param name 文件名
     * @return
     */
    // http://localhost/common/download?name=38db0beb-2636-4b82-ad18-929b1fc603d2.jpeg
    @GetMapping("/download")
    public void download(String name, HttpServletResponse resp) {
        // 日志
        log.info("name = {}", name);
        // 特判
        if (name == null || name.equals("")) {
            throw new SystemException(Code.SYSTEM_ERR, "文件名不能为空");
        }
        ServletOutputStream sos = null;
        InputStream fis = InputStreamUtils.getImageStream(name);
        int len = 0;
        // 处理
        try {
            // 通过InputStreamUtils 将url所在资源转成inputStream
            // 输出流，通过输出流将文件写回到浏览器
            sos = resp.getOutputStream();
            // 位置
//            resp.setContentType("image/jpeg");
            resp.setHeader("content-disposition", "attachment;fileName=" + URLEncoder.encode(name, "UTF-8"));
            byte[] b = new byte[1024];
            while ((len = fis.read(b)) != -1){
                sos.write(b,0,len);
                sos.flush();
            }
        } catch (IOException e) {
            throw new SystemException(Code.SYSTEM_ERR, "文件下载失败，请稍后重试");
        } finally {
            try {
                if (sos != null) {
                    sos.close();
                }
            } catch (IOException e) {
                throw new SystemException(Code.SYSTEM_ERR, "文件下载失败，请稍后重试");
            }
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException e) {
                throw new SystemException(Code.SYSTEM_ERR, "文件下载失败，请稍后重试");
            }
        }
    }
}
