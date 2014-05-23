package org.apache.tapestry5.internal.plastic;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;

import org.apache.tapestry5.plastic.AnnotationAccess;

/**
 * Delegating annotation access is used when resolving annotations on a class; it searches the primary
 * annotation access and if not found there (and {@link Inherited} is on the annotation being searched),
 * it searches in the inherited access.
 */
public class DelegatingAnnotationAccess implements AnnotationAccess
{
    private final AnnotationAccess primary;

    private final AnnotationAccess inherited;

    public DelegatingAnnotationAccess(AnnotationAccess primary, AnnotationAccess inherited)
    {
        this.primary = primary;
        this.inherited = inherited;
    }

    private boolean isInherited(Class<? extends Annotation> annotationType)
    {
        return annotationType.getAnnotation(Inherited.class) != null;
    }

    @Override
    public <T extends Annotation> boolean hasAnnotation(Class<T> annotationType)
    {
        if (primary.hasAnnotation(annotationType))
            return true;

        return isInherited(annotationType) && inherited.hasAnnotation(annotationType);
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationType)
    {
        T fromPrimary = primary.getAnnotation(annotationType);

        if (fromPrimary != null)
            return fromPrimary;

        return isInherited(annotationType) ? inherited.getAnnotation(annotationType) : null;
    }
}
