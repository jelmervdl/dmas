# Design of Multi Agent Systems

## Inputting A Graph

Graphs can be read from txt files with the following content:

	Number of nodes and number of edges
	4 3
	Sources
	1 2
	Sinks
	0
	Edges
	1 3
	2 3
	3 0
	Node locations
	20 0
	10 20
	-10 20
	0 0

```Number of nodes and number of edges``` should indicate how many vertices and edges a graph has, note that bidirectional edges should be added as two different edges. Nodes are numbered from 0 to ```numberOfNodes - 1```. ```Sources``` indicate places where cars can enter the simulation, ```sinks``` indicate where they leave them, a vertex can be both a source and a sink. Node locations take the center of the screen as the origin, locations may use floats. For each node a location should be given. 

## Batch processing
In the TrafficDemo directory, run ```java -cp target/TrafficDemo-1.0-SNAPSHOT-jar-with-dependencies.jar nl.rug.dmas.trafficdemo.Batch ./input/graaf2.txt ./input/experiment.txt``` after building the project in Netbeans.

The experiment file looks like this:

	time = 150
	iterations = 3

	[variable]
	ratioAutonomousCars = 0.0:0.1:1.0

	[fixed]
	carWidth = 1.47-2.55
	carLength = 2.540-6.0
	viewLength = 4.00-12.00
	actPeriod = 100

time is the number of seconds the simulations simulate, and iterations the number of repititions of the same experiment. Both these variables are required. All other variables are optional. The names are the names of the properties of the Scenario class. Only Parameter arguments can be configured through this file.

In the *variable* section you can name all the variables (using the Parameter range syntax ```start:step:stop```) to name the parameters which are changed. For each possible value of these parameters, `iterations` number of simulations are run. All other parameters are taken from the *fixed* section.

If you name multiple parameters in the *variable* section, it only varies one of them at a time. So if there are two variables, it will first vary the first and take the value for the second from the *fixed* section. Then it will vary the second and take the value for the first from the *fixed* section. So *it does not* take all possible combinations for both variables into account.
