package me.raducapatina.server.data;

import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

//todo: fix SQL injection
@AllArgsConstructor
public class UserService implements Service<User> {

    private static final Logger logger = LogManager.getLogger(UserService.class);

    private final EntityManager entityManager;

    @Override
    public User findById(Long id) throws Exception {
        try {
            User user = entityManager.find(User.class, id);
            return user;
        } catch (NoResultException e) {
            logger.error(e);
            return null;
        }
    }

    public User findByUsername(String username) throws Exception {

        User user = entityManager.createNamedQuery("User.findByUsername", User.class)
                .setParameter("username", username)
                .getSingleResult();
        return user;
    }

    public boolean existsByUsername(String username) throws Exception {
        return findByUsername(username) != null;
    }

    @Override
    public void add(User user) {
        entityManager.getTransaction().begin();
        entityManager.persist(user);
        entityManager.getTransaction().commit();
    }


    @Override
    public void updateById(Long id) {
        entityManager.createNamedQuery("User.updateById", User.class)
                .setParameter("id", id)
                .executeUpdate();
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
}
