package com.dzcx.core.log.utils;

import android.content.ComponentName;
import android.content.Intent;
import android.text.TextUtils;

import com.dzcx.core.log.bean.MsgModel;
import com.dzcx.core.log.config.EventType;
import com.dzcx.core.log.logdb.LogMsgManager;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by chen3 on 2017/12/5.
 */

public class HookUtils {


    private HookUtils() {

    }

    public static void hook() {
        new HookUtils().hookAms();
    }


    public void hookAms() {

        //一路反射，直到拿到IActivityManager的对象
        try {
            Class<?> ActivityManagerNativeClss = Class.forName("android.app.ActivityManagerNative");
            Field defaultFiled = ActivityManagerNativeClss.getDeclaredField("gDefault");
            defaultFiled.setAccessible(true);
            Object defaultValue = defaultFiled.get(null);
            //反射SingleTon
            Class<?> SingletonClass = Class.forName("android.util.Singleton");
            Field mInstance = SingletonClass.getDeclaredField("mInstance");
            mInstance.setAccessible(true);
            //到这里已经拿到ActivityManager对象
            Object iActivityManagerObject = mInstance.get(defaultValue);
            //开始动态代理，用代理对象替换掉真实的ActivityManager，瞒天过海
            Class<?> IActivityManagerIntercept = Class.forName("android.app.IActivityManager");

            AmsInvocationHandler handler = new AmsInvocationHandler(iActivityManagerObject);

            Object proxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[]{IActivityManagerIntercept}, handler);

            //现在替换掉这个对象
            mInstance.set(defaultValue, proxy);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private class AmsInvocationHandler implements InvocationHandler {

        private Object iActivityManagerObject;

        private AmsInvocationHandler(Object iActivityManagerObject) {
            this.iActivityManagerObject = iActivityManagerObject;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

            //我要在这里搞点事情
            String targetMethodName = "startActivity";
            if (targetMethodName.equals(method.getName())) {

                for (int i = 0; i < args.length; i++) {
                    Object arg = args[i];
                    if (arg instanceof Intent) {
                        Intent intent = (Intent) args[i];
                        ComponentName component = intent.getComponent();
                        if (component != null && !TextUtils.isEmpty(component.getClassName())) {
                            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
                            int index = getIndex(stackTrace);
                            if (index > -1) {
                                int length = index + 3;
                                if (index + 3 > stackTrace.length) {
                                    length = stackTrace.length;
                                }
                                StringBuilder builder = new StringBuilder();
                                builder.append("{\"type\":\"startActivity\",\"activityName\":\"");
                                builder.append(component.getClassName());
                                builder.append("\",\"msg\":[");
                                for (; index < length; ++index) {
                                    StackTraceElement element = stackTrace[index];
                                    builder.append("{\"className\":\"");
                                    builder.append(element.getClassName());
                                    builder.append("\",\"methodName\":\"");
                                    builder.append(element.getMethodName());
                                    builder.append("\",\"lineNumber\":");
                                    builder.append(element.getLineNumber());
                                    builder.append("},");
                                }
                                builder.deleteCharAt(builder.length() - 1);
                                builder.append("]}");
                                MsgModel model = new MsgModel();
                                model.setType(EventType.TYPE_START_ACTIVITY);
                                model.setMsg(builder.toString());
                                LogMsgManager.getInstance().addLogMsg(model);
                            }
                        }
                    }
                }

            }
            return method.invoke(iActivityManagerObject, args);
        }

        private int getIndex(StackTraceElement[] stackTrace) {
            int index = -1;
            for (int j = stackTrace.length - 1; j > -1; --j) {
                StackTraceElement element = stackTrace[j];
                String methodName = element.getMethodName();
                if ("startActivity".equals(methodName)) {
                    index = j + 1;
                    break;
                } else {
                    continue;
                }
            }
            return index;
        }
    }
}

