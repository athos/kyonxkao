(ns kyonxkao.vm
  (:refer-clojure :exclude [val])
  (:require [clojure.core.match :as m]))

;;
;; Below are the VM instructions:
;;  [:mov z x]
;;  [:add z x y]
;;  [:sub z x y]
;;  [:mul z x y]
;;  [:div z x y]
;;  [:rem z x y]
;;  [:eq x y]
;;  [:lt x y]
;;  [:gt x y]
;;  [:neg]
;;  [:push z x]
;;  [:pop z]
;;  [:jnz label]
;;  [:jz label]
;;  [:j label]
;;  [:rd z]
;;  [:pr z]
;;  [:rdi z]
;;  [:pri z]
;;

(defn- val [env x]
  (if (number? x)
    x
    (peek (env x))))

(defn- main-loop [section env insts]
  (loop [env env [inst & insts] insts]
    (letfn [(subst [dst x]
              (update-in env [dst] #(conj (pop %) x)))
            (do-op [op dst x y]
              (subst dst (op (val env x) (val env y))))
            (do-cmp [cmp x y]
              (assoc env :flag (cmp (val env x) (val env y))))]
      (if (nil? inst)
        [(inc section) env]
        (m/match inst
          [:mov z x]   (recur (subst z x) insts)
          [:add z x y] (recur (do-op + z x y) insts)
          [:sub z x y] (recur (do-op - z x y) insts)
          [:mul z x y] (recur (do-op * z x y) insts)
          [:div z x y] (recur (do-op quot z x y) insts)
          [:rem z x y] (recur (do-op rem z x y) insts)
          [:eq x y]    (recur (do-cmp = x y) insts)
          [:lt x y]    (recur (do-cmp < x y) insts)
          [:gt x y]    (recur (do-cmp > x y) insts)
          [:neg]       (recur (update-in env [:flag] not) insts)
          [:push z x]  (recur (update-in env [z] #(conj % (val env x))) insts)
          [:pop z]     (recur (update-in env [z] pop) insts)
          [:jnz l]     (if (:flag env)
                         [l env]
                         (recur env insts))
          [:jz l]      (if (:flag env)
                         (recur env insts)
                         [l env])
          [:j l]       [l env]
          [:rd z]      (recur (subst z (.read *in*)) insts)
          [:pr z]      (do (print (char (val env z)))
                           (recur env insts))
          [:rdi z]     (recur (subst z (.read *in*)) insts) ; FIXME: what's the difference between :rd and :rdi?
          [:pri z]     (do (print (val env z))
                           (recur env insts)))))))

(defn run [env program]
  (loop [section 0, env env]
    (if-let [insts (program section)]
      (let [[section' env'] (main-loop section env insts)]
        (recur section' env'))
      env)))
