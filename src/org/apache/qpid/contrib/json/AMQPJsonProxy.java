
package org.apache.qpid.contrib.json;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author zdc
 * @since 2015年5月27日
 */
public class AMQPJsonProxy implements InvocationHandler {

    private Object obj;

    public AMQPJsonProxy(Object obj) {
        super();
        this.obj = obj;
    }

    public static Object newInstance(Object obj) {
        return Proxy.newProxyInstance(obj.getClass().getClassLoader(), obj.getClass().getInterfaces(), new AMQPJsonProxy(obj));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        Class<?>[] params = method.getParameterTypes();

        // equals and hashCode are special cased
        if (methodName.equals("equals") && params.length == 1 && params[0].equals(Object.class)) {
            Object value = args[0];
            if (value == null || !Proxy.isProxyClass(value.getClass())) {
                return false;
            }

            // AMQPJsonProxy handler = (AMQPJsonProxy) Proxy.getInvocationHandler(value);

            return true;
        } else if (methodName.equals("hashCode") && params.length == 0) {
            return false;
        } else if (methodName.equals("toString") && params.length == 0) {
            return "[JsonProxy " + proxy.getClass() + "]";
        } else {
            Object result = method.invoke(obj, args);
            return result;
        }

    }

}
