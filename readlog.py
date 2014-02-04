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
	print "\\begin{tabular}{l" + ("|p{2cm}" * (len(table[0]) - 1)) + "|}"
	for line in table:
		for item in line:
			print str(item),
			if item != line[-1]:
				print "&",
		if line != table[-1]:
			print "\\\\"
	print ""
	print "\\end{tabular}"

def combinevalue(filename, prop, agent, environments):
	xmldoc = minidom.parse(filename)
	outcomes = xmldoc.getElementsByTagName("NegotiationOutcome")
	pearsons = list()
	for outcome in outcomes:
		results = outcome.getElementsByTagName("resultsOfAgent")
		for result in results:
			if result.attributes['agentName'].value == agent and (result.attributes['utilspace'].value == environments[0] or result.attributes['utilspace'].value == environments[1]):
				pearson = float(result.attributes[prop].value)
				pearsons.append(pearson)
	return pearsons

#print numpy.mean(readpearson(filename))

own = 'bs: Group7_BS2 {Pconcede=0.05, Ppareto=0.5, averageOver=5.0, concedeFactor=0.3, concedeSteps=10.0, discount=0.0, e=0.3, niceFactor=0.5, phase2=0.1, phase3=0.95} as: Group7_AS {acceptCurveApproach=0.8, acceptCurveStart=1.0, acceptCurveType=2, capWorstMinimal=0.6, capWorstSlope=-0.3, panicConcede=0.05, panicWhenBidsLeft=3, percentDurationWeight=0.5, timeWindow=0.2} om: Group7_OM: Adaptive Frequency Model  oms: Group7_OMS '
gabber = 'Gahboninho V3'
hardHeaded = 'HardHeaded'
negotiator = 'bs: 2011 - The Negotiator  as: 2011 - The Negotiator  om: CUHKFrequencyModelV2  oms: Best bid {t=1.0}'

agents = [hardHeaded, gabber, negotiator, own]
shortNames = ['HardHeaded', 'Gahboninho', 'The Negotiator', 'Self']

table = list();
table.append(['', 'Average Time Agreement', 'Average Discounted Util', 'Average Dist. to Nash', 'Average Dist. to Pareto',  'Average Dist. to Kalai']);
for i in range(len(agents)):
	agent = agents[i]
	table.append([shortNames[i], 
		"%0.3f" % (numpy.mean( readvalues(filename, 'timeOfAgreement', agent, True))),
		"%0.3f" % (numpy.mean(readvalues(filename, 'discountedUtility', agent))), 
		"%0.3f" % (numpy.mean(readvalues(filename, 'nash_distance', agent))),
		"%0.3f" % (numpy.mean(readvalues(filename, 'pareto_distance', agent))),
		"%0.3f" % (numpy.mean(readvalues(filename, 'kalai_distance', agent)))])
latexprint(table)


## Create the table per domain
spaces = [["etc/templates/anac/y2011/Car/adg_deal.xml", "etc/templates/anac/y2011/Car/adg_deal2.xml"],
	["etc/templates/anac/y2011/Amsterdam/Amsterdam_party2.xml", "etc/templates/anac/y2011/Amsterdam/Amsterdam_party1.xml"],
	["etc/templates/anac/y2011/Camera/camera_seller_utility.xml", "etc/templates/anac/y2011/Camera/camera_buyer_utility.xml"],
	["etc/templates/anac/y2011/NiceOrDie/NiceOrDie1.xml", "etc/templates/anac/y2011/NiceOrDie/NiceOrDie2.xml"],
	["etc/templates/anac/y2011/Grocery/Grocery_domain_sam.xml", "etc/templates/anac/y2011/Grocery/Grocery_domain_mary.xml"],
	["etc/templates/anac/y2011/IS_BT_Acquisition/IS_BT_Acquisition_BT_prof.xml", "etc/templates/anac/y2011/IS_BT_Acquisition/IS_BT_Acquisition_IS_prof.xml"],
	["etc/templates/anac/y2011/Laptop/laptop_buyer_utility.xml", "etc/templates/anac/y2011/Laptop/laptop_seller_utility.xml"]
	]
spacesNames = ['Car', 'Amsterdam Party', 'Camera', 'Nice or Die', 'Grocery', 'IS BT Acquisition BT prof', 'Laptop']

def printTableForDomains(filename, agents, shortNames, spaces, spacesNames):
	table = list();
	agentNames = ['']
	for agent in shortNames:
		agentNames.append(agent)
	table.append(agentNames)
	for i in range(len(spaces)):
		curLine = [spacesNames[i]]
		for j in range(len(agents)):
			curLine.append("%0.3f" % (numpy.mean(combinevalue(filename, 'discountedUtility', agents[j], spaces[i]))))
		table.append(curLine)
	latexprint(table)

printTableForDomains(filename, agents, shortNames, spaces, spacesNames)


## Create another table per domain
spaces = [
	["etc/templates/anac/y2011/Camera/camera_seller_utility.xml", "etc/templates/anac/y2011/Camera/camera_buyer_utility.xml"],
	["etc/templates/anac/y2011/Grocery/Grocery_domain_sam.xml", "etc/templates/anac/y2011/Grocery/Grocery_domain_mary.xml"],
	["etc/templates/anac/y2011/IS_BT_Acquisition/IS_BT_Acquisition_BT_prof.xml", "etc/templates/anac/y2011/IS_BT_Acquisition/IS_BT_Acquisition_IS_prof.xml"],
	["etc/templates/anac/y2011/Laptop/laptop_buyer_utility.xml", "etc/templates/anac/y2011/Laptop/laptop_seller_utility.xml"]
	]
spacesNames = ['Camera', 'Grocery', 'IS BT Acquisition BT prof', 'Laptop']
filename = "Logs/Discount Tests/2014-02-04 06.44.48.xml"

own = "bs: Group7_BS2 {Pconcede=0.05, Ppareto=0.5, averageOver=5.0, concedeFactor=0.3, concedeSteps=10.0, discount=1.0, e=0.3, niceFactor=0.5, phase2=0.1, phase3=0.95} as: Group7_AS {acceptCurveApproach=0.8, acceptCurveStart=1.0, acceptCurveType=2, capWorstMinimal=0.6, capWorstSlope=-0.3, panicConcede=0.05, panicWhenBidsLeft=3, percentDurationWeight=0.5, timeWindow=0.2} om: Group7_OM: Adaptive Frequency Model  oms: Group7_OMS "
gabber = 'Gahboninho V3'
hardHeaded = 'HardHeaded'
negotiator = 'bs: 2011 - The Negotiator  as: 2011 - The Negotiator  om: CUHKFrequencyModelV2  oms: Best bid {t=1.0}'

agents = [hardHeaded, gabber, negotiator, own]

printTableForDomains(filename, agents, shortNames, spaces, spacesNames)

