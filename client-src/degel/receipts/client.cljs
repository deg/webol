(ns degel.receipts.client
  (:require-macros [hiccups.core :refer [html]])
  (:require [domina :refer [append! attr by-class by-id destroy! set-value! value]]
            [domina.events :refer [listen! prevent-default target]]
            [hiccups.runtime] ;; Needed by hiccups.core macros
            [shoreleave.remotes.http-rpc :refer [remote-callback]]
            [degel.receipts.static-validators :refer [validate-receipt-fields]]))

(defn fill-paid-by []
  (remote-callback :fill-paid-by [:israel] #(set-value! (by-id "PaidBy") %)))


(defn verify-not-empty [e]
  (let [target (target e)
        message (attr target :title)]
    (when (empty? (value e))
      (append! (by-id "newReceipt") (html [:div.help message])))))


(defn add-help []
  (let [errors (validate-receipt-fields
                (value (by-id "PaidBy"))
                (value (by-id "Date"))
                (value (by-id "Amount"))
                (value (by-id "Category"))
                (value (by-id "Vendor"))
                (value (by-id "Comments"))
                (value (by-id "ForWhom")))]
    (append! (by-id "newReceipt")
             (html [:div.help (str errors "Click here to submit receipt")]))))


(defn remove-help []
  (destroy! (by-class "help")))


(defn ^:export init []
  (when (and js/document
             (aget js/document "getElementById"))
    (let [form (by-id "submit")
          paid-by (by-id "PaidBy")
          amount (by-id "Amount")
          date (by-id "Date")]
      (fill-paid-by)
      ;(listen! paid-by :focus fill-paid-by)
      (listen! form :mouseover add-help)
      (listen! form :mouseout remove-help)
      (listen! paid-by :blur verify-not-empty)
      (listen! date :blur verify-not-empty)
      (listen! amount :blur verify-not-empty)
      (listen! date :mouseover add-help)
      (listen! date :mouseout remove-help))))
