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
