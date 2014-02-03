#!/bin/python
from xml.dom import minidom
import numpy
#Let's read the file.
#filename = "genius/log/2014-02-03 09.47.51.xml"
#filename = "genius/log/2014-02-03 10.45.16.xml"
#filename = "genius/log/2014-02-03 11.05.22.xml"
#filename = "genius/log/2014-02-03 13.42.56.xml"
#filename = "genius/log/2014-02-03 16.03.16.xml"
#filename = "genius/log/2014-02-03 19.56.39.xml"
#filename = "genius/log/om_changed.xml"
filename = "genius/log/2014-02-03 22.16.00.xml"

# Read the pearsons:
def readpearson(filename):
	return readproperty(filename, 'Pearson_Correlation_Bids', 'A')

def readvalues(filename, prop, agent, negOutcome = False):
	xmldoc = minidom.parse(filename)
	outcomes = xmldoc.getElementsByTagName("NegotiationOutcome")
	pearsons = list()
	for outcome in outcomes:
		results = outcome.getElementsByTagName("resultsOfAgent")
		for result in results:
			if result.attributes['agentName'].value == agent:
				pearson = 0.0
				if negOutcome:
					pearson = float(outcome.attributes[prop].value)
				else:
					pearson = float(result.attributes[prop].value)
				pearsons.append(pearson)
	return pearsons

def readstrings(filename, prop, agent, negOutcome = False):
	xmldoc = minidom.parse(filename)
	outcomes = xmldoc.getElementsByTagName("NegotiationOutcome")
	pearsons = list()
	for outcome in outcomes:
		results = outcome.getElementsByTagName("resultsOfAgent")
		for result in results:
			if result.attributes['agentName'].value == agent:
				pearson = ''
				if negOutcome:
					pearson = result.attributes[prop].value
				else:
					pearson = result.attributes[prop].value
				pearsons.append(pearson)
	return pearsons

def latexprint(table):
	print "\\begin{tabular}{" + ("l" * len(table[0])) + "}"
	for line in table:
		for item in line:
			print str(item),
			if item != line[-1]:
				print "&",
		if line != table[-1]:
			print "\\\\"
	print ""
	print "\\end{tabular}"

#print numpy.mean(readpearson(filename))

own = 'bs: Group7_BS2 {Pconcede=0.05, Ppareto=0.5, averageOver=5.0, concedeFactor=0.3, concedeSteps=10.0, discount=0.0, e=0.3, niceFactor=0.5, phase2=0.1, phase3=0.95} as: Group7_AS {acceptCurveApproach=0.8, acceptCurveStart=1.0, acceptCurveType=2, capWorstMinimal=0.6, capWorstSlope=-0.3, panicConcede=0.05, panicWhenBidsLeft=3, percentDurationWeight=0.5, timeWindow=0.2} om: Group7_OM: Adaptive Frequency Model  oms: Group7_OMS '
gabber = 'Gahboninho V3'
hardHeaded = 'HardHeaded'
negotiator = 'bs: 2011 - The Negotiator  as: 2011 - The Negotiator  om: CUHKFrequencyModelV2  oms: Best bid {t=1.0}'

agents = [hardHeaded, gabber, negotiator, own]
shortNames = ['HardHeaded', 'Gahboninho', 'The Negotiator', 'BOA Constructor']

table = list();
table.append(['Agent', 'Average discounted util', 'Avg time of agreement', 'Avg dist nash of agreement', 'Avg pareto', 'Avg KS agree']);
for i in range(len(agents)):
	agent = agents[i]
	table.append([shortNames[i], 
		numpy.mean(readvalues(filename, 'discountedUtility', agent)), 
		numpy.mean(readvalues(filename, 'timeOfAgreement', agent, True)),
		numpy.mean(readvalues(filename, 'nash_distance', agent)),
		numpy.mean(readvalues(filename, 'pareto_distance', agent)),
		numpy.mean(readvalues(filename, 'kalai_distance', agent))])
latexprint(table)


