package org.example.app0.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

/**
 * This class is an example for an entity that uses a compound id. This triggers a bug with tapestry <= 5.8.6 during
 * initialisation, see TAP-123.
 */
@IdClass(CompoundIdEntityPK.class)
@Entity
public class CompoundIdEntity {
    @Id private Long firstId;
    @Id private Long secondId;
}
