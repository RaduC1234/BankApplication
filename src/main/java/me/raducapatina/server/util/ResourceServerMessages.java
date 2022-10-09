package me.raducapatina.server.util;

import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;

public class ResourceServerMessages extends ResourceBundle {

    private static ResourceServerMessages INSTANCE = new ResourceServerMessages();
    private static ResourceBundle BUNDLE;
    private Locale locale = Locale.getDefault();

    public static ResourceServerMessages getInstance() {
        return INSTANCE;
    }

    protected ResourceServerMessages() {
       BUNDLE = ResourceBundle.getBundle("ServerMessage", locale);
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public static String getObjectAsString(String key) {
        return BUNDLE.getObject(key).toString();
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
