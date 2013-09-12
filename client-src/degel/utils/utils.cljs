(ns degel.utils.utils
  (:require
   ;; [TODO] {FogBugz:141} Is clojure.edn available in cljs? Is read-string a security hole?
   [cljs.reader :refer [read-string]]))


(defn now-string []
  (let [date (js/Date.)
        day (.getDate date)
        month (inc (.getMonth date))]
    (str (.getFullYear date) "-"
         (if (< month 10) "0" "") month "-"
         (if (< day 10) "0" "") day)))


(defn read-string-or-nil
  "Read a string or nil, or return clojure value."
  [str]
  (if (empty? str)
    nil
    (read-string str)))


(defn debug [x]
   (js* "debugger;")
   x)
