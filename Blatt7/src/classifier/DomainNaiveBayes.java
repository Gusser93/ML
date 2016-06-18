package classifier;
import dataset.Attribute;
import dataset.AttributeType;
import dataset.Instance;
import dataset.Instances;
import evaluation.Evaluation;

import java.io.Serializable;
import java.util.*;

public class DomainNaiveBayes implements Classifier, Cloneable, Serializable {

	private Set<String> vocabulary = new HashSet<String>();
	private Map<String, Double> p_v = new HashMap<String, Double>();
	private Map<Map.Entry, Double> p_w_v = new HashMap<Map.Entry, Double>();
	private Collection<String> v_js = new ArrayList<String>();
	private HashMap<String, Double> distribution = new HashMap<String, Double>();
	private final boolean DEBUG_FLAG = true;

	private static final String[] FILTER_LIST = {"this", "is", "in", "the",
			"of", "with", "been", "they", "an", "as","be", "are", "may",
			"such", "to", "that", "cannot", "only", "has", "which", "no",
			"some", "for", "not", "and", "match", "among", "from", "we",
			"was", "by", "these", "have", "were", "two", "on", "other", "at",
			"also", "or", "but", "one", "it", "its", "all", "three", "most",
			"here", "both", "than", "previously", "open", "known", "their",
			"using", "new", "showed", "more", "those", "found", "into",
			"many", "high", "different", "several", "within", "single",
			"there", "used", "important", "each", "well", "number", "large",
			"first", "four", "suggest", "however", "can", "our", "suggests",
			"shown", "indicated", "very", "pathogen", "extensive", "evolution", 
			"had", "contrast", "reveals", "responsible", "indicates", "least", 
			"contained", "differences", "studies", "required", "total", 
			"primary", "about", "when", "study", "thus", "product", "products", 
			"shows", "same", "contain", "through", "detected", "any", "small", 
			"closely", "five", "could", "during", "complex", "although", 
			"compared", "unique", "common", "identity", "show", "approximately",
			"suggesting", "containing", "data", "results", "highly", "report", 
			"present", "related", "similar", "reading", "between", "contains",
			"connect", "illustrated", "orderly", "introducing", "directing",
			"cut", "house", "manners", "sixteen", "built", "going", "publicly",
			"text", "widest", "informations", "minimally", "childhood", "look",
			"disrupt", "lose", "theory", "logical", "loose", "turns", "forth",
			"grew", "receiving", "older", "happened", "game", "electric",
			"unreported", "scoring", "keeping", "predictive", "familiar",
			"needs", "triggered", "easily", "manipulation", "unbroken",
			"suggeste", "similarily", "seek", "joins", "formyl", "ruled",
			"thin", "know", "deeply", "get", "power", "self", "depended",
			"recognizes", "success", "sampled", "focus", "delivered", "instant",
			"beneath", "quantify", "accidentally", "consecutive", "summarized",
			"combining", "so", "control", "caused", "higher", "divergence",
			"repeats", "ability", "apparent", "multiple", "further",
			"development", "composed", "relative", "production", "components",
			"together", "transcriptional", "regulation", "demonstrated",
			"against", "designated", "additional", "six", "obtained", "degree",
			"distribution", "causes", "insertion", "factor", "consistent",
			"similarities", "translation", "variety", "entire", "whereas",
			"include", "after", "unknown", "second", "while", "length", "long",
			"assigned", "group", "sites", "either", "specific", "set",
			"northern", "subunits", "diverse", "model", "content", "part",
			"described", "ii", "growth", "members", "various", "likely",
			"identify", "low", "essential", "analysis", "identified",
			"complete", "determined", "predicted", "revealed", "including",
			"similarity", "potential", "major", "characterized", "identical",
			"located", "functional", "role", "corresponding", "reported",
			"order", "organization", "analyses", "indicate", "addition",
			"evidence", "factors", "consists", "member", "mass", "cause",
			"appears", "close", "over", "possible", "position", "largest",
			"recently", "appear", "range", "available", "mechanism",
			"probably", "information", "suggested", "represents", "whole",
			"active", "end", "early", "origin", "identification",
			"consensus", "therefore", "iii", "iv", "vi", "vii", "viii",
			"far", "third", "database", "horizontal", "experiments",
			"overall", "deletion", "typical", "strong", "majority", "whose",
			"eight", "use", "increased", "understanding", "provide",
			"provides", "methods", "databases", "finding", "forms", "completely"

	};
	private static final Set<String> FILTER = new HashSet<>(Arrays.asList(FILTER_LIST));
	private static final String FILTER_REGEX = "[A-Za-z][^\\s!.,;:/=@]+";
	//"[A-Za-z][A-Za-z]+"; //bislang bestes

	//------------------------------------------------------------
	//----------------------- Inner Tuple class ------------------
	//------------------------------------------------------------

	/**
	 * Inner class
	 * @param <K>
	 * @param <V>
     */
	private class Tuple<K extends Comparable,V> implements Map.Entry<K,
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

