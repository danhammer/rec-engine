(ns rec-engine.core
  "This namespace contains a series of queries that will return a list
  of recommended products for each user. Note that the structure of
  the environment, as currently specified, creates a unique, directed
  network whereby users buy products.  This directed network can be
  reframed as an undirected network of users by collapsing
  user-product-user connections to user-user connections.  Consider,
  for example, users 'a' and 'b' that both purchase 'p:1'.  Then the
  representation of the graph collapse is

         'a' ----> 'p:1' <---- 'b'    =>    'a' ---- 'b'

  If 'b' purchases another product 'p:2' that 'a' does not, then we can
  establish a link between 'a' and 'p:2' in order to recommend this
  product to user 'a':

       'a' ---- 'b' ----> 'p:2'    =>    'a' ----> 'p:2'

  The edges are assumed homogenous and the edge weights are constant.
  The recommendations can be greatly improved by creating a more
  refined measure of connectedness.  For now, however, the rough draft
  recommendation engine works; and we can begin to build an API around
  the output."
  (:use [cascalog.api])
  (:require [cascalog.ops :as ops]))

;; set the number of ranked products to recommend for each user
(def ^:const num-ranked 3)

(defn edge-count
  "accepts a cascalog source that defines the connections between a
  user and a product; each record indicates a separate connection.
  returns a tap with the user, new product, and the number of
  first-degree connections with that previously unpurchased product."
  [edge-src]
  (let [link-src (<- [?product ?user-seed ?user-end]
                     (edge-src ?user-seed ?product)
                     (edge-src ?user-end ?product)
                     (not= ?user-seed ?user-end))]
    (<- [?user-seed ?product ?ct]
        (link-src _ ?user-seed ?user)
        (edge-src ?user ?product)
        (ops/count ?ct))))

(defbufferop product-set
  "accepts a sequence of user-products tuples and returns a set of
  unique products associated with that user."
  [tuples]
  (let [product-coll (for [[user product] tuples] product)]
    [(set product-coll)]))

(defn new-products
  "accepts a source of the user-products connections and returns new
  products for each user, along with the count of connectedness"
  [edge-src]
  (let [src (edge-count edge-src)]
    (<- [?user ?new-product ?ct]
        (edge-src ?user ?product)
        (src ?user ?new-product ?ct)
        (product-set ?user ?product :> ?product-set)
        ((ops/negate #'contains?) ?product-set ?new-product))))

(defbufferop rank-products
  "accepts new products and measure of connectedness for each
  user. each tuple is of the form [user-id product-id]"
  [tuples]
  (let [a (map first (sort-by second > tuples))]
    [[(take num-ranked (flatten a))]]))

(defn recommender
  "accepts a source of user-product connections and returns a set of
  recommended products as a vector"
  [edge-src]
  (let [src (new-products edge-src)]
    (<- [?user ?recommeded-products]
        (src ?user ?product ?val)
        (rank-products ?product ?val :> ?recommeded-products))))
