package me.raducapatina.server.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.AllArgsConstructor;
import me.raducapatina.server.MainServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.io.File;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@AllArgsConstructor
public class ArticleService implements Service<Article> {

    private static final Logger logger = LogManager.getLogger(ArticleService.class);

    private final EntityManager entityManager;

    @Override
    public Article findById(Long id) throws Exception {
        try {
            return entityManager.find(Article.class, id);
        } catch (NoResultException e) {
            logger.error(e);
            return null;
        }
    }

    public Article findByTitle(String title) {
        return entityManager.createNamedQuery("Article.findByTitle", Article.class)
                .setParameter("title", title)
                .getSingleResult();
    }

    @Override
    public void add(Article element) throws Exception {
        entityManager.getTransaction().begin();
        entityManager.persist(element);
        entityManager.getTransaction().commit();
    }

    @Override
    public void update(Article element) throws Exception {
        entityManager.getTransaction().begin();
        entityManager.merge(element);
        entityManager.getTransaction().commit();
    }

    @Override
    public boolean deleteById(Long id) throws Exception {
        Article byId = findById(id);

        if (byId == null)
            return false;

        entityManager.getTransaction().begin();
        entityManager.remove(byId);
        entityManager.getTransaction().commit();
        return true;
    }

    public boolean deleteByTitle(String title) {
        Article article = findByTitle(title);

        if (article == null) {
            return false;
        }

        entityManager.getTransaction().begin();
        entityManager.remove(article);
        entityManager.getTransaction().commit();
        return true;
    }

    public List<Article> getAllArticles() throws Exception {
        List<Article> unprocessedImages = entityManager.createQuery("SELECT a FROM Article a", Article.class).getResultList();

        List<Article> processedList = new ArrayList<>();

        for(Article article : unprocessedImages) {
            processedList.add(processArticle(article));
        }

        return processedList;
    }

    private Article processArticle(Article article) throws Exception {
        Article returnArticle = new ObjectMapper(new YAMLFactory()).readValue(new File(article.getDocumentPath()),Article.class);

        if(returnArticle.getImagePath() == null) {
            returnArticle.setImageBase64(Base64.getUrlEncoder().encodeToString(MainServer.class.getResourceAsStream("/article-default-image.png").readAllBytes()));
            return returnArticle;
        }
        returnArticle.setImageBase64(Base64.getUrlEncoder().encodeToString(article.getImagePath().getBytes()));

        return returnArticle;
    }
}
