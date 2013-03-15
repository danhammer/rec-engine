# simple recommendation engine in cascalog

This project contains a series of simple cascalog queries that serve
as a basic recommendation engine.  Suppose, for example, that each
consumer purchases a subset of available products.  The engine will
return a ranked list of recommended products for each user based on
the purchasing patterns of other users, much like Amazon's "users who
bought X also bought Y" recommendations.  The queries are written to
run on an arbitrarily large EC2 cluster.

A user's preference for a product can be represented as a directed
edge.  This directed framework is a little uneccessary for this simple
application; but we generalize this code for extensive form games,
where the directed framework is useful.  Thus, the preference of each
user for a particular product is represented as a directed edge, so
the representation of user `'u:1'` buying product `'p:1'` is 
```bash
                                         'u:1' ----> 'p:1'
```
This directed network can be reframed as an undirected network of
users by collapsing user-product-user connections to user-user
connections.  Consider, for example, users `'u:1'` and `'u:2'` that
both bought product `p:1'`.  Then the representation of the graph
collapse is
```bash
                       'u:1' ----> 'p:1' <---- 'u:2'    =>    'u:1' ---- 'u:2'
```
If `'u:2'` buys another product `'p:2'` that `'u:1'` does not, then we can
establish a link between `'u:1'` and `'p:2'` in order to recommend this
product to user `'u:1'`:
```bash
                       'u:1' ---- 'u:2' ----> 'p:2'    =>    'u:1' ----> 'p:2'
```
The edges are assumed homogenous and the edge weights are constant.
The recommendations can be greatly improved by creating a more refined
measure of connectedness.  For now, however, the rough draft
recommendation engine works; and we can begin to build an API around
the output.

The [test namespace] contains a simulated Hylo network, with three
users and seven products.  The clojure representation is a vector of
tuples representing edges:

```clojure
(def product-src
  [["aaron" "a"]
   ["aaron" "b"]
   ["aaron" "c"]
   ["aaron" "d"]
   ["aaron" "e"]
   ["dan" "a"]
   ["dan" "b"]
   ["dan" "d"]
   ["sander" "a"]
   ["sander" "d"]
   ["sander" "f"]
   ["sander" "g"]])
```

Using the [loom](https://github.com/jkk/loom) project, we can
visualize this network:

```clojure
(loom.io/view (apply loom.graph/digraph product-src))
```
![](http://i.imgur.com/PeZmzTl.png)

The recommender will yield the ranked products not yet followed by each
user:

```clojure
(recommender product-src) => (produces [["aaron"  ["f" "g"]]
                                        ["dan"    ["c" "e" "f"]]
                                        ["sander" ["b" "c" "e"]]])
```
