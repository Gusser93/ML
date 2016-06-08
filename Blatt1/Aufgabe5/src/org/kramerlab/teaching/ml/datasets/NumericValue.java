//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.kramerlab.teaching.ml.datasets;

public class NumericValue extends Value {
    private double value;

    public NumericValue(double value) {
        this.value = value;
    }

    public double getValue() {
        return this.value;
    }

    public boolean equals(Object o) {
        if(o == null) {
            return false;
        } else {
            NumericValue val = (NumericValue)o;
            return this.getValue() == val.getValue();
        }
    }

    public boolean smallerThan(NumericValue val) {
        return val == null?false:this.getValue() < val.getValue();
    }

    public boolean greaterThan(NumericValue val) {
        return val == null?false:this.getValue() > val.getValue();
    }

    public int hashCode() {
        return (int)this.value;
    }

    public String toString() {
        return Double.toString(this.getValue());
    }
}
