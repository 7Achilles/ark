package com.hallo.peach.ioc.core;


import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import com.hallo.peach.ioc.annotation.PAspect;
import com.hallo.peach.ioc.annotation.PAutowired;
import com.hallo.peach.ioc.annotation.PElement;
import com.hallo.peach.ioc.annotation.PPointCut;
import com.hallo.peach.ioc.proxy.PeachMethodInterceptor;
import com.hallo.peach.ioc.start.PeachApplication;
import com.hallo.peach.ioc.test.AopTest;
import com.hallo.peach.ioc.util.ClassUtil;

import java.util.concurrent.ConcurrentHashMap;


import org.apache.commons.lang.StringUtils;


/**
 * @author lym
 */
public class PeachApplicationContext{

    // 扫包范围
    private String packageName;

    private static ConcurrentHashMap<String,Object> beans;


    /**
     * 单例模式
     */
    private static PeachApplicationContext instance;


    private PeachApplicationContext() {
    }

    public static PeachApplicationContext getInstance(String packageName) {

        if (instance == null) {

            synchronized (PeachApplication.class) {

                if (instance == null) {

                    try {

                        instance = new PeachApplicationContext(packageName);

                    } catch (Exception e) {

                        e.printStackTrace();
                    }

                }
            }
        }
        return instance;
    }

    public static ConcurrentHashMap<String, Object> getBeans() {
        return beans;
    }



    public PeachApplicationContext(String packageName) throws Exception {

        this.packageName = packageName;

        beans = new ConcurrentHashMap<String, Object>();

        // 初始化带有组件注解的模块
        initBeans();

        // 初始化属性 及 Autowired注解
        initAttris();


    }

    private void initAttris() throws Exception {
        for (Object o : beans.keySet()) {
            // 依赖注入
            attriAssign(beans.get(o));
        }
    }

    /**
     * 初始化bean对象
     *
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    private void initBeans() throws IllegalArgumentException, IllegalAccessException {

        //扫描包下的所有文件
        List<Class<?>> classes = ClassUtil.getClasses(packageName);

        //查找文件中带有注解的文件并且初始化
        ConcurrentHashMap<String, Object> annotations = getAnnotation(classes);

        if (annotations == null || annotations.isEmpty()) {
            throw new RuntimeException("There is no beans");
        }
    }


    public static Object getBean(String beanId) throws Exception {

        if (beanId == null || StringUtils.isEmpty(beanId)) {
            throw new RuntimeException("beanId cannot be null");
        }

        Object class1 = beans.get(beanId);

        if (class1 == null) {
            throw new RuntimeException("this package has no beanId of" + beanId + "'s bean");
        }
        return class1;
    }

    /**
     * 查找注解
     *
     * @param classes
     * @return
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    private ConcurrentHashMap<String, Object> getAnnotation(List<Class<?>> classes)
            throws IllegalArgumentException {
        for (Class<?> class1 : classes) {
            //构建组件beans
            PElement pElement = class1.getAnnotation(PElement.class);
            if (pElement != null) {
                // beanId 类名小写
                String beanId = pElement.name();
                if (StringUtils.isEmpty(beanId)) {
                    // 获取当前类名
                    beanId = toLowerCaseFirstOne(class1.getSimpleName());
                }
                Object instance = newInstance(class1, beanId);
                beans.put(beanId, instance);
            }
            //构建切面
            PAspect pAspect = class1.getAnnotation(PAspect.class);
            if (pAspect != null) {
                //获取当前切面的所有方法
                Method[] methods = class1.getDeclaredMethods();
                //找切点方法
                for (Method method : methods) {

                    //找到
                    if (method.isAnnotationPresent(PPointCut.class)) {

                        PPointCut pPointCut = method.getAnnotation(PPointCut.class);

                        String methodName = pPointCut.methodName();

                        String[] beanIdArr = class1.getName().split("\\.");

                        int index = beanIdArr.length;

                        String beanId = beanIdArr[index - 1];
                        if (!StringUtils.isEmpty(beanId)) {
                            // 获取当前类名
                            beanId = toLowerCaseFirstOne(class1.getSimpleName());
                        }

                        //根据切点 创建切面对象
                        Object ob =  newInstance(class1, beanId);

                        //构造代理类
                        PeachMethodInterceptor proxyer = new PeachMethodInterceptor(ob);

                        //创建被代理对象
                        String className = pPointCut.className();

                        Object o = ClassUtil.getInstance(className);

                        //设置代理的方法
                        proxyer.setProxyMethodName(methodName);

                        Object object = proxyer.createProxyObject(o);

                        if (object != null && !StringUtils.isEmpty(className)) {

                            beanIdArr = className.split("\\.");

                            index = beanIdArr.length;

                            beanId = beanIdArr[index - 1];

                            beanId = toLowerCaseFirstOne(beanId);

                            beans.put(beanId, object);
                        }

                    }

                }

            }

        }
        return beans;
    }

    /**
     * 首字母小写
     *
     * @param s
     * @return s
     */
    public static String toLowerCaseFirstOne(String s) {
        if (Character.isLowerCase(s.charAt(0))) {
            return s;
        } else {
            return (new StringBuilder()).append(Character.toLowerCase(s.charAt(0))).append(s.substring(1)).toString();
        }
    }

    /**
     * 反射
     *
     * @param classInfo
     * @return
     */
    public Object newInstance(Class<?> classInfo, String beanId) {
        try {
            return classInfo.newInstance();
        } catch (Exception e) {
            StringBuffer stringBuffer = new StringBuffer("There is a mistake to create bean of ");
            stringBuffer.append(beanId);
            stringBuffer.append(",cause by ");
            stringBuffer.append(e.getMessage());
            throw new RuntimeException(stringBuffer.toString());
        }
    }

    /**
     * 依赖注入
     *
     * @param object
     * @throws Exception
     */
    public void attriAssign(Object object) throws Exception {

        //使用反射机制获取当前类的所有属性
        Field[] declaredFields = object.getClass().getDeclaredFields();

        //判断当前属性是否存在注解
        for (Field field : declaredFields) {

            PAutowired pAutowired = field.getAnnotation(PAutowired.class);

            if (pAutowired != null) {

                // 获取属性名称
                String name = field.getName();

                // 根据beanName查找对象
                Object newBean = getBean(name);

                // 默认使用属性名称,查找bean容器对象
                if (object != null) {
                    field.setAccessible(true);
                    // 给属性赋值 将对象注入到 属性中
                    field.set(object, newBean);
                }
            }

        }
    }


}
