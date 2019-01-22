package com.dzcx.core.log.utils;

import java.lang.reflect.Field;

/**
 * user：yeszhjian on 2019/1/4 11:18
 * email：yeszhjian@163.com
 */
public class ReflectUtil {

    /**
     * 获取Object对象，指定成员变量属性值
     */
    public static Object getObjAttr(String fieldName, Object obj) {
        Object value = null;
        Class tmpClass = obj.getClass();
        while (tmpClass != null && !tmpClass.getName().toLowerCase().equals("java.lang.object")) {
            // 获取对象obj的所有属性域
            Field[] fields = tmpClass.getDeclaredFields();
            boolean isHit = false;
            for (Field field : fields) {
                // 对于每个属性，获取属性名
                String varName = field.getName();
                if (varName.equals(fieldName)) {
                    try {
                        boolean access = field.isAccessible();
                        if (!access) field.setAccessible(true);
                        //从obj中获取field变量
                        value = field.get(obj);
                        if (!access) field.setAccessible(false);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    isHit = true;
                    break;
                }
            }

            if (isHit) {
                break;
            } else {
                tmpClass = tmpClass.getSuperclass();
            }
        }

        return value;
    }

}
