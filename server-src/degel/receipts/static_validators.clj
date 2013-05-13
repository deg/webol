(ns degel.receipts.static-validators
  (:require [valip.core :refer [validate]]
            [valip.predicates :refer [present? matches email-address?]]))

(defn validate-receipt-fields [paid-by date amount category vendor comment for-whom]
  (validate
   {:paid-by paid-by
    :date date
    :amount amount
    :category category
    :vendor vendor
    :comment comment
    :for-whom for-whom}
   [:paid-by present? "paid-by can't be empty."]
   [:date present? "date can't be empty."]
   [:amount present? "amount can't be empty."]
   [:category present? "category can't be empty."]
   [:vendor present? "vendor can't be empty."]
   #_[:comment present? "comment can't be empty."]
   #_[:for-whom present? "for-whom can't be empty."]
   ))
