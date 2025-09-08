package com.empresa.banking.infrastructure.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Properties;


@Configuration
@EnableTransactionManagement
public class TransactionConfig {

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        transactionManager.setRollbackOnCommitFailure(true);
        transactionManager.setGlobalRollbackOnParticipationFailure(true);

        // ESTA ES LA CLAVE: Forzar el cleanup después del rollback
        transactionManager.setJpaProperties(getJpaProperties());
        return transactionManager;
    }

    private Properties getJpaProperties() {
        Properties props = new Properties();
        props.setProperty("hibernate.connection.provider_disables_autocommit", "true");
        props.setProperty("hibernate.connection.handling_mode", "DELAYED_ACQUISITION_AND_RELEASE_AFTER_TRANSACTION");
        return props;
    }
}