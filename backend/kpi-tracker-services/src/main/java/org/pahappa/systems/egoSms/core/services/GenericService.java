package org.pahappa.systems.egoSms.core.services;

import com.googlecode.genericdao.search.Search;
import org.sers.webutils.model.exception.OperationFailedException;
import org.sers.webutils.model.exception.ValidationFailedException;

import java.util.List;

/**
 * Generic interface which is used my most services. It defines CRUD
 * methods for all entities that inherit/implement this interface.
 *
 * @param <T>
 */

public interface GenericService<T> {
	
	/**
	 * Saved <T> to the database after appropriate validation rules have been passed.
	 * 
	 * @param entityInstance
	 * @return
	 * @throws ValidationFailedException
	 */
	T saveInstance(T entityInstance) throws ValidationFailedException, OperationFailedException;
	
	/**
	 * Queries for a list of object instances that match the specified search
	 * criteria, from the specified offset and limit.
	 * 
	 * @param search
	 * @param offset
	 * @param limit
	 * @return
	 */

	List<T> getInstances(Search search, int offset, int limit);
	
	/**
	 * Returns the instance of the object represented by the specified identifier.
	 * 
	 * @return
	 */
	T getInstanceByID(String id);
	
	/**
	 * Queries for the number of object instances that match the specified search
	 * criteria.
	 * 
	 * @param search
	 * @return
	 */
	int countInstances(Search search);
	
	/**
	 * Deactivates the specified instance. Deactivated records can neither be used
	 * to create new records nor can they appear in lists of queried data from the
	 * database.
	 * 
	 * However, records already saved that reference the deactivated record still
	 * maintain that relationship. As a result, a deactivated record will appear in
	 * its relationships but only for viewing purposes.
	 * 
	 * This obviously means that we do not permanently delete records from the DB,
	 * but change record statuses.
	 * 
	 * @param instance
	 * @throws OperationFailedException
	 */
	void deleteInstance(T instance) throws OperationFailedException;
	
	/**
	 * Queries all contents of a specified entity
	 * 
	 * @return
	 */
	List<T> getAllInstances();
	
}