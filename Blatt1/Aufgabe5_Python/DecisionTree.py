# Created by David Klopp
# 24.04.2016

#!/usr/bin/env python
# -*- coding: utf-8 -*-

# tested only with python3.4
# requieres liac-arff module

import os.path
import math
import arff

class DecisionTree(object):
    def __init__(self, arff_file_path=None):
        self.attr = []
        self.data = []
        if arff_file_path is not None:
            self.load_arff(arff_file_path)

    def load_arff(self, arff_file_path):
        if os.path.isfile(arff_file_path):
            extension = os.path.splitext(arff_file_path)[1]
            if extension == '.arff':
                arff_file = arff.load(open(arff_file_path, 'r'))
                self.data, self.attr = arff_file['data'], arff_file['attributes']

    def information_gain(self, attribute, indices):
        s = [self.data[i] for i in range(0, indices[-1]+1)]
        a = next(filter(lambda x: x[0] == attribute, self.attr))
        
        idx, sum = self.attr.index(a), 0
        for v in a[1]:
            s_v = list(filter(lambda e: e[idx] == v, s))
            sum += ( (len(s_v)/len(s)) * self.h(s_v) )

        return self.h(s) - sum

    # Entropy
    def h(self, instances):
        def log2(x):
            return 0 if x == 0.0 else math.log(x)/math.log(2)
        
        class_idx = -1
        class_attr = self.attr[class_idx]
        num_of_values = len(class_attr[1])

        values = [0 for i in range(0, num_of_values)]
        for v in range(0, num_of_values):
            for instance in instances:
                if class_attr[1][v] == instance[class_idx]:
                    values[v] += 1;
    
        val_sum = float(sum(values))
        entropy = 0.0;
        for value in values:
            p = float(value)/val_sum
            entropy -= p * log2(p)


        return entropy

    def test(self):
        indices = [i for i in range(0, len(self.data))]
        
        for a in self.attr:
            print('Attribute ' + a[0] + ' has an InformationGain of ' + str(self.information_gain(a[0], indices)))

tree = DecisionTree('/Users/David/Desktop/weather.arff')
tree.test()