package org.pahappa.systems.egoSms.core.services.impl;

import org.hibernate.SessionFactory;
import org.sers.webutils.server.core.dao.MigrationDao;
import org.sers.webutils.server.core.service.migrations.MigrationTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

@Service
@Transactional
public class CustomMigrationService extends MigrationTemplate {

    @Autowired
    MigrationDao migrationDao;

    @Autowired
    SessionFactory sessionFactory;

    @Autowired
    CustomPermissionRoleMigrations permissionRoleMigrations;

    private final List<Class<?>> migrationClasses = Arrays
            .asList(new Class<?>[]{CustomPermissionRoleMigrations.class});

    public static boolean EXECUTED_MIGRATIONS = false;

    @PostConstruct
    public void execute() {
        System.out.println("Executing migrations...");
        EXECUTED_MIGRATIONS = super.executeMigrations(EXECUTED_MIGRATIONS, migrationClasses, migrationDao,
                sessionFactory);
        System.out.println("Executed migrations...");
    }

    public Object getBean(Class<?> klass) throws Exception {
        for (Field field : CustomMigrationService.class.getDeclaredFields())
            if (field.getType() == klass)
                return field.get(CustomMigrationService.this);
        throw new Exception("No bean declared for class " + klass + " in this class");
    }
}
