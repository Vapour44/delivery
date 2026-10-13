package com.sky.controller.admin;


import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.service.ResponseService;
import com.sky.utils.AliOssUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/admin/common/upload")
public class UploadController {
    @Autowired
    private AliOssUtil aliOssUtil;
    @Autowired
    private ResponseService responseService;
    @PostMapping
    public Result upload(MultipartFile file) throws Exception {

        String url = aliOssUtil.upload(file.getBytes(),file.getOriginalFilename());

        return Result.success(url);

    }

}
