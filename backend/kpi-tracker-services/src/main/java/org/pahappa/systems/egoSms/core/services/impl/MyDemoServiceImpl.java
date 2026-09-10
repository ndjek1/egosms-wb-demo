package org.pahappa.systems.egoSms.core.services.impl;

import org.pahappa.systems.egoSms.core.services.MyDemoService;
import org.pahappa.systems.egoSms.models.demo.MyDemo;
import org.pahappa.systems.egoSms.utils.Validate;
import org.sers.webutils.model.exception.OperationFailedException;
import org.sers.webutils.model.exception.ValidationFailedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MyDemoServiceImpl extends GenericServiceImpl<MyDemo> implements MyDemoService {

    @Override
    public MyDemo saveInstance(MyDemo entityInstance) throws ValidationFailedException, OperationFailedException {
        Validate.notNull(entityInstance, "Missing details");
        return save(entityInstance);
    }

    @Override
    public boolean isDeletable(MyDemo instance) throws OperationFailedException {
        return true;
    }
}
