package me.raducapatina.server.data;

import me.raducapatina.server.util.HibernateUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.Query;

public class DatabaseManager {

    private final static Logger logger = LogManager.getLogger(DatabaseManager.class);
    private static DatabaseManager instance = new DatabaseManager();

    private DatabaseManager() {
    }

    public static synchronized DatabaseManager getInstance() {
        return instance;
    }

    public UserService getUserService() {
        return new UserService(getEntityManager());
    }

    public ArticleService getArticleService() {
        return new ArticleService(getEntityManager());
    }

    public SubjectService getSubjectService() {
        return new SubjectService(getEntityManager());
    }

    public Query createSQLNativeQuery(String query) {
        return getEntityManager().createNativeQuery(query);
    }

    public synchronized EntityManager getEntityManager() {
        return HibernateUtil.getSessionFactory().openSession().getEntityManagerFactory().createEntityManager();
    }

}
