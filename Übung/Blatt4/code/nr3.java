// erzeugen eines Remove Filters, der die Attribute an den angegebenen Indicies entfernt
Remove rm = new Remove();
rm.setAttributeIndices("1,4,6,last");
// erzuegen eines neuen untrainierten J48 Classifier
// unpruned := versuche nicht den Baum zu vereinfachen => größerer Baum
J48 j48 = new J48();
j48.setUnpruned(true);
// erzeugen eines FilteredClassifier, der auf die Daten zuerst den angegeben Filter anwendet, bevor der Algorithmus (J48) trainiert wird
FilteredClassifier fc = new FilteredClassifier();
fc.setFilter(rm);
fc.setClassifier(j48);
// erzeugen einer neuen CVParameterSelection, die Parameter auf Basis von cross-validation auswählt
CVParameterSelection cvp = new CVParameterSelection();
cvp.setClassifier(fc);
// C := confidence-Parameter 0.1 bis 0.5 mit einer Schrittweite von 5
String[] params = new String[]{"C", "0.1","0.5","5"};
cvp.setCVParams(params);
// trainiere den Classifier und schicke die Trainingsdaten zuerst durch den Filter
cvp.buildClassifier(dataset);
// treffe eine Vorhersage über die Klassenverteilung für die gegebenen Instanzen
// die Testdaten gelangen hierbei nicht durch den Filter
cvp.distributionForInstance(instance);