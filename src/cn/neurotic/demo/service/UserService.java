package cn.neurotic.demo.service;

import cn.neurotic.demo.dao.UserDao;
import cn.neurotic.springmvc.annotation.Autowired;
import cn.neurotic.springmvc.annotation.Service;

/**
 * @Author: hx
 * @Date: 2019/7/7 15:45
 * @Description:
 */
@Service("userService")
public class UserService {
    @Autowired
    UserDao userDao;

    public String test(String name, String pass) {
        return "success"+name+"  "+pass;
    }
}
