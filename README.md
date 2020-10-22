# MultiDyNoSlice 

MultiDyNoSlice is the first multi-level event based graph drawing algorithm. 

# Installation

#### Requirements

- **Build**: To build MultiDynNos, Maven is required.
- **Layout**: To produce its layouts, MultiDynNoSlice requires a version of [GraphViz](https://graphviz.org/documentation/) installed on the system. By default, MultiDynNos looks for a Windows Linux Subsystem (WLS) install. Please modify **COMMAND_LINES** variables in **SFDPExecutor** class if needed to point at the command to run Graphviz on your machine.

#### Build

Clone the repository and navigate to the root folder of the project. Build the software by running:

```
$ mvn clean package -DskipTests
```

# Usage

Building the software will produce, in the **target** folder, several files. The build that contains the software, its dependencies, and its data is **multi-dynnoslice-1.0.0-complete.jar**. To show a quick help description, please run the following command:

```
$ java -jar /path/to/multi-dynnoslice-1.0.0-complete.jar
```
This software has **two** modes: **Layout** and **Metrics**.

#### Layout Mode:
It is used to run DynNoSlice or Multi-DynNoSlice on a single graph. The result can be plotted as a space time cube or shown as animation on a new window.

To run the system in this mode, please run:

```
$ java -jar /path/to/multi-dynnoslice-1.0.0-complete.jar <MODE> <GRAPH> <LAYOUT> <OPTIONS>
```

| Mode | Command Line Argument | Description |
| ------ | ------ | ------ |
| Animate | ```animate``` | Provides a 30 seconds animation of the resulting layout. |
| Cube | ```showcube``` | Shows the trajectories of the nodes in a space time cube. |
| Metrics | ```metrics``` | Boots the system in compute metric mode. |
| Help | ```help``` | Shows a complete but compact definition of the system usage.

| Graph | Command Line option | Description | Reference |
| ------ | ------ | ------ | ------ |
| *Smaller Graphs* | ------ | ------ | ------ |
| Newcomb* | ```newcomb``` | Contains the sociometric preference of 17 members of a fraternity | [*]()
| VanDeBunt* | ```vandebunt``` | Shows the relationships between 32 freshmen at seven different time points. | [*]()
| InfoVis* | ```infovis``` | Is a co-authorship network for papers published in the InfoVis conference from 1995 to 2015. | [*]()
| Dialogs | ```pride``` | lists the dialogues between characters in the novel Pride and Prejudice in order. | [*]()
| Rugby-Tweets | ```rugby``` | Is a network derived from over 3,000 tweets involving teams in the "Guinness Pro12" rugby competition. | [*]()
| *Larger Graphs* | ------ | ------ | ------ | 
| MOOC | ```mooc``` | This dataset represents the actions taken by users on a popular massive open online class platform. | [*]()
| Bitcoin Alpha | ```bitalpha``` | This is who-trusts-whom network of people who trade using Bitcoin on a platform called Bitcoin Alpha. | [*]()
| Bitcoin OTC | ```bitotc``` | This is who-trusts-whom network of people who trade using Bitcoin on a platform called Bitcoin OTC. | [*]()
| College Message | ```college``` | SThis dataset is comprised of private messages sent on an online social network at the University of California, Irvine. | [*]()
| Reality Mining | ```reality``` | This data comes from The Reality Mining study. | [*]()
| Ramp Infection Map | ```ramp``` | Contact-tracing Network. | [*]()
| *Custom Graph* | ```custom``` | See below for how to run the system with custom graphs. | ------ |


**Timesliced graph*

| Layout | Command Line Argument | Description |
| ------ | ------ | ------ |
| Multi-DynNoSlice | ```multi``` | Run the multi-level algorithm on the selected graph. |
| DynNoSlice | ```single``` | Run a single level event-based layout on the selected graph. |
| SFDP | ```sfdp``` | Run the sfdp layout algorithm on the selected graph. |

As an example, to show the animation of RAMP graph using Multi-DynNoSlice, the command would be the following:

```
$ java -jar /path/to/multi-dynnoslice-1.0.0-complete.jar animate ramp multi
```

| Options | Command Line Argument | Description |
| ------ | ------ | ------ |
| Delta | ```-d <value>``` | Run the layout with a custom delta. |
| Tau | ```-t <value>``` | Run the layout with a custom tau. |
| Text Out | ```-o /path/to/graph``` | If specified, the output graph will be converted to text file and saved where specified.

##### Custom Graph:

To run the system with a user-provided graph, please use the following syntax:

```
$ java -jar /path/to/multi-dynnoslice-1.0.0-complete.jar animate custom /path/to/node/file /path/to/edge/file <LAYOUT> <OPTIONS>
```

With ```<LAYOUT>``` and ```<OPTIONS>``` the same as above. An example of node and edge files can be found below.

```
Node dataset example:
	Alice,1,5
	Bob,2,4.6
	Carol,1.5,3
	<Node ID>,<Start Time>,<Duration>

Edge dataset example:
	Alice,Bob,2.5,1
	Bob,Carol,2.1,0.6
	<Source Node ID>, <Target Node ID>,<Start Time>,<Duration>
```

##### Window interaction:

It is possible to pan and rotate the space time cube and animation by dragging with the mouse left and right buttons respectively. In animation mode, press ```P``` to play the animation and ```S``` to stop it.

#### Metrics Mode: 
It is used to compute the stress, movement, and crowing metrics for the selected layout methods and graph typologies. To run it, please run the following command:

```
$ java -jar /path/to/multi-dynnoslice-1.0.0-complete.jar metrics <OPTIONS>
```

| Option name | Command Line Argument | Description |
| ------ | ------ | ------ |
| Smaller | ```--smaller``` | Executes the experiment on the smaller graphs |
| Larger | ```--larger``` |  Executes the experiment on the larger graphs |
| Multi | ```--multi``` | Executes the experiment using MultiDynNoS |
| Single | ```--single``` | Executes the experiment using DynNoSlice |
| Visone | ```--visone``` | Computes metrics for stored Visone graphs |
| SFDP | ```--sfdp``` | Flattens graphs and executes the experiment using SFDP |
| Verbose | ```--verbose``` | Extra output on console during computation |
| Output | ```--out /path/desired/``` | The path where to save the resulting statistics file. Defaults to working directory. |

More than one option can be selected. For example, to compute the metrics for both small and large graphs using visone and MultiDynNos, the resulting command is as follows:

```
$ java -jar /path/to/multi-dynnoslice-1.0.0-complete.jar metrics --larger --smaller --multi --visone --out /path/to/file
```

At least one graph category and one layout method must be selected for the experiment to run.

# Todos

 - Write MORE Tests

# Acknowledgments

Contains original *DynNoSlice* software by Paolo Simonetto.

License
----

[Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0)
   
