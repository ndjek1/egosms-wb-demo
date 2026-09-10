package org.pahappa.systems.egoSms.core.dao;

import com.googlecode.genericdao.dao.jpa.GenericDAO;
import org.sers.webutils.model.RecordStatus;

import java.util.List;

/**
 * * A Base Interface for all Data Access interfaces that enables the Data
 * Access Layer objects to use generics <br/>
 * <br/>
 * 
 * The BaseDao interface extends the {@link GenericDAO} interface to provide the
 * generalization of the Data Access objects. <br/>
 * <br/>
 * 
 * Usage of this interface can be seen below <br/>
 * <code>
 * 	public interface UserDAO extends BaseDao{@code <UserAccount>}
 * </code>
 * 
 * @author Deo
 * 
 * @param <T>
 */
public interface BaseDao<T> extends GenericDAO<T, String> {
	/**
	 * retrieves a list of active entities(whose record status is active) with a
	 * given property value is equal to the given value
	 * 
	 * @param property
	 * @param value
	 * @return
	 */
	List<T> searchByPropertyEqual(String property, Object value);

	/**
	 * retrieves a of type T active entities (whose record status is active) with a
	 * given property whose value is equal to the given value
	 */
	T searchUniqueByPropertyEqual(String property, Object value);

	/**
	 * searches for a list of entities of a given record status with a given
	 * property whose value is equal to the given value
	 * 
	 * @param property
	 * @param value
	 * @param recordStatus
	 * @return
	 */
	List<T> searchByPropertyEqual(String property, Object value, RecordStatus recordStatus);

	/**
	 * searches for a list of all entities of a given record status
	 * 
	 * @param recordStatus
	 * @return
	 */
	List<T> searchByRecordStatus(RecordStatus recordStatus);

	/**
	 * searches for an entity of a given record status with a given property whose
	 * value is equla to the given value
	 * 
	 * @param property
	 * @param value
	 * @param recordStatus
	 * @return
	 */
	T searchUniqueByPropertyEqual(String property, Object value, RecordStatus recordStatus);

	/**
	 * deletes a given entity. This doesn't remove the entity entirely from the
	 * system, but marks the record as deleted and leave it intact for historical
	 * purposes
	 * 
	 * @param entity
	 */
	void delete(T entity);

	/**
	 * updates a given entity
	 * 
	 * @param entity
	 */
	void update(T entity);

	/**
	 * adds a given entity
	 * 
	 * @param entity
	 */
	void add(T entity);

	T directSave(T entity);

	T directMerge(T entity);

	/**
	 * This method is used to
	 * 
	 * @param entity
	 */
	void directUpdate(T entity);

	void saveBG(T obj);

	void updateBG(Object obj);

	T mergeBG(T obj);

}
