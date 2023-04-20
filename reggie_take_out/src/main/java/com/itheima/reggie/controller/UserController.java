package com.itheima.reggie.controller;

import com.aliyun.dysmsapi20170525.Client;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.sample.Sample;
import com.itheima.reggie.service.UserService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.internal.http2.Http2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.server.Session;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import com.aliyun.dysmsapi20170525.models.*;
import com.aliyun.tea.TeaException;
import com.aliyun.teautil.Common;
import com.aliyun.teautil.models.RuntimeOptions;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user , HttpSession session) throws Exception {
        String phone = user.getPhone();
        log.info("手机号: "+phone);
        if (phone!=null){
            com.aliyun.dysmsapi20170525.Client client = Sample.createClient();
            String code = String.valueOf((int)((Math.random()*9+1)*1000));
            com.aliyun.dysmsapi20170525.models.SendSmsRequest sendSmsRequest = new com.aliyun.dysmsapi20170525.models.SendSmsRequest()
                    .setPhoneNumbers(phone)
                    .setSignName("阿里云短信测试")
                    .setTemplateCode("SMS_154950909")
                    .setTemplateParam("{\"code\":"+code+"}");
//            SendSmsResponse sendSmsResponse = client.sendSmsWithOptions(sendSmsRequest, new RuntimeOptions());
//            log.info(sendSmsResponse.body.getMessage());
//            log.info(sendSmsResponse.body.requestId);
//            log.info(sendSmsResponse.body.getCode());
//            log.info(sendSmsResponse.body.getBizId());
            log.info("验证码:"+code);
//            session.setAttribute(phone,code);

            redisTemplate.opsForValue().set(phone,code,5, TimeUnit.MINUTES);
            return R.success("验证码已发送");
        }
        return R.error("发送失败");
    }

    @PostMapping("/login")
    public R<String> login(@RequestBody Map map, HttpSession session){
        log.info(map.toString());
        String phone = map.get("phone").toString();
        String code = map.get("code").toString();
        log.info("phone: "+phone);
        log.info("code: "+code);

//        Object attribute = session.getAttribute(phone);
        Object attribute = redisTemplate.opsForValue().get(phone);

        if (attribute!=null&&attribute.equals(code)){
            LambdaQueryWrapper<User> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,phone);
            User user = userService.getOne(queryWrapper);
            if (user==null){
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            session.setAttribute("user",user.getId());
            redisTemplate.delete(phone);
            return R.success("成功登陆");
        }
        return R.error("登陆失败");
    }
}
