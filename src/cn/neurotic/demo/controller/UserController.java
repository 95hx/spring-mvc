package cn.neurotic.demo.controller;

import cn.neurotic.demo.service.UserService;
import cn.neurotic.springmvc.annotation.Autowired;
import cn.neurotic.springmvc.annotation.Controller;
import cn.neurotic.springmvc.annotation.RequestMapping;
import cn.neurotic.springmvc.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * @Author: hx
 * @Date: 2019/7/7 15:42
 * @Description:
 */
@Controller
@RequestMapping("/")
public class UserController {
    @Autowired("userService")
    UserService userService;

    @RequestMapping("/test")
    private String test(HttpServletRequest request, HttpServletResponse response, @RequestParam("name") String name, @RequestParam("pass") String pass) throws Exception{
        PrintWriter printWriter = response.getWriter();
        String result=userService.test(name,pass);
        printWriter.write(result);
        return result;
    }
}
