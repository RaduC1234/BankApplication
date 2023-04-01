package me.raducapatina.server.data;

import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.transaction.Transactional;
import java.util.List;

@AllArgsConstructor
public class SubjectService implements Service<Subject> {

    private static final Logger logger = LogManager.getLogger(UserService.class);

    private final EntityManager entityManager;

    @Override
    public Subject findById(Long id) throws Exception {
        try {
            return entityManager.find(Subject.class, id);
        } catch (NoResultException e) {
            logger.error(e);
            return null;
        }
    }

    @Override
    @Transactional
    public void add(Subject element) throws Exception {
        entityManager.getTransaction().begin();
        entityManager.persist(element);
        entityManager.getTransaction().commit();
    }

    @Override
    public void update(Subject element) throws Exception {
        entityManager.getTransaction().begin();
        entityManager.merge(element);
        entityManager.getTransaction().commit();
    }

    @Override
    public boolean deleteById(Long id) throws Exception {
        Subject byId = findById(id);

        if (byId == null)
            return false;

        entityManager.getTransaction().begin();
        entityManager.remove(byId);
        entityManager.getTransaction().commit();
        return true;

    }

    @Transactional
    public List<Subject> getAllSubjects() {
        return entityManager.createQuery("select u from Subject u", Subject.class).getResultList();
    }
}
