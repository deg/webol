(ns degel.receipts.receipts
  (:require [clojure.string :refer [split]]
            [clojure.pprint :refer [cl-format]]))

(defn month-string [month]
  (let [month-val (Integer/parseInt month)]
    (if (and (integer? month-val) (<= 1 month-val 12))
      (["Jan" "Feb" "Mar" "Apr" "May" "Jun" "Jul" "Aug" "Sep" "Oct" "Nov" "Dec"] (dec month-val))
      "???")))

(defn format-receipt [paid-by date amount category vendor comments for-whom]
  (println paid-by date amount vendor comments for-whom)
  (let [[year month day] (split date #"-")]
    (cl-format false "~A;~D~A~D;~A;~A;~A;~A;~A"
               paid-by
               day (month-string month) year
               amount category vendor comments for-whom)))

(defn enter-receipt [paid-by date amount category vendor comments for-whom]
  (str "GOT: " (format-receipt paid-by date amount category vendor comments for-whom)))

