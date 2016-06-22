#Implementierung    



##Allgemein
Die Implementierung des Classifiers ist vollständig losgelöst von Weka. Alle Klassen zum Einlesen von Datensätzen, sowie jene zur Representation der Selbigen wurden nach dem Vorbild von Weka implementiert.    
Zu diesen Klassen zählen: `Attribute`, `AttributeType`, `Instance`, `Instances`. Das eingeführte `Classifier` Interface soll hierbei eine leichtere Portierung auf die Weka Klassen ermöglichen.

##DomainNaiveBayes

Der Algorithmus funktioniert im wesentlichen wie in der Vorlesung beschrieben. 

###buildClassifier
  
Zum Trainieren des Classifiers wird die `buildClassfier` Methode mit einem entsprechenden Datensatz aufgerufen. Es wird nun das erste `Attribute` aus diesem Datensatz, das *__nicht__* dem Klassenattribute entspricht, für die weitere Verwendung gespeichert (1).    
Aus den eingelesenen Daten werden nun alle Wörter extrahiert und in einem Vocabulary Liste gespeichert. Wörter die entweder häufig in der englischen Sprache vorkommen, oder aber die Qualität der Ergebnisse auf andere weise verschlechtern werden nicht in das Vokabular übernommen. Diese Wörter wurden vorab von uns definiert und durch ausprobieren optimiert.   
Für jede target value des Klassenattributes wird nun folgender Algorithmus angewandt:

Wenn der `classValueString` einer `Instance` der aktuellen target value entspricht, so speichere die `Instance` in einer Subliste `doc_j` und speichere den Wert der `Instance` für das in (1) gewählte Attribute in einem Leerzeichen separierten `String`, `text_j` ab. Auf diese Weise enthält dieser `String` alle Wörter der Instanzen in der Subliste.    
Nach der Formel von Bayes errechnet man nun ein P(v<sub>j</sub>) = (Größe des Datensatzes)/(Größe der Subliste). v<sub>j</sub> meint hierbei die aktuelle target value. Nach dem Filtern von `text_j` mittels der gleichen Wörterliste, die bereits vorhin verwendet wurde, zählt man, wie häufig jedes Wort aus dem Vokabular in dem bereinigten `text_j` vorkommt. Die Wahrscheinlichkeit P(w<sub>k</sub> | v<sub>j</sub>) = (Häufigkeit des Wortes + 1)/(Gesamtzahl aller Wörter) wird nun für jedes Wort gespeichert. 


###classifyInstance
