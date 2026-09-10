package org.pahappa.systems.egoSms.core.services.impl;

import com.googlecode.genericdao.search.Search;
import org.pahappa.systems.egoSms.core.dao.impl.BaseDAOImpl;
import org.pahappa.systems.egoSms.core.services.GenericService;
import org.sers.webutils.model.BaseEntity;
import org.sers.webutils.model.RecordStatus;
import org.sers.webutils.model.exception.OperationFailedException;
import org.sers.webutils.server.shared.CustomLogger;
import org.sers.webutils.server.shared.CustomLogger.LogSeverity;
import org.sers.webutils.server.shared.SharedAppData;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Transactional//if anything goes wrong, it rolls back
public abstract class GenericServiceImpl<T extends BaseEntity> extends BaseDAOImpl<T> implements GenericService<T> {
	
	
	/**
	 * 
	 * This method carries out the delete action for an entity instance
	 * 
	 */
	@Override
	public void deleteInstance(T instance) throws OperationFailedException {
		if(!isDeletable(instance))
			throw new OperationFailedException("Deletion is not yet supported for this instance");
		changeStatusToDeleted(instance);
	}

	/**
	 * Deactivates the instance by changing its status to deleted
	 * 
	 * @param instance
	 */
	private void changeStatusToDeleted(T instance) {
		CustomLogger.log(getClass(), LogSeverity.LEVEL_DEBUG,
				String.format("Instance is deletable! Now setting the audit trail."));
		instance.setChangedBy(SharedAppData.getLoggedInUser());
		instance.setDateChanged(new Date());
		instance.setRecordStatus(RecordStatus.DELETED);
		super.save(instance);
		CustomLogger.log(getClass(), LogSeverity.LEVEL_DEBUG, String.format("Set record to deleted!"));
	}

	
	/**
	 * Must be implemented by all classes that extend this abstract class.
	 * This method must be implemented  to specify whether instances of an entity
	 * can be deleted or not.
	 *
	 * @param instance
	 * @return
	 * @throws OperationFailedException
	 */
	public abstract boolean isDeletable(T instance) throws OperationFailedException;


	/**
	 * Retrieves items from the database based on the ID provided
	 */
	@Override
	public T getInstanceByID(String instance) {
		return super.searchUniqueByPropertyEqual("id", instance);
	}
	
	/**
	 * Counts the number of occurrences of what is specified as the search
	 */
	@Override
	public int countInstances(Search search) {
		return super.count(search);
	}

	/**
	 * Returns item being searched for from the database
	 */
	@Override
	public List<T> getInstances(Search search, int offset, int limit) {
		return super.search(search.setFirstResult(offset).setMaxResults(limit));	

	}
	
	/**
	 * Returns all instances from the database with a RecordStatus of ACTIVE
	 */
	@Override
	public List<T> getAllInstances() {
		Search search = new Search();
		search.addFilterEqual("recordStatus", RecordStatus.ACTIVE);
		return super.search(search);
	}

}

