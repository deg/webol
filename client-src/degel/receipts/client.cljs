(ns degel.receipts.client
  (:require-macros [hiccups.core :refer [html]])
  (:require [domina :refer [append! attr by-class by-id destroy! set-value! value]]
            [domina.events :refer [listen! prevent-default target]]
            [hiccups.runtime] ;; Needed by hiccups.core macros
            [shoreleave.remotes.http-rpc :refer [remote-callback]]
            [degel.receipts.static-validators :refer [validate-receipt-fields]]))

(defn log [s]
  (.log js/console s))

(def storage (.-localStorage js/window))

(defn fill-defaults []
  (remote-callback :fill-paid-by [:israel]
                   #(append! (by-id "PaidBy")
                             (html [:datalist {:id "PaymentDevices"}
                                    (for [x %] [:option {:value x}])])))
  (let [date (js/Date.)
        day (.getDate date)
        month (inc (.getMonth date))]
    (set-value! (by-id "Date")
                (str (.getFullYear date) "-"
                     (if (< month 10) "0" "") month "-"
                     (if (< day 10) "0" "") day)))

  (set-value! (by-id "Password") (.getItem storage :password)))


(defn refresh-history []
  (let [password (value (by-id "Password"))]
    (remote-callback :fill-receipt-history [password]
                     (fn [records]
                       (destroy! (by-class "history"))
                       (let [h (html [:div.history
                                      (map (fn [r] [:p r]) records)])]
                         (append! (by-id "ForHistory") h))))))


(defn verify-not-empty [e]
  (let [target (target e)
        message (attr target :title)]
    (when (empty? (value target))
      (append! (by-id "newReceipt") (html [:div.help message])))))


(defn add-help []
  #_
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

(defn cache-password []
  (let [password (-> "Password" by-id value)]
    (.setItem storage :password password)))


(defn ^:export init []
  (when (and js/document
             (aget js/document "getElementById"))
    (let [form (by-id "submit")
          password-btn (by-id "submit-pwd")
          history-btn (by-id "refresh-history")
          paid-by (by-id "PaidBy")
          amount (by-id "Amount")
          date (by-id "Date")]
      (fill-defaults)
      ;(listen! paid-by :focus fill-paid-by)
      ;(listen! amount :blur verify-not-empty)
      (listen! password-btn :click cache-password)
      (listen! history-btn :click refresh-history)
      (listen! form :mouseover add-help)
      (listen! form :mouseout remove-help))))
