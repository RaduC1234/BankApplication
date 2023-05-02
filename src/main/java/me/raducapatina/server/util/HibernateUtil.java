package me.raducapatina.server.util;

import me.raducapatina.server.data.Article;
import me.raducapatina.server.data.Grade;
import me.raducapatina.server.data.Subject;
import me.raducapatina.server.data.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.service.ServiceRegistry;

import java.util.Properties;

public class HibernateUtil {

    private static Logger logger = LogManager.getLogger(HibernateUtil.class);

    //XML based configuration
    private static SessionFactory sessionFactory;

    private static SessionFactory buildSessionFactory() {
        try {

            Properties settings = new Properties();
            settings.put(Environment.DRIVER, ResourceServerProperties.getInstance().getObject("hibernate.connection.driver_class"));
            settings.put(Environment.URL, ResourceServerProperties.getInstance().getObject("hibernate.connection.url"));
            settings.put(Environment.USER, ResourceServerProperties.getInstance().getObject("hibernate.connection.username"));
            settings.put(Environment.PASS, ResourceServerProperties.getInstance().getObject("hibernate.connection.password"));
            settings.put(Environment.DIALECT, ResourceServerProperties.getInstance().getObject("hibernate.connection.dialect"));
            settings.put(Environment.SHOW_SQL, ResourceServerProperties.getInstance().getObject("hibernate.connection.show_sql"));
            settings.put(Environment.HBM2DDL_AUTO, ResourceServerProperties.getInstance().getObject("hibernate.connection.hbm2ddl.auto"));

            Configuration configuration = new Configuration();
            configuration.setProperties(settings);
            configuration.addAnnotatedClass(User.class);
            configuration.addAnnotatedClass(Subject.class);
            configuration.addAnnotatedClass(Article.class);
            configuration.addAnnotatedClass(Grade.class);

            logger.info("Hibernate Configuration loaded");

            ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
            logger.info("Hibernate serviceRegistry created");

            SessionFactory sessionFactory = configuration.buildSessionFactory(serviceRegistry);

            return sessionFactory;
        } catch (Throwable ex) {

            logger.fatal("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static synchronized SessionFactory getSessionFactory() {
        if (sessionFactory == null) sessionFactory = buildSessionFactory();
        return sessionFactory;
    }
}
