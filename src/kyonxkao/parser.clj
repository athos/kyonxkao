(ns kyonxkao.parser
  (:require [instaparse.core :as p]
            [clojure.string :as s]
            [clojure.java.io :as io]))

(def ^:private wordlists
  #{"character" "first_person" "second_person"
    "positive_noun" "positive_adjective" "neutral_adjective"
    "negative_noun" "negative_adjective"})

(defn load-wordlist [wordlist]
  (with-open [r (io/reader (str "resources/wordlist/" wordlist ".wordlist"))]
    (let [format (if (= wordlist "character")
                   #(str "'" % "'")
                   #(str "<'" % "'>"))]
      (str (s/join " | " (map format (line-seq r))) \newline))))

(def ^:private parser
  (let [replace #(s/replace %1 (str "{{" (s/upper-case %2) "}}") (load-wordlist %2))
        rule (reduce replace (slurp "resources/grammer.ebnf") wordlists)]
    (p/parser rule)))

(defn parse [source]
  (-> source
      (s/replace #"\n#.*?\n" "")
      (s/replace #"[\r\n]" "")
      parser))
