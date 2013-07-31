(ns degel.utils.utils
  (:require
   ;; [TODO] Is clojure.edn available in cljs?
   [cljs.reader :refer [read-string]]))

(defn now-string []
  (let [date (js/Date.)
        day (.getDate date)
        month (inc (.getMonth date))]
    (str (.getFullYear date) "-"
         (if (< month 10) "0" "") month "-"
         (if (< day 10) "0" "") day)))

(defn safe-read-string
  "Read a string or nil, or return clojure value."
  [str]
  (if (empty? str)
    nil
    (read-string str)))
