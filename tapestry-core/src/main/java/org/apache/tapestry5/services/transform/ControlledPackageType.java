package org.apache.tapestry5.services.transform;

/**
 * Defines how a particular controlled package is processed. Currently there is only one option, but further options
 * (to allow a package to be reloaded but not transformed, and to allow a package to be transformed but not as
 * components)
 * may be supported in the future.
 * 
 * @since 5.3
 */
public enum ControlledPackageType
{
    /**
     * Top-level classes within the packages are components, that are transformed according to
     * {@link ComponentClassTransformWorker2}. All top-level classes must be components.
     */
    COMPONENT;
}
