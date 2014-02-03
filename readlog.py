#!/bin/python
from xml.dom import minidom
import numpy
#Let's read the file.
#filename = "genius/log/2014-02-03 09.47.51.xml"
#filename = "genius/log/2014-02-03 10.45.16.xml"
filename = "genius/log/2014-02-03 11.05.22.xml"

# Read the pearsons:
def readpearson(filename):
	xmldoc = minidom.parse(filename)
	outcomes = xmldoc.getElementsByTagName("NegotiationOutcome")
	pearsons = list()
	for outcome in outcomes:
		results = outcome.getElementsByTagName("resultsOfAgent")
		for result in results:
			if result.attributes['agent'].value == 'A':
				pearson = float(result.attributes['Pearson_Correlation_Bids'].value)
				pearsons.append(pearson)
	return pearsons

print numpy.mean(readpearson(filename))