		/**
		 * Compares this object with the specified object for order.  Returns a
		 * negative integer, zero, or a positive integer as this object is less
		 * than, equal to, or greater than the specified object.
		 * <p>
		 * <p>The implementor must ensure <tt>sgn(x.compareTo(y)) ==
		 * -sgn(y.compareTo(x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
		 * implies that <tt>x.compareTo(y)</tt> must throw an exception iff
		 * <tt>y.compareTo(x)</tt> throws an exception.)
		 * <p>
		 * <p>The implementor must also ensure that the relation is transitive:
		 * <tt>(x.compareTo(y)&gt;0 &amp;&amp; y.compareTo(z)&gt;0)</tt> implies
		 * <tt>x.compareTo(z)&gt;0</tt>.
		 * <p>
		 * <p>Finally, the implementor must ensure that <tt>x.compareTo(y)==0</tt>
		 * implies that <tt>sgn(x.compareTo(z)) == sgn(y.compareTo(z))</tt>, for all
		 * <tt>z</tt>.
		 * <p>
		 * <p>It is strongly recommended, but <i>not</i> strictly required that
		 * <tt>(x.compareTo(y)==0) == (x.equals(y))</tt>.  Generally speaking, any
		 * class that implements the <tt>Comparable</tt> interface and violates this
		 * condition should clearly indicate this fact.  The recommended language is
		 * "Note: this class has a natural ordering that is inconsistent with
		 * equals."
		 * <p>
		 * <p>In the foregoing description, the notation <tt>sgn(</tt><i>expression</i><tt>)</tt>
		 * designates the mathematical <i>signum</i> function, which is defined to
		 * return one of <tt>-1</tt>, <tt>0</tt>, or <tt>1</tt> according to whether
		 * the value of <i>expression</i> is negative, zero or positive.
		 *
		 * @param o
		 *         the object to be compared.
		 *
		 * @return a negative integer, zero, or a positive integer as this object is
		 * less than, equal to, or greater than the specified object.
		 *
		 * @throws NullPointerException
		 *         if the specified object is null
		 * @throws ClassCastException
		 *         if the specified object's type prevents it from being compared to
		 *         this object.
		 */
		@Override
		public int compareTo(Tuple<? extends K, ?> o) {
			return this.key.compareTo(o.key);
		}
	}



	//------------------------------------------------------------
	//----------------------- general helper ---------------------
	//------------------------------------------------------------


	// count how often an element occurs inside the array
	private static int count(Object[] arr, Object objc) {
		int count = 0;
		for (Object o : arr) {
			//o nicht arr, oder?
			if (o.equals(objc)) {
				count++;
			}
		}
		return count;
	}

	// get all words inside string
	private static String[] getWords(String s) {
		String[] words = s.split("\\s+");
		Set<String> wordList = new HashSet<>();
		for (int i = 0; i < words.length; i++) {
			words[i] = words[i].replaceAll("[^\\w]", "");
		}
		return words;
	}


	private static String[] getFilteredWords(String s) {
		String[] words = s.split("\\s+");
		Set<String> wordList = new HashSet<>();
		for (int i = 0; i<words.length; i++) {
			if (words[i].matches(FILTER_REGEX)) {
				wordList.add(words[i].replaceAll("[^\\w]", ""));
			}
		}
		wordList.removeAll(FILTER);
		return wordList.toArray(new String[0]);

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

	/**
	 *
	 * @param data
	 * @param attr
     * @return
     */
	private static int getIndexForAttribute(Instances data, Attribute attr) {
		return data.getAttributes().indexOf(attr);
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

		// TODO delte
		List<String> DEBUG = new ArrayList<>();

		// get all distinct words
		int attrIdx = getIndexForAttribute(data, notClassAttr);
		for (Instance i : data.getInstances()) {
			// split value to get all distinct words
			Collections.addAll(vocabulary, getFilteredWords(i.stringValue(attrIdx)));
			Collections.addAll(DEBUG, getFilteredWords(i.stringValue(attrIdx)));
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


			if (DEBUG_FLAG) {
				List<Tuple<Integer, String>> score = new LinkedList<>();

				for (String word : vocabulary) {
					score.add(new Tuple<Integer, String>
							(count(DEBUG.toArray(new String[0]), word), word));
				}

				Collections.sort(score);

				for (Tuple entry : score) {
					System.out.println(entry.key + " " + entry.value);
				}

				//System.out.println(this.p_w_v);
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
				result *= p_w_v.get(new Tuple<>(a, v_j));
			}

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
		eval.holdoutEvaluation(1, 0.66, new Random(1), DomainNaiveBayes.class);
		System.out.println("Accuracy = " + eval.accuracy());
		/*Instances test = new Instances(data, "tst.txt", "\t");
		test.print();
		DomainNaiveBayes c = new DomainNaiveBayes();
		c.buildClassifier(data);
		Instances labeled = c.classify(test);
		labeled.print();*/
	}
}

//0.628
//0.691
//0.708
//0.741
//0.744
//0.771