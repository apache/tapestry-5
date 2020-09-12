package org.apache.tapestry5.ioc.test.internal.util;


/**
 * Used by {@link ioc.specs.InheritanceSearchSpec}.
 * Kind of dubious that anything Playable is also Drivable, but this is an edge case.
 */
public interface Playable extends Drivable
{
    void play();
}
