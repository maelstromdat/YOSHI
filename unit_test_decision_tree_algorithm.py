__author__ = 'jackie'

import unittest
import json
import os
from YoshiViz import report_generator,decision_tree_algorithm

file_directory = os.path.join(os.path.abspath('.'), 'YoshiViz', 'input.txt')
file = open(file_directory)
#temp(string) txt to string
temp = file.read()
data = json.loads(temp)

class test_decision_tree_algorithm(unittest.TestCase):

    def setUp(self):
        pass

    def tearDown(self):
        pass

    def test_engagement(self):
        self.assertEqual(decision_tree_algorithm.\
                         engagement(True,9.794,14.22,0.200,1.220),True,'Test of engagement failed')
        self.assertEqual(decision_tree_algorithm.\
                         engagement(True,9.794,14.22,0.200,1.2),False,'Test of engagement failed')

    def test_informality(self):
        self.assertEqual(decision_tree_algorithm.\
                         informality(0.1,0.8,True),True,'test of informality failed')
        self.assertEqual(decision_tree_algorithm.\
                         informality(0.1,0.8,False),False,'test of informality failed')

    def test_situatedness(self):
        self.assertEqual(decision_tree_algorithm.\
                         situatedness(4000,15),True,'test of situatedness failed')
        self.assertEqual(decision_tree_algorithm.\
                         situatedness(4000,16),False,'test of situatedness failed')

    def test_network_of_practice(self):
        self.assertEqual(decision_tree_algorithm.\
                         network_of_practice(0.9),True,'Test of network_of_practice failed')
        self.assertEqual(decision_tree_algorithm.\
                         network_of_practice(0.8),False,'Test of network_of_practice failed')


if __name__ =='__main__':
    unittest.main()