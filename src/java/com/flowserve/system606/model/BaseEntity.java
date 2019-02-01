package com.flowserve.system606.model;

import java.io.Serializable;

public abstract class BaseEntity<T extends Serializable> {

    // Return the primary key.
    public abstract T getId();

    /* As a starting point, we provide a basic mean for entities to test for equality using their JPA "id".
     *
     * Please note that THIS IS NOT ALWAYS ACCEPTABLE since newly generated
     * entities may break Set/Collection semantics. Override equals in subclasses when necessary.
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BaseEntity)) {
            return false;
        }
        if (getId() == null || ((BaseEntity<?>) obj).getId() == null) {
            return false;
        }
        if (!getId().equals(((BaseEntity<?>) obj).getId())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return getId() == null ? super.hashCode() : getId().hashCode();
    }

}
