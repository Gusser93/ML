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
|  2500     |  false           | 0.914  |
-----------------------------------------
|  3000     |    true          |  0.87  |
|  3000     |    false         |  0.912 |
-----------------------------------------
|  5000     |    true          |  0.84  |
|  5000     |    false         |  0.89  |
-----------------------------------------
*/



package classifier;
import dataparsing.Stemmer;
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

	private Set<String> vocabulary = new HashSet<String>();
	private Map<String, Double> p_v = new ConcurrentHashMap<String, Double>();
	private Map<Map.Entry<String, String>, Double> p_w_v =
			new ConcurrentHashMap<Map.Entry<String, String>, Double>();
	private Map<Map.Entry<String, String>, Double> d_i_j =
			new ConcurrentHashMap<Map.Entry<String, String>, Double>();
	private Collection<String> v_js = new ArrayList<String>();
	private HashMap<String, Double> distribution = new HashMap<String, Double>();
	private static final boolean USE_STEMMING = false;
	private final int SCORE = 2500;

	// TODO zeige klassen verteilung für wörter statt anzahl
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
			"provides", "methods", "databases", "finding", "forms",
			"completely", "deduced", "under", "did", "pair", "them",
			"another", "duplication", "start", "furthermore", "yet",
			"nearly", "normal", "weight", "like", "half", "being", "shared",
			"does", "highest", "confirmed", "source", "larger","increase",
			"unusual", "class", "significantly", "will", "possibly", "rate",
			"central", "limited", "public", "now", "search", "point",
			"around", "because", "great", "greatly", "background",
			"substantial", "partially", "program", "evident", "usage",
			"independently", "occurrence", "fully", "lower", "negative",
			"island", "mobile", "local", "belonging", "via", "laboratory",
			"smaller", "amount", "compare", "origins", "belong", "location",
			"percent", "outer", "finally", "performed", "unusually",
			"feature", "amounts", "functionally", "presented", "published",
			"unlike", "relatively", "per", "wide", "southern", "missing",
			"implicated", "resulted", "describe", "result", "response",
			"direct", "strongly", "size", "frequent", "currently",
			"research", "requires", "continues", "points", "plus", "acts",
			"tests", "unselected", "orders", "distant", "lesser", "nameley",
			"test", "noted", "conditions", "resulting", "produced",
			"incldes", "absent", "positions", "represent"};

	private static final String[] MOST_COMMON_ENGLISH_WORDS = {
			"the", "and", "to", "of", "a", "I", "in", "was", "he", "that", "it",
			"his", "her", "you", "as", "had", "with", "for", "she", "not", "at",
			"but", "be", "my", "on", "have", "him", "is", "said", "me", "which",
			"by", "so", "this", "all", "from", "they", "no", "were", "if",
			"would", "or", "when", "what", "there", "been", "one", "could",
			"very", "an", "who", "them", "Mr", "we", "now", "more", "out",
			"do", "are", "up", "their", "your", "will", "little", "than",
			"then", "some", "into", "any", "well", "much", "about", "time",
			"know", "should", "man", "did", "like", "upon", "such", "never",
			"only", "good", "how", "before", "other", "see", "must", "am",
			"own", "come", "down", "say", "after", "think", "made", "might",
			"being", "Mrs", "again", "great", "two", "can", "go", "over", "too",
			"here", "came", "old", "thought", "himself", "where", "our", "may",
			"first", "way", "has", "though", "without", "went", "us", "away",
			"day", "make", "these", "young", "nothing", "long", "shall", "sir",
			"back", "don't", "house", "ever", "yet", "take", "every", "hand",
			"most", "last", "eyes", "its", "miss", "having", "off", "looked",
			"even", "while", "dear", "look", "many", "life", "still", "mind",
			"quite", "another", "those", "just", "head", "tell", "better",
			"always", "saw", "seemed", "put", "face", "let", "took", "poor",
			"place", "why", "done", "herself", "found", "through", "same",
			"going", "under", "enough", "soon", "home", "give", "indeed",
			"left", "get", "once", "mother", "heard", "myself", "rather",
			"love", "knew", "got", "lady", "room", "something", "yes", "thing",
			"father", "perhaps", "sure", "heart", "oh", "right", "against",
			"three", "men", "night", "people", "door", "told", "round",
			"because", "woman", "till", "felt", "between", "both", "side",
			"seen", "morning", "began", "whom", "however", "asked", "things",
			"part", "almost", "moment", "looking", "want", "far", "hands",
			"gone", "world", "few", "towards", "gave", "friend", "name", "best",
			"word", "turned", "kind", "cried", "since", "anything", "next",
			"find", "half", "hope", "called", "nor", "words", "hear", "brought",
			"set", "each", "replied", "wish", "voice", "whole", "together",
			"manner", "new", "believe", "course", "least", "years", "answered",
			"among", "stood", "sat", "speak", "leave", "work", "keep", "taken",
			"end", "less", "present", "family", "often", "wife", "whether",
			"master", "coming", "mean", "returned", "evening", "light", "money",
			"cannot", "whose", "boy", "days", "near", "matter", "suppose",
			"gentleman", "used", "says", "really", "rest", "business", "full",
			"help", "child", "sort", "passed", "lay", "small", "behind", "girl",
			"feel", "fire", "care", "alone", "open", "person", "call", "given",
			"I'll", "sometimes", "making", "short", "else", "large", "within",
			"chapter", "true", "country", "times", "ask", "answer", "air",
			"kept", "hour", "letter", "happy", "reason", "pretty", "husband",
			"certain", "others", "ought", "does", "known", "it's", "bed",
			"table", "that's", "ready", "read", "already", "pleasure", "either",
			"means", "spoke", "taking", "friends", "talk", "hard", "walked",
			"turn", "strong", "thus", "yourself", "high", "along", "above",
			"feeling", "glad", "children", "doubt", "nature", "themselves",
			"black", "hardly", "town", "sense", "saying", "deal", "account",
			"use", "white", "bad", "everything", "can't", "neither", "wanted",
			"mine", "close", "return", "dark", "fell", "subject", "bear",
			"appeared", "fear", "state", "thinking", "also", "point",
			"therefore", "fine", "case", "doing", "held", "certainly", "walk",
			"lost", "question", "company", "continued", "fellow", "truth",
			"water", "possible", "hold", "afraid", "bring", "honour", "low",
			"ground", "added", "five", "remember", "except", "power", "seeing",
			"dead", "I'm", "usual", "able", "second", "arms", "late", "opinion",
			"window", "brother", "live", "four", "none", "death", "arm", "road",
			"hair", "sister", "entered", "sent", "married", "longer",
			"immediately", "god", "women", "hours", "ten", "understand", "son",
			"horse", "wonder", "cold", "beyond", "please", "fair", "became",
			"sight", "met", "afterwards", "eye", "year", "show", "general",
			"itself", "silence", "lord", "wrong", "turning", "daughter", "stay",
			"forward", "O", "interest", "thoughts", "followed", "won't",
			"different", "opened", "several", "idea", "received", "change",
			"laid", "strange", "nobody", "fact", "during", "feet", "tears",
			"run", "purpose", "character", "body", "ran", "past", "order",
			"need", "pleased", "trouble", "whatever", "dinner", "happened",
			"sitting", "getting", "there's", "besides", "soul", "ill", "early",
			"rose", "aunt", "hundred", "minutes", "across", "carried", "sit",
			"observed", "suddenly", "creature", "conversation", "worse", "six",
			"quiet", "chair", "doctor", "tone", "standing", "living", "sorry",
			"stand", "meet", "instead", "wished", "ah", "lived", "try", "red",
			"smile", "sound", "expected", "silent", "common", "meant", "tried",
			"until", "mouth", "distance", "occasion", "cut", "marry", "likely",
			"length", "story", "visit", "deep", "seems", "street", "remained",
			"become", "led", "speaking", "natural", "giving", "further",
			"struck", "week", "loved", "drew", "seem", "church", "knows",
			"object", "ladies", "marriage", "book", "appearance", "pay", "I've",
			"obliged", "particular", "pass", "thank", "form", "knowing", "lips",
			"knowledge", "former", "blood", "sake", "fortune", "necessary",
			"presence", "feelings", "corner", "beautiful", "talking", "spirit",
			"ago", "foot", "circumstances", "wind", "presently", "comes",
			"attention", "wait", "play", "easy", "real", "clear", "worth",
			"cause", "send", "spirits", "chance", "didn't", "view", "pleasant",
			"party", "beginning", "horses", "stopped", "notice", "duty", "he's",
			"age", "figure", "leaving", "sleep", "entirely", "twenty", "fall",
			"promise", "months", "broken", "heavy", "secret", "thousand",
			"happiness", "comfort", "minute", "act", "human", "fancy",
			"strength", "showed", "pounds", "nearly", "probably", "captain",
			"piece", "school", "write", "laughed", "reached", "repeated",
			"walking", "father's", "heaven", "beauty", "shook", "sun",
			"waiting", "moved", "bit", "desire", "news", "front", "effect",
			"laugh", "uncle", "fit", "miles", "handsome", "caught", "hat",
			"regard", "gentlemen", "supposed", "easily", "impossible", "glass",
			"resolved", "grew", "consider", "green", "considered", "unless",
			"stop", "forth", "expect", "perfectly", "altogether", "surprise",
			"sudden", "free", "exactly", "grave", "carriage", "believed",
			"service", "angry", "putting", "carry", "everybody", "mentioned",
			"looks", "scarcely", "society", "affection", "exclaimed", "dress",
			"die", "earth", "latter", "garden", "step", "perfect",
			"countenance", "liked", "dare", "pain", "companion", "journey",
			"paper", "opportunity", "makes", "honest", "arrived", "you'll",
			"bright", "pity", "directly", "cry", "trust", "fast", "ye", "warm",
			"danger", "trees", "breakfast", "rich", "engaged", "proper",
			"talked", "respect", "fixed", "hill", "wall", "determined", "wild",
			"shut", "top", "plain", "scene", "sweet", "especially", "public",
			"acquaintance", "forget", "history", "pale", "pray", "books",
			"afternoon", "man's", "otherwise", "mention", "position", "speech",
			"gate", "'em", "boys", "yours", "drink", "slowly", "broke",
			"clothes", "fond", "pride", "watch", "sooner", "settled", "paid",
			"reply", "tea", "lie", "running", "died", "gentle", "particularly",
			"allowed", "outside", "placed", "joy", "hearing", "note",
			"condition", "follow", "begin", "neck", "serious", "hurt",
			"kindness", "mere", "farther", "changed", "o'clock", "passing",
			"girls", "force", "situation", "greater", "expression", "eat",
			"reading", "spoken", "raised", "anybody", "started", "following",
			"although", "sea", "proud", "future", "quick", "safe", "temper",
			"laughing", "ears", "difficulty", "meaning", "servant", "sad",
			"advantage", "appear", "offer", "breath", "opposite", "number",
			"miserable", "law", "rising", "favour", "save", "twice", "single",
			"blue", "noise", "stone", "mistress", "surprised", "allow", "spot",
			"burst", "keeping", "line", "understood", "court", "finding",
			"direction", "anxious", "pocket", "around", "conduct", "loss",
			"fresh", "below", "hall", "satisfaction", "land", "telling",
			"passion", "floor", "break", "lying", "waited", "closed", "meeting",
			"trying", "seat", "king", "confidence", "offered", "stranger",
			"somebody", "matters", "noble", "pardon", "private", "sharp",
			"evil", "weeks", "justice", "hot", "cast", "letters", "youth",
			"lives", "health", "finished", "hoped", "holding", "touch", "spite",
			"delight", "bound", "consequence", "rain", "wouldn't", "third",
			"hung", "ways", "weather", "written", "difference", "kitchen",
			"she's", "mother's", "persons", "quarter", "promised", "hopes",
			"brown", "nay", "seven", "simple", "wood", "beside", "middle",
			"ashamed", "lose", "dreadful", "move", "generally", "cousin",
			"surely", "satisfied", "bent", "shoulder", "art", "field",
			"quickly", "thrown", "tired", "share", "pair", "to-morrow",
			"aware", "colour", "writing", "whenever", "quietly", "fool",
			"forced", "touched", "smiling", "taste", "dog", "spent", "steps",
			"worst", "legs", "watched", "ay", "thee", "eight", "worthy",
			"wrote", "manners", "proceeded", "frightened", "somewhat", "born",
			"greatest", "charge", "degree", "shame", "places", "ma'am",
			"couldn't", "tongue", "according", "box", "wine", "filled",
			"servants", "calling", "fallen", "supper"};
	private static final Set<String> FILTER1 = new HashSet<>(
			Arrays.asList(FILTER_LIST));
	private static final Set<String> FILTER2 = new HashSet<>(
			Arrays.asList(MOST_COMMON_ENGLISH_WORDS));
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
	//----------------------- Word processing --------------------
	//------------------------------------------------------------

	/**
	 * count how often an element occurs inside the array
	 * @param arr
	 * @param objc
     * @return
     */
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


	/**
	 * get all words inside string
	 * @param s
	 * @return
     */
	private static String[] getWords(String s) {
		String[] words = s.split("\\s+");
		for (int i = 0; i < words.length; i++) {
			words[i] = words[i].replaceAll("[^\\w]", "");
			// only letters inside the word
			if (USE_STEMMING && words[i].matches("[a-zA-Z]+")) {
				// transform our word to the root form
				String temp = Stemmer.readString(words[i]).get(0);

				if (temp.length() > 5) {
					words[i] = temp;
				}
			}
		}
		return words;
	}

	/**
	 *
	 * @param s
	 * @return
     */
	private static String[] getFilteredWords(String s) {
		String[] words = s.split("\\s+");
		List<String> wordList = new ArrayList<>();
		for (int i = 0; i < words.length; i++) {
			if (words[i].matches(FILTER_REGEX)) {
				words[i] = words[i].replaceAll("[^\\w]", "");
				// if only letters inside the word
				if (USE_STEMMING && words[i].matches("[a-zA-Z]+")) {
					// transform our word to the root form
					String temp = Stemmer.readString(words[i]).get(0);
					// prevent words like 'ha'
					if (temp.length() > 5) {
						words[i] = temp;
					}
				}
				wordList.add(words[i]);
			}
		}
		wordList.removeAll(FILTER1);
		wordList.removeAll(FILTER2);
		for (String str : wordList) {
			if (str.length() < 1) {
				System.out.println(str);
			}
		}
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
			String[] filteredWords = getFilteredWords(i.stringValue(attrIdx));
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
						String[] words = getWords(txt);
						int n = words.length;

						ArrayList<Thread> inner_threads = new ArrayList<>();

						// iterate over vocabulary
						for (String w_k : vocabulary) {
							// calculate probabilities inside thread
							Thread inner_t = new Thread() {
								public void run() {
									double n_k = count(words, w_k);

									/*//TWCNB
									n_k = Math.log(n_k + 1);
									double num =  v_js.size();
									double denum = 0.0;

									ConcurrentHashMap<String, List<String>>
											docs = new ConcurrentHashMap<>();
									for (String v_j2 : v_js) {
										docs.put(v_j2, getDoc(data,
												v_j2, attrIdx)
												.getValue());

									}
									for (Map.Entry<String, List<String>> e :
											docs.entrySet()) {
										List<String> doc = e.getValue();
										String v_j2 = e.getKey();
										if (doc.contains(v_j2)) {
											denum++;
										}
									}

									n_k *= Math.log(num / denum);
									Vector<Double> somethings = new Vector<>();
									double something = 0.0;


									for (String w_k2 : vocabulary) {

										double count = count(words, w_k2);
										somethings.add(count * count);
									}

									for (Double d : somethings) {
										something += d;
									}
									n_k /= Math.sqrt(something);
									*/
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

			for (Map.Entry<Map.Entry<String, String>, Double> entry1 : p_w_v
					.entrySet()) {
				Map.Entry c = entry1.getKey();
				double temp = 0.0;
				for (Map.Entry<Map.Entry<String, String>, Double> entry2 : p_w_v
						.entrySet()) {
					temp += entry2.getValue();
				}
				double result = entry1.getValue() / temp;
				p_w_v.put(c, result);
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
