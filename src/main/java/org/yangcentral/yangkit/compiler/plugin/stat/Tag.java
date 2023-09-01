package org.yangcentral.yangkit.compiler.plugin.stat;

import javax.xml.namespace.QName;

public class Tag {
    private String name;
    private String field;
    private String value;

    public Tag(String name, String field) {
        this.name = name;
        this.field = field;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getField() {
        return field;
    }
}
