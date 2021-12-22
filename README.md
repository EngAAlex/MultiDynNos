# MultiDynNoS

MultiDynNoS is the first multi-level event based graph drawing algorithm. 

The following links apply for the previously published short paper version of this research.

[Link to the Video](https://www.youtube.com/watch?v=nnQwiNTurXc)

[Link to the Paper](https://www.cvast.tuwien.ac.at/multidynnos)


# Installation

#### Requirements

- **Build**: To build MultiDynNoS, Maven is required.
- **Layout**: To produce its layouts, MultiDynNoS requires a version of [GraphViz](https://graphviz.org/documentation/) installed on the system. By default, MultiDynNoS looks for a Windows Subsystem for Linux (WSL) install. Please modify **COMMAND_LINES** variables in **SFDPExecutor** class if needed to point at the command to run Graphviz on your machine.

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
This software has **three** modes: **Layout**, **Metrics**, and **Plot Slices**.

#### Layout Mode:
It is used to run DynNoSlice or MultiDynNoS on a single graph. The result can be plotted as a space time cube or shown as animation on a new window.

To run the system in this mode, please run:

```
$ java -jar /path/to/multi-dynnoslice-1.0.0-complete.jar <MODE> <GRAPH> <LAYOUT> <OPTIONS>
```

As an example, to show the animation of RAMP graph using MultiDynNoS, the command would be the following:

```
$ java -jar /path/to/multi-dynnoslice-1.0.0-complete.jar animate ramp multi
```

| Mode | Command Line Argument | Description |
| ------ | ------ | ------ |
| Animate | ```animate``` | Provides a 30 seconds animation of the resulting layout. |
| Cube | ```showcube``` | Shows the trajectories of the nodes in a space time cube. |
| Metrics | ```metrics``` | Boots the system in compute metric mode. |
| Plot Slices | ```dump``` | Selects the Plot Slices mode. |
| Help | ```help``` | Shows a complete but compact definition of the system usage.

| Graph | Command Line option | Description | Reference |
| ------ | ------ | ------ | ------ |
| *Smaller Graphs* | ------ | ------ | ------ |
| VanDeBunt* | ```vandebunt``` | Shows the relationships between 32 freshmen at seven different time points. | [*](https://link.springer.com/article/10.1023/A:1009683123448)
| Newcomb* | ```newcomb``` | Contains the sociometric preference of 17 members of a fraternity | [*](https://psycnet.apa.org/record/2008-17460-015)
| InfoVis* | ```infovis``` | Is a co-authorship network for papers published in the InfoVis conference from 1995 to 2015. | [*](http://www.cc.gatech.edu/gvu/ii/citevis/infovis-citation-data.txt)
| Dialogs | ```pride``` | lists the dialogues between characters in the novel Pride and Prejudice in order. | [*](https://link.springer.com/chapter/10.1007/978-3-319-46224-0_7)
| Rugby-Tweets | ```rugby``` | Is a network derived from over 3,000 tweets involving teams in the "Guinness Pro12" rugby competition. | [*](https://ieeexplore.ieee.org/abstract/document/8580419/)
| *Larger Graphs* | ------ | ------ | ------ | 
| MOOC | ```mooc``` | This dataset represents the actions taken by users on a popular massive open online class platform. | [*](https://dl.acm.org/doi/abs/10.1145/3292500.3330895?casa_token=nUM3JyeDa24AAAAA:eSXXwXuBdb0Nz2hU-prOM53jKea12sxcLEuz3WRpWTeZ-PZN5dZMdfWZjerfObhnBaJt9CgLcpUd)
| College Message | ```college``` | SThis dataset is comprised of private messages sent on an online social network at the University of California, Irvine. | [*](https://asistdl.onlinelibrary.wiley.com/doi/abs/10.1002/asi.21015?casa_token=hJRB2bR4UvEAAAAA:Twq4CupkDrrejnj9KnOOXssiA_8Agx_sqAjc_oh-qeYDTp1jp1f6XAbNnFp69M_OuOnz50dSEwJprFg)
| Reality Mining | ```reality``` | This data comes from The Reality Mining study. | [*](https://link.springer.com/content/pdf/10.1007/s00779-005-0046-3.pdf)
| Ramp Infection Map | ```ramp``` | Contact-tracing Network. | [*](https://github.com/ScottishCovidResponse/Contact-Tracing-Model)
| *Custom Graph* | ```custom``` | See below for how to run the system with custom graphs. | ------ |


**Timesliced graphs*

| Layout | Command Line Argument | Description |
| ------ | ------ | ------ |
| Multi-DynNoSlice | ```multi``` | Run the multi-level algorithm on the selected graph. |
| DynNoSlice | ```single``` | Run a single level event-based layout on the selected graph. |
| SFDP | ```sfdp``` | Run the sfdp layout algorithm on the selected graph. |

| Options | Command Line Argument | Description |
| ------ | ------ | ------ |
| Delta | ```-d <value>``` | Run the layout with a custom delta. |
| CLI Tau | ```-t <value>``` | Run the layout with a custom tau. |
| ManualTau | ```-T``` | Run the layout with the ManualTau found in the dataset class (if available). If not specified, defaults to automatically computed Tau. |
| Bend Transfer | ```-bT``` | Enables MultiDynNoSlice bend transfer extension |
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

When used, it will run the metrics to reproduce the experiments in the paper. It is run with the following:

```
$ java -jar /path/to/multi-dynnoslice-1.0.0-complete.jar metrics [Metrics Options]
```

| Option name | Command Line Argument | Description |
| ------ | ------ | ------ |
| Smaller | ```--smaller``` | Executes the experiment on the smaller graphs |
| Larger | ```--larger``` |  Executes the experiment on the larger graphs |
| Multi | ```--multi``` | Executes the experiment using MultiDynNoS |
| Single | ```--single``` | Executes the experiment using DynNoSlice |
| Visone | ```--visone``` | Computes metrics for stored Visone graphs |
| SFDP | ```--sfdp``` | Flattens graphs and executes the experiment using SFDP |
| ManualTau | ```--manualTau``` | Run the experiments with the ManualTau found in the dataset class (if available). Defaults to automatically computed Tau. |
| Bend Transfer | ```--bT``` | Enables MultiDynNoSlice bend transfer extension (MultiDynNoS only) |
| Verbose | ```--verbose``` | Extra output on console during computation (MultiDynNoS only) |
| Output | ```--out /path/desired/``` | The path where to save the resulting statistics file. Defaults to working directory. (MultiDynNoS only) |

More than one option can be selected. For example, to compute the metrics for both small and large graphs using Visone and MultiDynNoS, the resulting command is as follows:

```
$ java -jar /path/to/multi-dynnoslice-1.0.0-complete.jar metrics --larger --smaller --multi --visone --out /path/to/file
```

At least one graph category and one layout method must be selected for the experiment to run, except for options marked by an asterisk (*): in that case, only the graph category is needed and layout options will be ignored.


#### Plot Slices Mode (experimental):

When used, it will create a specified number of GML files that correspond to the evolution of the network over time. For example, if 5 slices are requested, the same number of GML files will be created, each representing the layout of the network at 1/5 intervals. Only the nodes and edges that are currently present in the selected interval will be included. Please note that this only works with MultiDynNoS and with the preloaded graphs.

```
$ java -jar /path/to/multi-dynnoslice-1.0.0-complete.jar dump [Metrics Options]
```

The same options of the metrics mode apply in this case as well.

# Todos

 - Write MORE Tests
 

# Acknowledgments

Contains original *DynNoSlice* software by Paolo Simonetto.

License
----

[Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0)
   
