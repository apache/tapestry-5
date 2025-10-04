package org.apache.tapestry5.integration.app1;

public class GenericsEntity<T> {

    private final String id_;

    private final String label_;

    public GenericsEntity(String id, String label) {
        id_ = id;
        label_ = label;
    }

    @SuppressWarnings("unused")
    private String getId() {
        return id_;
    }

    @SuppressWarnings("unused")
    private String getLabel() {
        return label_;
    }
}
