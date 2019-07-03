package com.hallo.peach.ioc.core;


import java.lang.reflect.Field;
import java.util.List;

import com.hallo.peach.ioc.annotation.PAutowired;
import com.hallo.peach.ioc.annotation.PElement;
import com.hallo.peach.ioc.util.ClassUtil;

import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang.StringUtils;


/**
 * @author lvyamin
 */
public class PeachApplicationContext {

    // 扫包范围
    private String packageName;

    private ConcurrentHashMap<String, Object> beans = null;

    public PeachApplicationContext(String packageName) throws Exception {
        this.packageName = packageName;
        beans = new ConcurrentHashMap<String, Object>();
        // 初始化beans Service注解
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

    // 初始化对象
    public void initBeans() throws IllegalArgumentException, IllegalAccessException {
        // 1.扫包
        List<Class<?>> classes = ClassUtil.getClasses(packageName);
        // 2.判断是否有注解
        ConcurrentHashMap<String, Object> findClassExisAnnotation = findClassExisAnnotation(classes);
        if (findClassExisAnnotation == null || findClassExisAnnotation.isEmpty()) {
            throw new RuntimeException("该包下没有这个注解");
        }
    }

    public Object getBean(String beanId) throws Exception {

        if (beanId == null || StringUtils.isEmpty(beanId)) {
            throw new RuntimeException("beanId不能为空");
        }

        Object class1 = beans.get(beanId);

        if (class1 == null) {
            throw new RuntimeException("该包下没有BeanId为" + beanId + "的类");
        }
        return class1;
    }

    // 是否有注解
    public ConcurrentHashMap<String, Object> findClassExisAnnotation(List<Class<?>> classes)
            throws IllegalArgumentException, IllegalAccessException {
        for (Class<?> class1 : classes) {
            PElement annotation = class1.getAnnotation(PElement.class);
            if (annotation != null) {
                // beanId 类名小写
                String beanId = annotation.name();
                if (StringUtils.isEmpty(beanId)) {
                    // 获取当前类名
                    beanId = toLowerCaseFirstOne(class1.getSimpleName());
                }
                Object newInstance = newInstance(class1);
                beans.put(beanId, newInstance);
            }
        }
        return beans;
    }

    // 首字母转小写
    public static String toLowerCaseFirstOne(String s) {
        if (Character.isLowerCase(s.charAt(0))) {
            return s;
        } else {
            return (new StringBuilder()).append(Character.toLowerCase(s.charAt(0))).append(s.substring(1)).toString();
        }
    }

    // 通过反射生成对象
    public Object newInstance(Class<?> classInfo) {
        try {
            return classInfo.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("反射生成对象失败" + e.getMessage());
        }
    }

    // 依赖注入实现原理
    public void attriAssign(Object object) throws Exception {
        // 1.使用反射机制获取当前类的所有属性
        Field[] declaredFields = object.getClass().getDeclaredFields();
        // 2.判断当前类是否存在注解
        for (Field field : declaredFields) {
            PAutowired annotation = field.getAnnotation(PAutowired.class);
            if (annotation != null) {
                // 获取属性名称
                String name = field.getName();
                // 根据beanName查找对象
                Object newBean = getBean(name);
                // 3.默认使用属性名称,查找bean容器对象
                if (object != null) {
                    field.setAccessible(true);
                    // 给属性赋值 将对象注入到 属性中
                    field.set(object, newBean);
                }
            }
        }
    }






}
