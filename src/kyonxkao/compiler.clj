(ns kyonxkao.compiler
  (:refer-clojure :exclude [compile])
  (:require [clojure.core.match :refer [match]]))

(defn map-act&scene [f acts]
  (for [act acts
        :let [[_ act-no & scenes] act]
        scene scenes
        :let [[_ scene-no & contents] scene]]
    (f [(Long/parseLong act-no) (Long/parseLong scene-no)] contents)))

(defn make-act&scene->section [acts]
  (let [m (->> (map-act&scene (fn [x y] x) acts)
               (map-indexed #(vector %2 %1))
               (group-by (comp first first)))]
    (->> (for [[k v] m]
           [k (reduce (fn [m [act&scene section]]
                        (assoc m (second act&scene) section))
                      (sorted-map)
                      v)])
         (into {}))))

(defn the-other [stage character]
  (val (first (dissoc stage character))))

(defn constant->number [c]
  (letfn [(power-of-2 [n sign] (nth (iterate #(* 2 %) sign) n))]
    (match c
      [:PositiveConstant & cs] (power-of-2 (dec (count cs)) 1)
      [:NegativeConstant & cs] (power-of-2 (dec (count cs)) -1))))

(defn not-implemented [cause]
  (throw (ex-info "not implemented" {:cause cause})))

(defn compile-line [character act-no scene-no stage sentences]
  (let [the-other (the-other @stage character)]
    (letfn [(compile-sentence [insts sentence]
              (match sentence
                [_ [:InOut [:SpeakYourMind]]] (conj insts [:pr the-other])
                [_ [:InOut [:OpenYourMind]]] (conj insts [:rd the-other])
                [_ [:Jump & _]] (not-implemented sentence)
                [_ [:Question & _]] (not-implemented sentence)
                [_ [:Recall & _]] (not-implemented sentence)
                [_ [:Statement [:Equality adjective]]] (not-implemented sentence)
                [_ [:Statement constant]] (let [c (constant->number constant)]
                                            (conj insts [:mov the-other c]))
                [_ [:Conditional [:IfSo]] sentence'] (not-implemented sentence)
                [_ [:Conditional [:IfNot]] sentence'] (not-implemented sentence)))]
      (reduce compile-sentence [] sentences))))

(defn compile-scene [characters act-no scene-no stage contents]
  (letfn
      [(compile-content [insts content]
         (match content
           [:Enter & cs] (do (apply swap! stage conj (for [c cs] [c (characters c)]))
                             insts)
           [:Exit & cs] (do (apply swap! stage dissoc cs)
                            insts)
           [:Exeunt] (do (swap! stage empty)
                         insts)
           [:Line c & sentences] (->> (compile-line c act-no scene-no stage sentences)
                                      (into insts))))]
    (reduce compile-content [] contents)))

(defn compile-acts [characters acts]
  (let [act&scene->section (make-act&scene->section acts)
        stage (atom {})]
    (->> (map-act&scene (fn [[act-no scene-no] contents]
                          [((act&scene->section act-no) scene-no)
                           (compile-scene characters act-no scene-no stage contents)])
                        acts)
         (into {}))))

(defn compile [play]
  (let [[_ _ [_ & characters] & acts] play
        characters (->> characters
                        (map-indexed (fn [i x] [x (symbol (str '$ i))]))
                        (into {}))]
    (compile-acts characters acts)))
