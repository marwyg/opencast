package org.opencastproject.adopterstatistics.registration;

import org.opencastproject.util.NotFoundException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

public class AdopterStatisticsRegistrationRepositoryImpl implements AdopterStatisticsRegistrationRepository {

  /** The factory used to generate the entity manager. */
  protected EntityManagerFactory emf = null;

  /**
   * The setter for OSGI.
   * @param emf The entity manager factory.
   */
  void setEntityManagerFactory(EntityManagerFactory emf) {
    this.emf = emf;
  }

  @Override
  public void save() {
    EntityManager em = null;
    EntityTransaction tx = null;
    try {
      em = emf.createEntityManager();
      tx = em.getTransaction();
      tx.begin();
      AdopterStatisticsRegistrationForm form = findBy(orgId, em);
      if (organization == null)
        throw new NotFoundException("Organization " + orgId + " does not exist");

      em.remove(organization);
      tx.commit();
    } catch (NotFoundException e) {
      throw e;
    } catch (Exception e) {
      logger.error("Could not delete organization: {}", e.getMessage());
      if (tx.isActive()) {
        tx.rollback();
      }
      throw new OrganizationDatabaseException(e);
    } finally {
      if (em != null)
        em.close();
    }
  }

  @Override
  public void delete() {

  }

  AdopterStatisticsRegistrationForm findByAdopterKey(String adopterKey) {
    return null;
  }

}
