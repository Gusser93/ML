//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.kramerlab.teaching.ml.datasets;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Dataset {
    private String name;
    private List<Attribute> attributes;
    private List<Instance> instances;
    private int classIndex;

    public Dataset() {
        this.attributes = new ArrayList();
        this.instances = new ArrayList();
    }

    public Dataset(String name) {
        this();
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Attribute> getAttributes() {
        return this.attributes;
    }

    public void addAttribute(Attribute att) {
        this.attributes.add(att);
    }

    public int getNumberOfInstances() {
        return this.instances.size();
    }

    public Instance getInstance(int index) {
        return (Instance)this.instances.get(index);
    }

    public void addInstance(Instance inst) {
        this.instances.add(inst);
    }

    public void load(File file) throws Exception {
        String line = null;
        BufferedReader r = new BufferedReader(new FileReader(file));
        boolean headerSection = true;

        while((line = r.readLine()) != null) {
            line = line.trim();
            if(headerSection) {
                if(line.startsWith("@relation")) {
                    String inst = line.split(" ")[1];
                    this.setName(inst);
                } else if(line.startsWith("@attribute")) {
                    this.addAttribute(this.parseAttribute(line));
                }
            } else {
                Instance inst1 = this.parseInstance(line);
                if(inst1 != null) {
                    this.addInstance(inst1);
                }
            }

            if(line.startsWith("@data")) {
                headerSection = false;
            }
        }

        r.close();
        this.classIndex = this.attributes.size() - 1;
    }

    private Attribute parseAttribute(String line) {
        line = line.trim();
        int space1 = line.indexOf(" ");
        int space2 = line.indexOf(" ", space1 + 1);
        String name = line.substring(space1 + 1, space2);
        String type = line.substring(space2 + 1);
        Object att = null;
        if(type.indexOf("{") == -1) {
            att = new NumericAttribute(name);
        } else {
            type = type.replace("{", "");
            type = type.replace("}", "");
            type = type.replace(" ", "");
            String[] vals = type.split(",");
            att = new NominalAttribute(name);

            for(int i = 0; i < vals.length; ++i) {
                ((NominalAttribute)att).addValue(new NominalValue(vals[i]));
            }
        }

        return (Attribute)att;
    }

    private Instance parseInstance(String line) {
        line = line.trim();
        if(line.length() == 0) {
            return null;
        } else {
            Instance inst = new Instance();
            String[] vals = line.split(",");
            if(vals.length != this.getAttributes().size()) {
                return null;
            } else {
                for(int i = 0; i < this.getAttributes().size(); ++i) {
                    Object val = null;
                    Attribute att = (Attribute)this.getAttributes().get(i);
                    if(att instanceof NominalAttribute) {
                        val = new NominalValue(vals[i]);
                    } else if(att instanceof NumericAttribute) {
                        try {
                            val = new NumericValue(Double.parseDouble(vals[i]));
                        } catch (NumberFormatException var8) {
                            val = null;
                        }
                    }

                    inst.addValue(att, (Value)val);
                }

                return inst;
            }
        }
    }

    public String toString() {
        String result = "@relation " + this.getName() + "\n";

        Attribute att;
        for(Iterator i = this.getAttributes().iterator(); i.hasNext(); result = result + att.toString() + "\n") {
            att = (Attribute)i.next();
        }

        for(int var4 = 0; var4 < this.getNumberOfInstances(); ++var4) {
            result = result + this.getInstance(var4).toString(this.getAttributes()) + "\n";
        }

        return result;
    }

    public static void main(String[] args) {
        File f = new File("weather.nominal.arff");

        try {
            Dataset ex = new Dataset();
            ex.load(f);
            System.out.println(ex.toString());
        } catch (Exception var3) {
            var3.printStackTrace();
        }

    }

    public int getClassIndex() {
        return this.classIndex;
    }

    public void setClassIndex(int classIndex) {
        this.classIndex = classIndex;
    }
}
