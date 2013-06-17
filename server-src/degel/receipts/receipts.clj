(ns degel.receipts.receipts
  (:require [clojure.string :refer [split]]
            [clojure.pprint :refer [cl-format]]
            [degel.cljutil.devutils :as dev]
            [degel.receipts.db :as db]
            [degel.receipts.simpleDB :refer [put-record get-all-records]]
            [degel.receipts.static-validators :refer [validate-receipt-fields]]))

(defn month-string [month]
  (let [month-val (Integer/parseInt month)]
    (if (and (integer? month-val) (<= 1 month-val 12))
      (["Jan" "Feb" "Mar" "Apr" "May" "Jun" "Jul" "Aug" "Sep" "Oct" "Nov" "Dec"] (dec month-val))
      "???")))

(defn format-receipt [{:keys [paid-by date amount category vendor comment for-whom]}]
  (let [[year month day] (split date #"-")]
    (cl-format false "~A;~D~A~D;~A;~A;~A;~A;~A"
               paid-by
               day (month-string month) year
               amount category vendor comment for-whom)))


(defn enter-receipt-internal [{:keys [paid-by date amount category vendor comment for-whom
                                      user-id password]
                      :as columns}]
  (let [errors (validate-receipt-fields paid-by date amount category vendor comment for-whom)]
    (if (not (empty? errors))
      {:status db/FAILURE
       :errmsg (cl-format false "Something didn't validate: ~{~A ~}" (mapcat second errors))}
      (let [formatted (format-receipt columns)
            result (put-record (assoc columns :formatted formatted))]
        (if (= db/SUCCESS (:status result))
          (assoc result :formatted formatted)
          result)))))

(defn collect-receipt-history [password]
  (let [records (get-all-records password [:formatted])]
    (remove nil? (map :formatted (:values records)))))
