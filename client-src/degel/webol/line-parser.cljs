(ns degel.webol.line-parser
  (:require [clojure.string :as str]
            [domina :as dom :refer [log]]
            [degel.webol.store :as store]))


(defn tokenize-cmd [line]
  (let [[cmd rest] (str/split line #"\s+" 2)
        line-num (re-find #"^\d+$" cmd)]
    (if line-num
      {:line-number (cljs.reader/read-string line-num) :line-body rest}
      {:cmd (keyword (str/upper-case cmd)) :cmd-body rest})))

(defn parse [line]
  (let [line-map (tokenize-cmd line)]
    (if (:line-number line-map)
      line-map
      (condp = (:cmd line-map)
        :PRINT
        (let [text (:cmd-body line-map)]
          (assoc line-map :arg1 (if (empty? text) "" (cljs.reader/read-string text))))

        line-map))))
