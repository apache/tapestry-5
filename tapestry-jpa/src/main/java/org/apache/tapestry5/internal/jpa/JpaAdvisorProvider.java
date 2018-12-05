package org.apache.tapestry5.internal.jpa;

import org.apache.tapestry5.plastic.MethodAdvice;

import javax.persistence.PersistenceContext;

public interface JpaAdvisorProvider {
    MethodAdvice getAdvice(PersistenceContext context);
}
