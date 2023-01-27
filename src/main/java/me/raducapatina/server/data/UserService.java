package me.raducapatina.server.data;

import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Transient;
import javax.transaction.Transactional;
import java.util.List;

@AllArgsConstructor
public class UserService implements Service<User> {

    private static final Logger logger = LogManager.getLogger(UserService.class);

    private final EntityManager entityManager;

    @Override
    public User findById(Long id) throws Exception {
        try {
            return entityManager.find(User.class, id);
        } catch (NoResultException e) {
            logger.error(e);
            return null;
        }
    }

    public User findByUsername(String username) throws Exception {

        return entityManager.createNamedQuery("User.findByUsername", User.class)
                .setParameter("username", username)
                .getSingleResult();
    }

    public boolean existsByUsername(String username) throws Exception {
        User byUsername;
        try {
             byUsername = findByUsername(username);
        } catch (NoResultException e) {
            return false;
        }

        return byUsername != null;
    }

    @Override
    public void add(User user) {
        entityManager.getTransaction().begin();
        entityManager.persist(user);
        entityManager.getTransaction().commit();
    }


    @Override
    public void update(User element) {
        entityManager.getTransaction().begin();
        entityManager.merge(element);
        entityManager.getTransaction().commit();
    }

    @Override
    public boolean deleteById(Long id) throws Exception {
        User byId = findById(id);

        if (byId == null)
            return false;

        entityManager.getTransaction().begin();
        entityManager.remove(byId);
        entityManager.getTransaction().commit();
        return true;

    }

    public boolean deleteByUsername(String username) throws Exception {
        User byUsername = findByUsername(username);

        if (byUsername == null)
            return false;

        entityManager.getTransaction().begin();
        entityManager.remove(byUsername);
        entityManager.getTransaction().commit();
        return true;

    }

    @Transactional
    public List<User> getAllUsers() {
        List<User> resultList = entityManager.createQuery("select u from User u", User.class).getResultList();
        return resultList;
    }
}
