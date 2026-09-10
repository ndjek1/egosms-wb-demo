package org.pahappa.systems.egoSms.views.dialogs;

import org.primefaces.PrimeFaces;
import org.sers.webutils.model.BaseEntity;

import javax.faces.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

public abstract class DialogForm<T extends BaseEntity> extends FormPresenter<T> {

	private static final long serialVersionUID = 1L;
	private String name;
	private int width;
	private int height;
	private Map<String, Object> closeMap;

	/**
	 * @param name
	 * @param width
	 * @param height
	 */
	public DialogForm(String name, int width, int height) {
		super();
		this.name = name;
		this.width = width;
		this.height = height;
		this.closeMap = new HashMap<String, Object>();
	}

	//@SuppressWarnings("deprecation")
	public void show(ActionEvent actionEvent) {
		Map<String, Object> options = new HashMap<String, Object>();
		options.put("modal", true);
		options.put("draggable", true);
		options.put("resizable", true);
		options.put("contentHeight", height);
		options.put("contentWidth", width);
		options.put("style", "");
		options.put("widgetVar", this.name);
		options.put("id", this.name);
		this.closeMap.put("widgetVar", this.name);
		this.closeMap.put("id", this.name);		
		PrimeFaces.current().dialog().openDynamic(name, options, null);
	}

	public void save() throws Exception {
		try {
			persist();
			resetModal();
			MessageComposer.info("Action Successful", "Successfully saved record");
			hide();
		} catch (Exception e) {
			MessageComposer.error("Action Failure", e.getMessage());
		}
	}

	@SuppressWarnings("deprecation")
	public void hide() {
		PrimeFaces.current().dialog().closeDynamic(this.name);
	}

	/**
	 * Do nothing in this abstract class. To be overridden by Subclasses that
	 * wish to set any other form properties in case the set model is not null
	 * i.e editing mode.
	 */
	@Override
	public void setFormProperties() {
		super.isEditing = true;
	}

	@Override
	public void resetModal() {
		super.isEditing = false;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param width
	 *            the width to set
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * @param height
	 *            the height to set
	 */
	public void setHeight(int height) {
		this.height = height;
	}
}