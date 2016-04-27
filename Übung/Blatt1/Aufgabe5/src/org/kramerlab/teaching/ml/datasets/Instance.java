//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.kramerlab.teaching.ml.datasets;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class Instance {
    private HashMap<Attribute, Value> values = new HashMap();

    public Instance() {
    }

    public void addValue(Attribute att, Value val) {
        this.values.put(att, val);
    }

    public boolean hasAttribute(Attribute att) {
        return this.values.containsKey(att);
    }

    public Value getValue(Attribute att) {
        return (Value)this.values.get(att);
    }

    public String toString(List<Attribute> attributes) {
        String inst = "";
        Iterator lastComma = attributes.iterator();

        while(lastComma.hasNext()) {
            Attribute att = (Attribute)lastComma.next();
            if(this.hasAttribute(att)) {
                inst = inst + this.getValue(att).toString() + ",";
            } else {
                inst = inst + ",";
            }
        }

        int lastComma1 = inst.length() - 1;
        return inst.substring(0, lastComma1);
    }
}
