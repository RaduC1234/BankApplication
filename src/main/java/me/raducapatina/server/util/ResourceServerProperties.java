package me.raducapatina.server.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;

public class ResourceServerProperties extends ResourceBundle {

    private static ResourceServerProperties INSTANCE = new ResourceServerProperties();
    private static ResourceBundle BUNDLE;

    public static ResourceServerProperties getInstance() {
        return INSTANCE;
    }

    private ResourceServerProperties() {
        File file = new File("");
        URL[] urls = new URL[0];
        try {
            urls = new URL[]{file.toURI().toURL()};
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        ClassLoader loader = new URLClassLoader(urls);
        BUNDLE = ResourceBundle.getBundle("server", Locale.getDefault(), loader);
    }

    @Override
    protected Object handleGetObject(String key) {
        return BUNDLE.getObject(key);
    }

    @Override
    public Enumeration<String> getKeys() {
        return BUNDLE.getKeys();
    }
}
