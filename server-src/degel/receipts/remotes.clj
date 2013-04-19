(ns degel.receipts.remotes
  (:require [clojure.string :as str]
            [clojure.pprint :refer [cl-format]]
            [compojure.handler :refer [site]]
            [shoreleave.middleware.rpc :refer [defremote wrap-rpc]]
            [degel.receipts.server :refer [handler]]))

(defn month-string [month]
  (let [month-val (Integer/parseInt month)]
    (if (and (integer? month-val) (<= 1 month-val 12))
      (["Jan" "Feb" "Mar" "Apr" "May" "Jun" "Jul" "Aug" "Sep" "Oct" "Nov" "Dec"] (dec month-val))
      "???")))

(defremote save-receipt [paid-by date amount category vendor comments for-whom]
  (println paid-by date amount vendor comments for-whom)
  (let [[year month day] (str/split date #"-")]
    (cl-format false "~A;~D~A~D;~A;~A;~A;~A;~A"
               paid-by
               day (month-string month) year
               amount category vendor comments for-whom)))

(def app (-> (var handler)
             (wrap-rpc)
             (site)))

