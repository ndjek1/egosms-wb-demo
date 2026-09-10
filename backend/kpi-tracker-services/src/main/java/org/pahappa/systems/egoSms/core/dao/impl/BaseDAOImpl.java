package org.pahappa.systems.egoSms.core.dao.impl;

import com.googlecode.genericdao.dao.jpa.GenericDAO;
import com.googlecode.genericdao.dao.jpa.GenericDAOImpl;
import com.googlecode.genericdao.search.Search;
import com.googlecode.genericdao.search.jpa.JPASearchProcessor;
import org.apache.commons.lang.StringUtils;
import org.hibernate.SessionFactory;
import org.sers.webutils.model.BaseEntity;
import org.sers.webutils.model.RecordStatus;
import org.sers.webutils.model.security.User;
import org.sers.webutils.server.core.dao.BaseDao;
import org.sers.webutils.server.core.security.service.impl.CustomSessionProvider;
import org.sers.webutils.server.core.utils.ApplicationContextProvider;
import org.sers.webutils.server.shared.SharedAppData;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

/**
 * Abstract Base class for all Data Access interface Implementations and
 * abstracts<br/>
 * </br/>
 * 
 * This class requires the hibernate {@link SessionFactory} <br/>
 * <br/>
 * 
 * This class also extends the {@link GenericDAOImpl} which is a hibernate
 * specific implementation of the {@link GenericDAO} interface. This allows our
 * implementing classes to use generics when providing CRUD operations *
 * 
 * @param <T>
 */
public abstract class BaseDAOImpl<T> extends GenericDAOImpl<T, String> implements BaseDao<T> {

	protected EntityManager entityManager;

	@Autowired//this is a spring annotation that injects the session factory
	private SessionFactory sessionFactory;

	// private Logger log = LoggerFactory.getLogger(BaseDAOImpl.class);

	@PersistenceContext
	@Override
	public void setEntityManager(EntityManager entityManager) {
		super.setEntityManager(entityManager);
		this.entityManager = entityManager;
	}


	@Autowired
	@Override
	public void setSearchProcessor(JPASearchProcessor searchProcessor) {
		super.setSearchProcessor(searchProcessor);
	}

	@Override
	public List<T> searchByPropertyEqual(String property, Object value) {
		Search search = new Search();
		search.addFilterEqual(property, value);
		// search.addFilterEqual("recordStatus", RecordStatus.ACTIVE);
		return search(search);
	}

	@Override
	@SuppressWarnings("unchecked")
	public T searchUniqueByPropertyEqual(String property, Object value) {
		Search search = new Search();
		search.addFilterEqual(property, value);
		// search.addFilterEqual("recordStatus", RecordStatus.ACTIVE);
		return (T) searchUnique(search);
	}

	@Override
	public void delete(T entity) {
		if (entity != null) {
			super.remove(entity);
		}
	}

	@Override
	public void update(T entity) {
		save(entity);
	}

	@Override
	public void add(T entity) {
		save(entity);
	}

	@Override
	public List<T> searchByPropertyEqual(String property, Object value, RecordStatus recordStatus) {
		Search search = new Search();
		search.addFilterEqual(property, value);
		search.addFilterEqual("recordStatus", recordStatus);
		return search(search);
	}

	@Override
	@SuppressWarnings("unchecked")
	public T searchUniqueByPropertyEqual(String property, Object value, RecordStatus recordStatus) {
		Search search = new Search();
		search.addFilterEqual(property, value);
		search.addFilterEqual("recordStatus", recordStatus);
		return (T) searchUnique(search);
	}

	@Override
	public List<T> searchByRecordStatus(RecordStatus recordStatus) {
		Search search = new Search();
		search.addFilterEqual("recordStatus", recordStatus);
		return search(search);
	}

	@SuppressWarnings("unchecked")
	@Override
	public T save(T entity) {
		if (entity == null)
			return null;
		User logged = null;

		try {
			CustomSessionProvider customSessionProvider = ApplicationContextProvider
					.getBean(CustomSessionProvider.class);
			logged = customSessionProvider.getLoggedInUser();
		} catch (Exception e) {
		}

		if (logged != null) {
			if (entity instanceof BaseEntity) {
				BaseEntity obj = (BaseEntity) entity;
				if (StringUtils.isBlank(obj.getId()) || StringUtils.isEmpty(obj.getId())) {
					obj.setCreatedBy(logged);

					if (StringUtils.isBlank(obj.getCustomPropOne()) || StringUtils.isEmpty(obj.getCustomPropOne()))
						obj.setCustomPropOne(SharedAppData.getCustomPropOne());
				}
				obj.setChangedBy(logged);
				return super.save((T) obj);
			}
		}
		return super.save(entity);
	}

	@SuppressWarnings("unchecked")
	@Override
	public T merge(T entity) {
		if (entity == null)
			return null;

		User logged = null;

		try {
			CustomSessionProvider customSessionProvider = ApplicationContextProvider
					.getBean(CustomSessionProvider.class);
			logged = customSessionProvider.getLoggedInUser();
		} catch (Exception e) {
		}
		if (logged != null) {
			if (entity instanceof BaseEntity) {
				BaseEntity obj = (BaseEntity) entity;
				if (StringUtils.isBlank(obj.getId()) || StringUtils.isEmpty(obj.getId())) {
					obj.setCreatedBy(logged);

					if (StringUtils.isBlank(obj.getCustomPropOne()) || StringUtils.isEmpty(obj.getCustomPropOne())) {
						obj.setCustomPropOne(SharedAppData.getCustomPropOne());
					}
				}
				obj.setChangedBy(logged);

				return super.merge((T) obj);
			}
		}

		return super.merge(entity);
	}

	@Override
	public T directSave(T entity) {
		if (entity == null)
			return null;
		return super.save(entity);
	}

	@Override
	public T directMerge(T entity) {
		if (entity == null)
			return null;
		return super.merge(entity);
	}

	@Override
	public void directUpdate(T entity) {
		super.save(entity);
	}

	@Override
	public void saveBG(T obj) {
		sessionFactory.getCurrentSession().close();
		sessionFactory.getCurrentSession().beginTransaction();
		sessionFactory.getCurrentSession().save(obj);
		sessionFactory.getCurrentSession().flush();
		sessionFactory.getCurrentSession().getTransaction().commit();
		sessionFactory.getCurrentSession().close();
	}

	@Override
	public void updateBG(Object obj) {
		sessionFactory.getCurrentSession().close();
		sessionFactory.getCurrentSession().beginTransaction();
		sessionFactory.getCurrentSession().update(obj);
		sessionFactory.getCurrentSession().flush();

		sessionFactory.getCurrentSession().getTransaction().commit();
		sessionFactory.getCurrentSession().close();
	}

	@Override
	public T mergeBG(T obj) {
		sessionFactory.getCurrentSession().close();
		sessionFactory.getCurrentSession().beginTransaction();
		@SuppressWarnings("unchecked")
		T saved = (T) sessionFactory.getCurrentSession().merge(obj);
		sessionFactory.getCurrentSession().flush();

		sessionFactory.getCurrentSession().getTransaction().commit();
		sessionFactory.getCurrentSession().close();
		return saved;
	}
}
