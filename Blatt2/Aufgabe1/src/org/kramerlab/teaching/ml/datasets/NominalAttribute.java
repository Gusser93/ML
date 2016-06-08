//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.kramerlab.teaching.ml.datasets;

import java.util.ArrayList;
import java.util.List;

public class NominalAttribute extends Attribute {
    private List<NominalValue> values = new ArrayList();

    public NominalAttribute(String name) {
        super(name);
    }

    public void addValue(NominalValue val) {
        if(!this.values.contains(val)) {
            this.values.add(val);
        }

    }

    @Override
    public boolean isNominal() {
        return true;
    }

    public NominalValue getValue(int index) {
        return (NominalValue)this.values.get(index);
    }

    public int getNumberOfValues() {
        return this.values.size();
    }

    public String toString() {
        String att = "@attribute " + this.getName() + " {";

        int lastComma;
        for(lastComma = 0; lastComma < this.getNumberOfValues(); ++lastComma) {
            att = att + this.getValue(lastComma).getValue() + ",";
        }

        lastComma = att.length() - 1;
        att = att.substring(0, lastComma);
        att = att + "}";
        return att;
    }
}
