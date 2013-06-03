# About PlayGraph
## What is a PlayGraph?

PlayGraphs are a more powerful version of the common "playlist". Items can be sequenced and, like a graph,
there may be more than one way to traverse this collection of items. This offers a more powerful and
flexible framework for content consumption and learning.

## What can a node contain?

A node in a PlayGraph can contain three different things:

* Arbitrary data
* Another graph
* A node pool (described later down)

### Arbitrary data

A node can contain anything. If you want to place the contents of a web page in it, that's just fine. You
could also put Base 64-encoded binary data in a node if you felt like it. It doesn't matter. When a node
with arbitrary data is being played, it simply returns its content. It's up to the graph "player" to know
how to interpret and display the data.

### Another graph

A node may also contain a pointer to another PlayGraph. When the node is played, the current graph,
including the position in it, is pushed onto a context stack and the new graph is begun. Whenever the graph
finished, the saved graph is popped off the context stack and resumed.

### A node pool

Similar to a node containing a graph, when a node containing a pointer to a node pool is run, the current
location is saved to the context stack and the node pool is executed.


## How does a node pool work?

The purpose of a node pool is to provide dynamic possibilities to a graph. It is defined by a collection of
nodes and a selection mechanism. First off, the selection mechanism is run and generates a list of nodes to
visit drawing from the defined collection of nodes. These nodes are then run in the generated order,
ignoring their own branching logic. When all the nodes are finished, then the node pool is done executing.

The following is an example of how a node pool could be used. The graph is a tutorial on French vocabulary.
At a certain point, the student is quizzed on vocab. The nodes in the node pool each would contain a
question about a vocab word. The selection mechanism would randomly select 10 of these. So, to the student,
it would appear that he or she was simply asked 10 random questions about the vocabulary.

# Setup

You'll need to have a MySQL database. Configure `application.conf` accordingly.

PlayGraph is a Play 2.1.0 application, so ensure that you have that. There are
<a href="http://www.playframework.com/documentation/2.1.1/Production">instructions</a> on the play website on running
and deploying a play application. The basics are these:

To run the test cases

```
play test
```

To run the development server

```
play run
```

To run the production server

```
play "start -DapplyEvolutions.default=true"
```