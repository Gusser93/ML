package classifier;
import java.io.Serializable;
import java.util.*;

import com.sun.tools.doclint.Entity;
import com.sun.tools.doclint.HtmlTag;
import dataset.*;

public class DomainNaiveBayes implements Classifier, Cloneable, Serializable {

	private Set<String> vocabulary = new HashSet<String>();
	private Map<String, Double> p_v = new HashMap<String, Double>();
	//private List<Double> p_w_v = new ArrayList<Double>();
	private Map<Map.Entry, Double> p_w_v = new HashMap<Map.Entry, Double>();
	private Collection<String> v_js = new ArrayList<String>();


	//------------------------------------------------------------
	//----------------------- Inner Tuple class ------------------
	//------------------------------------------------------------

	/**
	 * Inner class
	 * @param <K>
	 * @param <V>
     */
	private class Tuple<K,V> implements Map.Entry<K,V> {
		final K key;
		V value;
		Tuple<K,V> next;

		Tuple(K key, V value) {
			this.key = key;
			this.value = value;
		}

		public final K getKey()        { return key; }
		public final V getValue()      { return value; }
		public final String toString() { return key + "=" + value; }

		public final int hashCode() {
			return Objects.hashCode(key) ^ Objects.hashCode(value);
		}

		public final V setValue(V newValue) {
			V oldValue = value;
			value = newValue;
			return oldValue;
		}

		public final boolean equals(Object o) {
			if (o == this)
				return true;
			if (o instanceof Map.Entry) {
				Map.Entry<?,?> e = (Map.Entry<?,?>)o;
				if (Objects.equals(key, e.getKey()) &&
						Objects.equals(value, e.getValue()))
					return true;
			}
			return false;
		}
	}



	//------------------------------------------------------------
	//----------------------- general helper ---------------------
	//------------------------------------------------------------


	// count how often an element occurs inside the array
	private static int count(Object[] arr, Object objc) {
		int count = 0;
		for (Object o : arr) {
			if (arr.equals(objc)) {
				count++;
			}
		}
		return count;
	}

	// get all words inside string
	private static String[] getWords(String s) {
		String[] words = s.split("\\s+");
		for (int i = 0; i<words.length; i++) {
			words[i] = words[i].replaceAll("[^\\w]", "");
		}
		return words;
	}


	//------------------------------------------------------------
	//----------------------Classifier helper --------------------
	//------------------------------------------------------------

	/**
	 *
	 * @param data
	 * @return
     */
	private static Attribute notClassAttr(Instances data) {
		Attribute notClassAttr = null;
		try {
			Attribute classAttr = classAttribute(data);
			for (Attribute attr : data.getAttributes()) {
				if (!attr.equals(classAttr)) {
					notClassAttr = attr;
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// check if a attribute was found
		if (notClassAttr != null &&
				notClassAttr.attributeType().equals(AttributeType.string)) {
			return notClassAttr;
		}

		return null;
	}

	/**
	 *
	 * @param data
	 * @return class attribute of the given instances
     */
	private static Attribute classAttribute(Instances data) {
		try {
			return data.getInstances().get(0).classAttribute();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static int getIndexForAttribute(Instances data, Attribute attr) {
		return data.getInstances().indexOf(attr);
	}

	//------------------------------------------------------------
	//------------------------- Classifier -----------------------
	//------------------------------------------------------------

	public void buildClassifier(Instances data) {
		// get first non class attribute
		Attribute notClassAttr = notClassAttr(data);
		Attribute classAttr = classAttribute(data);

		// get all distinct words
		int attrIdx = getIndexForAttribute(data, notClassAttr);
		for (Instance i : data.getInstances()) {
			// split value to get all distinct words
			Collections.addAll(vocabulary, getWords(i.stringValue(attrIdx)));
		}

		// for each target value v_j
		// Should be A, B, C, D in our case
		try {
			this.v_js = classAttr.getValues();
			if (this.v_js == null) {
				System.err.println("No values for class attribute found");
				return;
			}
			// iterate over values
			for (String v_j : this.v_js) {
				// get subset of instances where traget value equals v_j
				StringBuilder text_j = new StringBuilder();
				List<Instance> docs_j = new ArrayList<Instance>();
				for (Instance i : data.getInstances()) {
						if (v_j.equals(i.classValueString())) {
							docs_j.add(i);

							text_j.append(i.stringValue(attrIdx));
							text_j.append(" ");
						}
				}

				// calculate values
				double exampleSize = (double)data.getInstances().size();
				double docs_jSize = (double)docs_j.size();
				p_v.put(v_j, docs_jSize/exampleSize);

				// count words inside subset
				String txt = text_j.toString();
				String[] words = getWords(txt);
				int n = words.length;

				// iterate over vocabulary
				for (String w_k : vocabulary) {
					int n_k = count(words, w_k);
					double numerator = (double)n_k+1.0d;
					double denominator = (double)(n+vocabulary.size());
					p_w_v.put(new Tuple<String, String>(w_k, v_j), numerator/denominator);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String classifyInstance(Instance instance) {
		Instances data = instance.dataset();
		Attribute attr =  notClassAttr(data);
		int attrIdx = getIndexForAttribute(data, attr);

		// get doc
		String doc_str = instance.stringValue(attrIdx);
		String[] doc = getWords(doc_str);

		// indices
		List<String> words = new ArrayList<String>();

		// count words which are also in vocabulary
		for (String word : doc) {
			if (vocabulary.contains(word)) {
				words.add(word);
			}
		}

		// get max value
		double argmax = Double.NEGATIVE_INFINITY;
		String vNB = null;

		for (String v_j : this.v_js) {
			double result = p_v.get(v_j);
			for (String a : words) {
				result *= p_w_v.get(new Tuple<>(words, v_j));
			}

			if (result > argmax) {
				argmax = result;
				vNB = v_j;
			}
		}

		return vNB;
	}
	
	public double[] distributionForInstace(Instance instance){
		return null;
	}
	
	public Capabilities getCapabilities(){
		return null;
	}
}
