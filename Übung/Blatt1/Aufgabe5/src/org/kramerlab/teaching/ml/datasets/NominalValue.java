//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.kramerlab.teaching.ml.datasets;

public class NominalValue extends Value {
    private String value;

    public NominalValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public boolean equals(Object o) {
        if(o == null) {
            return false;
        } else {
            NominalValue val = (NominalValue)o;
            return this.getValue().equals(val.getValue());
        }
    }

    public int hashCode() {
        return this.value.hashCode();
    }

    public String toString() {
        return this.getValue();
    }
}
