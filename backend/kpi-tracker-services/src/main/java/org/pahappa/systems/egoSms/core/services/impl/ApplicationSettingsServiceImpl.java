package org.pahappa.systems.egoSms.core.services.impl;

import com.googlecode.genericdao.search.Search;
import org.pahappa.systems.egoSms.core.services.ApplicationSettingsService;
import org.pahappa.systems.egoSms.utils.Validate;
import org.pahappa.systems.egoSms.models.settings.ApplicationSettings;
import org.sers.webutils.model.RecordStatus;
import org.sers.webutils.model.exception.OperationFailedException;
import org.sers.webutils.model.exception.ValidationFailedException;
import org.sers.webutils.server.core.utils.MailUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ApplicationSettingsServiceImpl extends GenericServiceImpl<ApplicationSettings> implements ApplicationSettingsService {
    @Override
    public ApplicationSettings saveInstance(ApplicationSettings entityInstance) throws ValidationFailedException, OperationFailedException {
        Validate.notNull(entityInstance, "Missing application settings details");
        Validate.notNull(entityInstance.getSenderAddress(), "Missing sender address");
        Validate.notNull(entityInstance.getSenderPassword(), "Missing sender password");
        Validate.notNull(entityInstance.getSenderSmtpHost(), "Missing smtp host");
        Validate.notNull(entityInstance.getSenderSmtpPort(), "Missing smtp port");

        if (!MailUtils.Util.getInstance().isValidEmail(entityInstance.getSenderAddress()))
            throw new ValidationFailedException("Invalid sender address");

        ApplicationSettings existingSetting = getActiveApplicationSettings();
        if (existingSetting != null && !existingSetting.equals(entityInstance))
            throw new ValidationFailedException(
                    "System settings already exist in the database. Retrieve a copy and make appropriate edits.");

        return super.save(entityInstance);
    }

    @Override
    public boolean isDeletable(ApplicationSettings instance) throws OperationFailedException {
        return false;
    }

    @Override
    public ApplicationSettings getActiveApplicationSettings() {
        return super.searchUnique(new Search().addFilterEqual("recordStatus", RecordStatus.ACTIVE));
    }
}
