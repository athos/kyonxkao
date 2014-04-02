(ns user
  (:require [clojure.tools.namespace.repl :refer [refresh refresh-all]]
            [clojure.repl :refer :all]
            [clojure.pprint :refer [pp]]
            [kyonxkao.vm :refer [run]]))

(def test-env
  '{$1 [100], $2 [200], $3 [0], $4 [0] :flag false})

(def test-program
  '{0 [[:mov $1 104]
       [:pr $1]
       [:mov $2 101]
       [:pr $2]]
    1 [[:mov $3 108]
       [:pr $3]
       [:mov $4 108]
       [:pr $4]
       [:mov $1 111]
       [:pr $1]]})
