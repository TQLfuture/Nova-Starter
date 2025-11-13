package com.starter.nova.mybatis.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

/**
 * @author tql
 * @version 1.0
 */
@Component
@Slf4j
public class TransactionUtil {

    @Autowired
    private PlatformTransactionManager transactionManager;

    public void execute(Runnable runnable) {
        execute(runnable, TransactionDefinition.PROPAGATION_REQUIRED);
    }

    public void execute(Runnable runnable, int propagationBehavior) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(propagationBehavior);
        transactionTemplate.execute(status -> {
            runnable.run();
            return null;
        });
    }

    public <T> T executeWithResult(Supplier<T> supplier) {
        return executeWithResult(supplier, TransactionDefinition.PROPAGATION_REQUIRED);
    }

    public <T> T executeWithResult(Supplier<T> supplier, int propagationBehavior) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(propagationBehavior);
        return transactionTemplate.execute(status -> {
            return supplier.get();
        });
    }
}
