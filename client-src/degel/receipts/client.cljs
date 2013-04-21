(ns degel.receipts.client
  (:require-macros [hiccups.core :refer [html]])
  (:require [domina :refer [append! attr by-class by-id destroy! set-value! value]]
            [domina.events :refer [listen! prevent-default target]]
            [hiccups.runtime] ;; Needed by hiccups.core macros
            ;[shoreleave.remotes.http-rpc :refer [remote-callback]]


;;; ;; define the function to be attached to form submission event
;;; (defn submit-form [event]
;;;   (let [paid-by (value (by-id "PaidBy"))
;;;         date (value (by-id "Date"))
;;;         amount (value (by-id "Amount"))
;;;         category (value (by-id "Category"))
;;;         vendor (value (by-id "Vendor"))
;;;         comments (value (by-id "Comments"))
;;;         for-whom (value (by-id "ForWhom"))]
;;;     (if (or (empty? paid-by) (empty? date) (empty? category) (empty? vendor))
;;;       (do (prevent-default event)
;;;           (js/alert "Missing field"))
;;;       (remote-callback :save-receipt [paid-by date amount category vendor comments for-whom]
;;;                        #(append! (by-id "newReceipt")
;;;                                  (html [:div.result %]))))))

(defn verify-not-empty [e]
  (let [target (target e)
        message (attr target :title)]
    (when (empty? (value e))
      (append! (by-id "newReceipt") (html [:div.help message])))))


(defn add-help []
  (append! (by-id "newReceipt")
           (html [:div.help "Click here to submit receipt"])))


(defn remove-help []
  (destroy! (by-class "help")))


(defn ^:export init []
  (when (and js/document
             (aget js/document "getElementById"))
    (let [form (by-id "submit")
          paid-by (by-id "PaidBy")
          amount (by-id "Amount")
          date (by-id "Date")]
;      (listen! form :click submit-form)
      (listen! form :mouseover add-help)
      (listen! form :mouseout remove-help)
      (listen! paid-by :blur verify-not-empty)
      (listen! date :blur verify-not-empty)
      (listen! amount :blur verify-not-empty)
      (listen! date :mouseover add-help)
      (listen! date :mouseout remove-help))))
