(ns degel.utils.utils
  (:require
   ;; [TODO] {FogBugz:141} Is clojure.edn available in cljs? Is read-string a security hole?
   [cljs.reader :refer [read-string]]))


(defn date-string
  "Return a simple sortable date string as YYYY-MM-DD, e.g. 2013-09-16"
  [date]
  (let [day (.getDate date)
        month (inc (.getMonth date))]
    (str (.getFullYear date) "-"
         (if (< month 10) "0" "") month "-"
         (if (< day 10) "0" "") day)))


(defn now-string []
  (date-string (js/Date.)))


(defn read-string-or-nil
  "Read a string or nil, or return clojure value."
  [str]
  (if (empty? str)
    nil
    (read-string str)))


(defn debug [x]
   (js* "debugger;")
   x)
