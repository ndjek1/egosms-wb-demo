package org.pahappa.systems.egoSms.core.services.impl;

import org.apache.commons.lang.StringUtils;
import org.pahappa.systems.egoSms.core.services.MailSettingService;
import org.pahappa.systems.egoSms.models.MailSetting;
import org.sers.webutils.model.exception.ValidationFailedException;
import org.sers.webutils.server.core.dao.impl.BaseDAOImpl;
import org.sers.webutils.server.core.utils.MailUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MailSettingServiceImpl extends BaseDAOImpl<MailSetting> implements MailSettingService{

	@Override
	public MailSetting saveMailSetting(MailSetting mailSetting) throws ValidationFailedException {
		if(StringUtils.isBlank(mailSetting.getSenderAddress()))
			throw new ValidationFailedException("Missing sender address");
		
		if(!MailUtils.Util.getInstance().isValidEmail(mailSetting.getSenderAddress()))
			throw new ValidationFailedException("Invalid sender address");
		
		if(StringUtils.isBlank(mailSetting.getSenderPassword()))
			throw new ValidationFailedException("Missing sender password");

		if(StringUtils.isBlank(mailSetting.getSenderSmtpHost()))
			throw new ValidationFailedException("Missing smtp host");
		
		if(StringUtils.isBlank(mailSetting.getSenderSmtpPort()))
			throw new ValidationFailedException("Missing smtp port");
		
		return super.save(mailSetting);
	}

	@Override
	public MailSetting getMailSetting() {
		if(super.findAll().size() > 0) {
			return super.findAll().get(0);
		}
		return null;
	}
	
}
