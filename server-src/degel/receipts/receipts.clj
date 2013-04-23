(ns degel.receipts.receipts
  (:require [clojure.string :refer [split]]
            [clojure.pprint :refer [cl-format]]
            [degel.receipts.db :refer [put-record]]
            [degel.receipts.static-validators :refer [validate-receipt-fields]]))

(defn month-string [month]
  (let [month-val (Integer/parseInt month)]
    (if (and (integer? month-val) (<= 1 month-val 12))
      (["Jan" "Feb" "Mar" "Apr" "May" "Jun" "Jul" "Aug" "Sep" "Oct" "Nov" "Dec"] (dec month-val))
      "???")))

(defn format-receipt [{:keys [paid-by date amount category vendor comments for-whom]}]
  (println paid-by date amount vendor comments for-whom)
  (let [[year month day] (split date #"-")]
    (cl-format false "~A;~D~A~D;~A;~A;~A;~A;~A"
               paid-by
               day (month-string month) year
               amount category vendor comments for-whom)))


(defn enter-receipt [{:keys [paid-by date amount category vendor comments for-whom password]
                      :as columns}]
  (let [errors (validate-receipt-fields paid-by date amount category vendor comments for-whom)]
    (if (empty? errors)
      (let [formatted (format-receipt columns)]
        (println "COLUMNS 2: " columns)
        (put-record (assoc columns :formatted formatted))
        formatted)
      (cl-format false "Something didn't validate: ~{~A ~}" (mapcat second errors)))))

