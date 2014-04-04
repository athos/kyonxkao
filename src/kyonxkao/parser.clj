(ns kyonxkao.parser
  (:require [instaparse.core :as p]
            [clojure.string :as s]
            [clojure.java.io :as io]))

(defn load-wordlist [filename]
  (with-open [r (io/reader (str "resources/wordlist/" filename ".wordlist"))]
    (str (s/join " | " (map #(str \' % \') (line-seq r))) \newline)))

(def ^:private parser
  (p/parser (-> (slurp "resources/grammer.ebnf")
                (s/replace "{{CHARACTERS}}" (load-wordlist "character")))))

(defn parse [source]
  (parser (s/replace source #"[\r\n]" "")))
