//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.kramerlab.teaching.ml.datasets;

public abstract class Attribute {
    private String name;

    public Attribute(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public boolean equals(Object o) {
        if(o == null) {
            return false;
        } else {
            Attribute att = (Attribute)o;
            return this.getName().equals(att.getName());
        }
    }

    public boolean isNominal() {
        return false;
    }

    public boolean isNumeric() {
        return false;
    }

    public int hashCode() {
        return this.getName().hashCode();
    }

    public String toString() {
        return "@attribute " + this.name;
    }
}
