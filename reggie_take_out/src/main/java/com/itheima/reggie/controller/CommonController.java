package com.itheima.reggie.controller;

import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/common")
public class CommonController {

    @Value("${reggie.path}")
    private String basePath;
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) throws IOException {
        log.info(file.toString());
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString()+suffix;

        File dir=new File(basePath);
        if(!dir.exists()){
            dir.mkdirs();
        }
        file.transferTo(new File(basePath+fileName));
        return R.success(fileName);
    }

    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) throws IOException {
        FileInputStream fileInputStream=new FileInputStream(new File(basePath+name));
        ServletOutputStream outputStream = response.getOutputStream();
        response.setContentType("image/jpeg");
        byte [] bytes=new byte[1024];
        int len=0;
        while ((len=fileInputStream.read(bytes))!=-1){
            outputStream.write(bytes,0,len);
            outputStream.flush();
        }
        outputStream.close();
        fileInputStream.close();
    }
}
