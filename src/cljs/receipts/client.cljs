(ns degel.receipts.cljs.client
  (:require-macros [hiccups.core :as h])
  (:require [domina :as dom]
            [hiccups.runtime :as hiccupsrt]
            [domina.events :as ev]
            [shoreleave.remotes.http-rpc :refer [remote-callback]]
            [cljs.reader :refer [read-string]]))

;; define the function to be attached to form submission event
(defn validate-form []
  (let [paid-by (dom/value (dom/by-id "PaidBy"))
        date (dom/value (dom/by-id "Date"))
        amount (dom/value (dom/by-id "Amount"))
        category (dom/value (dom/by-id "Category"))
        vendor (dom/value (dom/by-id "Vendor"))
        comments (dom/value (dom/by-id "Comments"))
        for-whom (dom/value (dom/by-id "ForWhom"))]
    (dom/set-value! (dom/by-id "ForWhom") "DASH")
    (remote-callback :save-receipt [paid-by date amount category vendor comments for-whom]
                     #(dom/append! (dom/by-id "newReceipt")
                                   (h/html [:div.result %])))))

(defn add-help []
  (dom/append! (dom/by-id "newReceipt")
               (h/html [:div.help "Help me please"])))

(defn remove-help []
  (dom/destroy! (dom/by-class "help")))

;; define the function to attach validate-form to onsubmit event of
;; the form
(defn ^:export init []
  ;; verify that js/document exists and that it has a getElementById
  ;; property
  (when (and js/document
             (aget js/document "getElementById"))
    (ev/listen! (dom/by-id "submit") :click validate-form)
    (ev/listen! (dom/by-id "submit") :mouseover add-help)
    (ev/listen! (dom/by-id "submit") :mouseout remove-help)
    (ev/listen! (dom/by-id "Date") :mouseover add-help)
    (ev/listen! (dom/by-id "Date") :mouseout remove-help)))
