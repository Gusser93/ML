#Implementierung    



##Allgemein
Die Implementierung des Classifiers ist vollständig losgelöst von Weka. Alle Klassen zum Einlesen von Datensätzen, sowie jene zur Representation der Selbigen wurden nach dem Vorbild von Weka implementiert.    
Zu diesen Klassen zählen: `Attribute`, `AttributeType`, `Instance`, `Instances`. Das eingeführte `Classifier` Interface soll hierbei eine leichtere Portierung auf die Weka Klassen ermöglichen. Der NaiveBayes Algorithmus wurde entsprechend der Vorlesung implementiert.

##Optimierungen

###runtime
- Die Berechnung für jedes v<sub>j</sub> läuft in einem eigenen Thread (7% Zeiteinsparung)
- Die Berechnung der Wahrscheinlichkeiten läuft in eigenen Threads (50% Zeiteinsparung)

###performance
- Stemming: Wörter werden auf ihren Wortstamm zurückgeführt z.B. programming, programmed => programm, program (Keine signifikante Verbesserung der accuracy)
- Filter: Häufigste Wörter, sowie ausgewählte Wörter werden herausgefiltert (Erhört accuracy um rund 35%)
- Score: siehe Paper von McCallum
