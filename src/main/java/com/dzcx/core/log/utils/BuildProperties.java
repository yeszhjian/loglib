package com.dzcx.core.log.utils;

import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

/**
 * Created by chen3 on 2017/9/21.
 */

public class BuildProperties {
    private static BuildProperties ourInstance;

    public static BuildProperties getInstance() {

        if (ourInstance == null) {
            ourInstance = new BuildProperties();
        }
        return ourInstance;

    }
    private Properties properties;
    private BuildProperties() {
        properties = new Properties();
        try {
            properties.load(new FileInputStream(new File(Environment.getRootDirectory(), "build.prop")));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public String getAll() {
        StringBuilder builder = new StringBuilder();
        Set<Object> objects = properties.keySet();
        Iterator<Object> iterator = objects.iterator();
        while (iterator.hasNext()) {
            Object next = iterator.next();
            builder.append(next + "=");
            builder.append(properties.getProperty(next.toString()));
            builder.append("\n");
        }
        return builder.toString();
    }
}
