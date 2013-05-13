(ns degel.receipts.utils)

(defn log [s]
  (.log js/console s))

(def storage (.-localStorage js/window))


(defn now-string []
  (let [date (js/Date.)
        day (.getDate date)
        month (inc (.getMonth date))]
    (str (.getFullYear date) "-"
         (if (< month 10) "0" "") month "-"
         (if (< day 10) "0" "") day)))
