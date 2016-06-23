/*
Test results:

-----------------------------------------
| SCORE     | STEMMING         | TIME   |
=========================================
|   500     |    true          |  0.87  |
|   500     |    false         |  0.87  |
-----------------------------------------
|  1000     |    true          |  0.89  |
|  1000     |    false         |  0.902 |
-----------------------------------------
|  2000     |    true          |  0.88  |
|  2000     |    false         |  0.911 |
-----------------------------------------
|  2500     |    true          |  0.88  |
|  2500     |    false         | 0.914  |
-----------------------------------------
|  3000     |    true          |  0.87  |
|  3000     |    false         |  0.912 |
-----------------------------------------
|  5000     |    true          |  0.84  |
|  5000     |    false         |  0.89  |
-----------------------------------------
*/



package classifier;
import wordprocessing.WordParser;
import dataset.Attribute;
import dataset.AttributeType;
import dataset.Instance;
import dataset.Instances;
import evaluation.Evaluation;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.DoubleStream;

public class DomainNaiveBayes implements Classifier, Cloneable, Serializable {
	// attributes
	private Set<String> vocabulary = new HashSet<String>();
	private Map<String, Double> p_v = new ConcurrentHashMap<String, Double>();
	private Map<Map.Entry<String, String>, Double> p_w_v =
			new ConcurrentHashMap<Map.Entry<String, String>, Double>();
	private Map<Map.Entry<String, String>, Double> d_i_j =
			new ConcurrentHashMap<Map.Entry<String, String>, Double>();
	private Collection<String> v_js = new ArrayList<String>();
	private HashMap<String, Double> distribution = new HashMap<String, Double>();

	// Configuration
	private final int SCORE = 2500;

	//------------------------------------------------------------
	//----------------------- Inner Tuple class ------------------
	//------------------------------------------------------------

	/**
	 * Inner class
	 * @param <K>
	 * @param <V>
     */
	private static class Tuple<K extends Comparable,V> implements Map.Entry<K,
			V>,
			Comparable<Tuple <? extends K, ?>> {
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


		@Override
		public int compareTo(Tuple<? extends K, ?> o) {
			return this.key.compareTo(o.key);
		}
	}



	//------------------------------------------------------------
	//--------------------------- Score --------------------------
	//------------------------------------------------------------


	/*
	private double score(String[] words, String str) {
		double score = 0.0;
		//double test = 0.0;
		double p_w = 0.0;

		for (Map.Entry<String, Double> v : this.p_v.entrySet()) {
			double p_w_v = this.p_w_v.get(new Tuple(str, v.getKey()));
			double p_v = v.getValue();
			p_w += p_w_v * p_v;
		}

		// Wahrscheinlichkeit für v - Wahrscheinlichkeit für v unter w
		for (Map.Entry<String, Double> v : this.p_v.entrySet()) {
			double p_w_v = this.p_w_v.get(new Tuple(str, v.getKey()));
			double p_v = v.getValue();
			double p_v_w = (p_w_v * p_v) / p_w;
			double dif = p_v - p_v_w;
			//test += p_v_w;
			score += dif * dif;
		}
		//System.out.println(test);
		//ohne count 93%
		return -Math.sqrt(score);// * count(words, str);
	} */


	/**
	 *
	 * @param words
	 * @param str
     * @return
     */
	private double score(String[] words, String str) {
		double score = 0.0;
		double p_w = 0.0;

		for (Map.Entry<String, Double> v : this.p_v.entrySet()) {
			double p_w_v = this.p_w_v.get(new Tuple(str, v.getKey()));
			double p_v = v.getValue();
			p_w += p_w_v * p_v;
		}

		// Wahrscheinlichkeit für v - Wahrscheinlichkeit für v unter w
		for (Map.Entry<String, Double> v : this.p_v.entrySet()) {
			double p_w_v = this.p_w_v.get(new Tuple(str, v.getKey()));
			double p_v = v.getValue();
			double p_v_w = (p_w_v * p_v) / p_w;
			double p_v_wc = p_v - p_v_w * p_w;
			double p_wc = 1 - p_w;

			score += p_v_w * Math.log(p_v_w / (p_v * p_w));
			score += p_v_wc * Math.log(p_v_wc / (p_v * p_wc));
		}
		return score;
	}

	/* private double score(String[] words, String str) {
		return -count(words, str);
	} */



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

	/**
	 *
	 * @param data
	 * @param attr
     * @return
     */
	private static int getIndexForAttribute(Instances data, Attribute attr) {
		return data.getAttributes().indexOf(attr);
	}

