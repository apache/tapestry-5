package org.example.app0.entities;

import java.io.Serializable;

public class CompoundIdEntityPK implements Serializable {
    private Long firstId;
    private Long secondId;

    public CompoundIdEntityPK(Long firstId, Long secondId) {
        this.firstId = firstId;
        this.secondId = secondId;
    }
}
