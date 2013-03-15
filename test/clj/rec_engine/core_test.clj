(ns rec-engine.core-test
  (:use [midje sweet cascalog]
        rec-engine.core)
  (:require [loom.io]
            [loom.graph]))

(def product-src
  "create a fake edge network, with three users (aaron, dan, sander)
  and seven products 'a' through 'g'"
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

(fact
  "check that the recommender provides the correct set of products"
  (recommender product-src) => (produces [["aaron"  ["f" "g"]]
                                          ["dan"    ["c" "e" "f"]]
                                          ["sander" ["b" "c" "e"]]]))

(defn big-src
  "accepts a number `n` of edge connections between 26 users and 1000
  products and returns a source of user-product edges for input into
  the recommender"
  [n]
  (let [users (flatten (partition 1 "abcdefghijklmnopqrstuvwxyz"))
        create-tuple (fn [] [(rand-nth users) (rand-int 1000)])]
    (vec (take n (repeatedly #(create-tuple))))))
