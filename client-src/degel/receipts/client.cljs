(ns degel.receipts.client
  (:require-macros [hiccups.core :refer [html]])
  (:require [domina :refer [append! by-class by-id destroy! set-value! value]]
            [hiccups.runtime] ;; Needed by hiccups.core macros
            [domina.events :refer [listen! prevent-default]]
            [shoreleave.remotes.http-rpc :refer [remote-callback]]
            [cljs.reader :refer [read-string]]))


;; define the function to be attached to form submission event
(defn submit-form [event]
  (let [paid-by (value (by-id "PaidBy"))
        date (value (by-id "Date"))
        amount (value (by-id "Amount"))
        category (value (by-id "Category"))
        vendor (value (by-id "Vendor"))
        comments (value (by-id "Comments"))
        for-whom (value (by-id "ForWhom"))]
    (if (or (empty? paid-by) (empty? date) (empty? category) (empty? vendor))
      (do (prevent-default event)
          (js/alert "Missing field"))
      (remote-callback :save-receipt [paid-by date amount category vendor comments for-whom]
                       #(append! (by-id "newReceipt")
                                 (html [:div.result %])))))
)


(defn add-help []
  (append! (by-id "newReceipt")
           (html [:div.help "Click here to submit receipt"])))


(defn remove-help []
  (destroy! (by-class "help")))


(defn ^:export init []
  (when (and js/document
             (aget js/document "getElementById"))
    (listen! (by-id "submit") :click submit-form)
    (listen! (by-id "submit") :mouseover add-help)
    (listen! (by-id "submit") :mouseout remove-help)
    (listen! (by-id "Date") :mouseover add-help)
    (listen! (by-id "Date") :mouseout remove-help)))
