package org.apache.tapestry5.internal.jpa;

import org.apache.tapestry5.jpa.EntityTransactionManager;
import org.apache.tapestry5.plastic.MethodAdvice;

import javax.persistence.PersistenceContext;

public class JpaAdvisorProviderImpl implements JpaAdvisorProvider {
    private final MethodAdvice shared;
    private final EntityTransactionManager transactionManager;

    public JpaAdvisorProviderImpl(EntityTransactionManager transactionManager) {
        this.shared = new CommitAfterMethodAdvice(transactionManager, null);
        this.transactionManager = transactionManager;
    }
    @Override
    public MethodAdvice getAdvice(PersistenceContext context) {
        return context == null ? shared : new CommitAfterMethodAdvice(transactionManager,context.unitName());
    }
}
