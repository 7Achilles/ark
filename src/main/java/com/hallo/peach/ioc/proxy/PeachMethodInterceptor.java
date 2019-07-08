package com.hallo.peach.ioc.proxy;

import com.hallo.peach.ioc.annotation.PAfter;
import com.hallo.peach.ioc.annotation.PBefore;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;

/**
 * 被代理类
 */
public class PeachMethodInterceptor implements MethodInterceptor {

    /**
     * 被代理的对象
     */
    private Object object;

    /**
     * 切面类对象
     *
     */
    private Object proxy;

    /**
     * 代理的方法名
     */
    private String proxyMethodName;


    public PeachMethodInterceptor(Object object){
        this.proxy = object;
    }

    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        Object result;

        String proxyMethod = getProxyMethodName();

        if (StringUtils.isNotBlank(proxyMethod) && proxyMethod.equals(method.getName())) {

            doBefore();
        }

        //执行拦截的方法
        result = methodProxy.invokeSuper(o, objects);

        if (StringUtils.isNotBlank(proxyMethod) && proxyMethod.equals(method.getName())) {
            doAfter();
        }
        return result;

    }

    public void doBefore() {
        try {

            Method[] methods = proxy.getClass().getDeclaredMethods();

            for(Method method : methods){
                PBefore pBefore = method.getAnnotation(PBefore.class);
                if(pBefore != null){
                    method.setAccessible(true);
                    method.invoke(proxy);
                }

            }



        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doAfter() {
        try {

            Method[] methods = proxy.getClass().getDeclaredMethods();

            for(Method method : methods){
                PAfter pAfter = method.getAnnotation(PAfter.class);
                if(pAfter != null){
                    method.setAccessible(true);
                    method.invoke(proxy);
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getProxyMethodName() {
        return proxyMethodName;
    }

    public void setProxyMethodName(String proxyMethodName) {
        this.proxyMethodName = proxyMethodName;
    }


    /**
     * 构建代理对象
     *
     * @param target
     * @return
     */
    public Object createProxyObject(Object target) {

        this.object = target;

        //该类用于生成代理对象
        Enhancer enhancer = new Enhancer();

        //设置目标类为代理对象的父类
        enhancer.setSuperclass(this.object.getClass());

        //设置回调
        enhancer.setCallback(this);

        return enhancer.create();
    }


}
