package org.pahappa.systems.egoSms.views.dialogs;

import org.sers.webutils.client.controllers.WebAppExceptionHandler;
import org.sers.webutils.client.views.presenters.WebForm;
import org.sers.webutils.model.BaseEntity;

/**
 * Defines methods to be used by both {@link DialogForm} and {@link WebForm}
 * 
 * @param <T>
 */
abstract class FormPresenter<T extends BaseEntity> extends WebAppExceptionHandler {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected T model;
	protected boolean isEditing = false;

	/**
	 * Method that performs the appropriate server call to persist the entity
	 * into the database.
	 * 
	 * @throws Exception
	 */
	public abstract void persist() throws Exception;

	/**
	 * Method that calls the persist function and performs appropriate cleaning
	 * of the User Interface and redirections after saving, if any.
	 * 
	 * To be overridden by {@link DialogForm} and {@link WebForm}
	 * 
	 * @throws Exception
	 */
	public abstract void save() throws Exception;

	/**
	 * Method that creates a new instance of the model used by this
	 * {@link FormPresenter}
	 */
	public abstract void resetModal();

	/**
	 * @return the model
	 */
	public T getModel() {
		return model;
	}

	/**
	 * To be implemented by Subclasses that wish to set any other form
	 * properties in case the set model is not null i.e editing mode.
	 */
	public abstract void setFormProperties();

	/**
	 * Method that sets the Model used by this {@link FormPresenter}. In case
	 * the model is null, its set by a call to resetModel that provides a new
	 * instance.
	 * 
	 * @param model
	 */
	public void setModel(T model) {
		this.model = model;
		if (this.model == null) {
			resetModal();
		} else {
			setFormProperties();
		}
	}
}