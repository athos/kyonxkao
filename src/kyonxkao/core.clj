(ns kyonxkao.core)

(defn parse-title [lines]
  )

(defn parse-characters [lines]
  )

(defn parse-line [lines]
  )

(defn parse-scene [lines]
  )

(defn parse-scenes [lines]
  )

(defn parse-acts [lines]
  )

(defn parse-program [lines]
  (let [[_ lines] (parse-title lines)
        [characters lines] (parse-characters lines)
        [acts lines] (parse-acts lines)]
    [characters acts]))