	private static Tuple<String, List> getDoc(Instances data, String v_j, int
			attrIdx) {
		// get subset of instances where traget value equals v_j
		StringBuilder text_j = new StringBuilder();
		List<Instance> docs_j = new ArrayList<Instance>();
		for (Instance i : data.getInstances()) {
			try {
				if (v_j.equals(i.classValueString())) {
					docs_j.add(i);

					text_j.append(i.stringValue(attrIdx));
					text_j.append(" ");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return new Tuple<String, List>(text_j.toString(), docs_j);
	}

	//------------------------------------------------------------
	//------------------------- Classifier -----------------------
	//------------------------------------------------------------

	/**
	 *
	 * @param data
     */
	public void buildClassifier(Instances data) {
		// get first non class attribute
		Attribute notClassAttr = notClassAttr(data);
		Attribute classAttr = classAttribute(data);


		// get all distinct words
		int attrIdx = getIndexForAttribute(data, notClassAttr);
		for (Instance i : data.getInstances()) {
			// split value to get all distinct words
			String[] filteredWords = WordParser.getFilteredWords(i.stringValue(attrIdx));
			Collections.addAll(vocabulary, filteredWords);
		}

		// for each target value v_j
		// Should be A, B, C, D in our case
		try {
			this.v_js = classAttr.getValues();
			if (this.v_js == null) {
				System.err.println("No values for class attribute found");
				return;
			}

			// save all threads
			ArrayList<Thread> threads = new ArrayList<>();
			final Collection<String> __v_js = this.v_js;


			// iterate over values
			for (String v_j : __v_js) {
				// perform each calculation on its own thread
				Thread t = new Thread() {
					public void run() {

						Tuple<String, List> tuple = getDoc(data, v_j, attrIdx);
						List<String> docs_j= tuple.getValue();
						// calculate values
						double exampleSize = (double)data.getInstances().size();
						double docs_jSize = (double)docs_j.size();
						p_v.put(v_j, docs_jSize / exampleSize);

						// count words inside subset
						String txt = tuple.getKey();
						String[] words = WordParser.getWords(txt);

						int n = words.length;

						ArrayList<Thread> inner_threads = new ArrayList<>();

						// iterate over vocabulary
						for (String w_k : vocabulary) {
							// calculate probabilities inside thread
							Thread inner_t = new Thread() {
								public void run() {
									double n_k = WordParser.countWords(words, w_k);
									double numerator = (double)n_k + 1.0d;
									double denominator = (double)(n + vocabulary.size());

									p_w_v.put(new Tuple<String, String>(w_k, v_j),
											numerator / denominator);
								}
							};
							inner_threads.add(inner_t);
							inner_t.start();
						}


						// join threads
						try {
							for (Thread inner_thread : inner_threads) {
								inner_thread.join();
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				};
				t.start();
				threads.add(t);
			}

			// Join our threads
			for (Thread thread : threads) {
				thread.join();
			}

			List<Tuple<Double, String>> score = new LinkedList<>();

			for (String word : vocabulary) {
				score.add(new Tuple<Double, String>(score(vocabulary.toArray(new
						String[0]), word), word));
			}

			Collections.sort(score);
			//Collections.reverse(score);
			int number = Math.min(SCORE, score.size());
			score = score.subList(0, number);

			this.vocabulary = new HashSet<>();
			for (Tuple<? extends Number, String> t : score) {
				vocabulary.add(t.getValue());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 *
	 * @param instance
	 * @return
     */
	public String classifyInstance(Instance instance) {
		Instances data = instance.dataset();
		Attribute attr =  notClassAttr(data);
		int attrIdx = getIndexForAttribute(data, attr);

		// get doc
		String doc_str = instance.stringValue(attrIdx);
		String[] doc = WordParser.getWords(doc_str);

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
				result *= p_w_v.get(new Tuple<>(a, v_j));
			}

			// CNB
			result = - result;

			this.distribution.put(v_j, result);

			if (result > argmax) {
				argmax = result;
				vNB = v_j;
			}
		}

		return vNB;
	}

	/**
	 *
	 * @param instance
	 * @return
     */
	public HashMap<String, Double> distributionForInstace(Instance instance){
		this.classifyInstance(instance);
		return this.distribution;
	}

	public Instances classify(Instances data) throws Exception {
		Instances result = new Instances(data);
		for (Instance instance : data.getInstances()) {
			String label = this.classifyInstance(instance);
			Instance labeled = new Instance(instance, label);
			result.addInstance(labeled);
		}
		return result;
	}

	public Capabilities getCapabilities(){
		return null;
	}


	public static void main(String[] args) throws Exception {
		Instances data = new Instances("trg.txt", "\t");
		data.setClassIndex(0);
		Evaluation eval = new Evaluation(data);
		eval.holdoutEvaluationDEBUG(0.33, DomainNaiveBayes.class);
		System.out.println("Accuracy = " + eval.accuracy());
	}
}
