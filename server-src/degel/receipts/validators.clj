(ns degel.receipts.validators
  (:require [valip.core :refer [validate]]
            [valip.predicates :refer [present? matches email-address?]]))

(defn validate-receipt-fields [paid-by date amount category vendor comments for-whom]
  (validate
   {:paid-by paid-by
    :date date
    :amount amount
    :category category
    :vendor vendor
    :comments comments
    :for-whom for-whom}
   [:paid-by present? "paid-by can't be empty."]
   [:date present? "date can't be empty."]
   [:amount present? "amount can't be empty."]
   [:category present? "category can't be empty."]
   [:vendor present? "vendor can't be empty."]
   [:comments present? "comments can't be empty."]
   [:for-whom present? "for-whom can't be empty."]))
