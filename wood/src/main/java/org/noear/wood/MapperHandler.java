package org.noear.wood;

import org.noear.wood.utils.InvocationHandlerUtils;
import org.noear.wood.utils.ThrowableUtils;
import org.noear.wood.wrap.MethodWrap;
import org.noear.wood.xml.Namespace;

import java.lang.reflect.*;
import java.sql.SQLException;

class MapperHandler implements InvocationHandler {

    protected DbContext db;
    protected Class<?> mapperClz;

    protected MapperHandler(DbContext db, Class<?> mapperClz) {
        this.db = db;
        this.mapperClz = mapperClz;
    }

    private static IMapperInvoke annInvoke = new MapperInvokeForAnn();
    private static IMapperInvoke xmlInvoke = new MapperInvokeForXml();
    private static IMapperInvoke basInvoke = new MapperInvokeForBas();

    protected static UnsupportedOperationException UOE = new UnsupportedOperationException();

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            return invoke0(proxy, method, args);
        } catch (Throwable ex) {
            ex = ThrowableUtils.throwableUnwrap(ex);

            if (ex instanceof RuntimeException) {
                throw ex;
            } else if (ex instanceof SQLException) {
                throw ex;
            } else {
                throw new RuntimeException(ex);
            }
        }
    }

    public Object invoke0(Object proxy, Method method, Object[] args) throws Throwable {
        Class caller = method.getDeclaringClass();

        if (method.isDefault()) {
            return InvocationHandlerUtils.invokeDefault(proxy, method, args);
        } else {
            String sqlid = getSqlid(caller, method);
            MethodWrap mWrap = MethodWrap.get(method);

            //1.尝试有@Sql注解的
            Object tmp = annInvoke.call(proxy, db, sqlid, caller, mWrap, args);

            if (UOE.equals(tmp)) {
                //2.尝试有xml的
                tmp = xmlInvoke.call(proxy, db, sqlid, caller, mWrap, args);

                if (UOE.equals(tmp)) {
                    //3.尝试BaseMapper
                    tmp = basInvoke.call(proxy, db, sqlid, caller, mWrap, args);

                    if (UOE.equals(tmp)) {
                        //4.尝试Object的
                        if (Object.class == caller) {
                            String name = method.getName();
                            switch (name) {
                                case "toString":
                                    return "Wood mapper: " + mapperClz;
                                case "hashCode":
                                    return System.identityHashCode(proxy);
                                case "equals":
                                    return false;
                            }
                        }

                        if (UOE.equals(tmp)) {
                            throw new RuntimeException("Xmlsql does not exist:@" + sqlid);
                        }
                    }
                }
            }

            return tmp;
        }
    }

    public static String getSqlid(Class<?> mapperClz, Method method) {
        Namespace c_meta = mapperClz.getAnnotation(Namespace.class);
        String fun_name = method.getName();

        if (c_meta == null) {
            return mapperClz.getPackage().getName() + "." + fun_name;
        } else {
            return c_meta.value() + "." + fun_name;
        }
    }


}
