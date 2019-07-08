package cn.neurotic.servlet;

import cn.neurotic.demo.controller.UserController;
import cn.neurotic.springmvc.annotation.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: hx
 * @Date: 2019/7/7 15:53
 * @Description:
 */
public class DispatcherServlet extends HttpServlet {
    private List<String> classNames = new ArrayList<String>();
    private Map<String, Object> beans = new HashMap<String, Object>();
    private Map<String, Object> urlMapping = new HashMap<String, Object>();

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        doScanPackage("cn.cn.neurotic.demo");
        doInstance();
        doAutowired();
        doUrlMapping();
    }

    private void doUrlMapping() {
        try {
            for (Map.Entry<String, Object> entry : beans.entrySet()) {
                Object instance = entry.getValue();
                Class<?> clazz = instance.getClass();
                if (clazz.isAnnotationPresent(Controller.class)) {
                    RequestMapping classAnnotation = clazz.getAnnotation(RequestMapping.class);
                    String classPath = classAnnotation.value();
                    Method[] methods = clazz.getDeclaredMethods();
                    for (Method method:methods) {
                        RequestMapping methodAnnotation = clazz.getAnnotation(RequestMapping.class);
                        urlMapping.put(classPath+methodAnnotation.value(),method);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doAutowired() {
        try {
            for (Map.Entry<String, Object> entry : beans.entrySet()) {
                Object instance = entry.getValue();
                Class<?> clazz = instance.getClass();
                if (clazz.isAnnotationPresent(Controller.class)) {
                    Field[] fields = clazz.getDeclaredFields();
                    for (Field field : fields) {
                        if (field.isAnnotationPresent(Autowired.class)) {
                            Autowired annotation = field.getAnnotation(Autowired.class);
                            String key = annotation.value();
                            field.setAccessible(true);
                            field.set(instance, beans.get(key));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doInstance() {
        try {
            for (String className : classNames) {
                String cn = className.replace(".class", "");
                Class<?> clazz = Class.forName(cn);
                if (clazz.isAnnotationPresent(Controller.class)) {
                    Object controllerInstance = clazz.newInstance();
                    RequestMapping annotation = clazz.getAnnotation(RequestMapping.class);
                    beans.put(annotation.value(), controllerInstance);
                } else if (clazz.isAnnotationPresent(Service.class)) {
                    Object serviceInstance = clazz.newInstance();
                    Service annotation = clazz.getAnnotation(Service.class);
                    beans.put(annotation.value(), serviceInstance);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void doScanPackage(String packages) {
        URL url = this.getClass().getClassLoader().getResource("/" + packages.replaceAll("\\.", "/"));
        String fileStr = url.getFile();
        File files = new File(fileStr);


        for (File file : files.listFiles()) {
            if (file.isDirectory()) {
                doScanPackage(packages + "." + file.getName());
            } else {
                classNames.add(packages + "." + file.getName());
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String uri = req.getRequestURI();
            String context = req.getContextPath();
            String path = uri.replaceAll(context, "");
            Method method = (Method) urlMapping.get(path);
            UserController instance = (UserController) beans.get("/" + path.split("/")[1]);
            Object[] args=hand(req,resp,method);
            method.invoke(instance, args);
        } catch (Exception e) {
        }
    }

    private static Object[] hand(HttpServletRequest req, HttpServletResponse resp, Method method) {
        Class<?>[] paramClazzs = method.getParameterTypes();
        Object[] args = new Object[paramClazzs.length];
        int index = 0;
        for (Class<?> paramClazz:paramClazzs) {
            if (ServletRequest.class.isAssignableFrom(paramClazz)) {
                args[index] = req;
            }
            if (ServletResponse.class.isAssignableFrom(paramClazz)) {
                args[index] = resp;
            }
            Annotation[] paramAns = method.getParameterAnnotations()[index];
            if (paramAns.length > 0) {
                for (Annotation paramAn : paramAns) {
                    if (RequestParam.class.isAssignableFrom(paramAn.getClass())) {
                        RequestParam rp = (RequestParam) paramAn;
                        args[index] = req.getParameter(rp.value());
                    }
                }
            }
        }
        return args;
    }
}
