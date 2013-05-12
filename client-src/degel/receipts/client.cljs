(ns degel.receipts.client
  (:require-macros [hiccups.core :refer [html]])
  (:require [domina :refer [add-class! append! attr by-class by-id destroy!
                            remove-class! set-html! set-inner-html! set-value! value]]
            [domina.events :refer [listen! prevent-default target]]
            [hiccups.runtime] ;; Needed by hiccups.core macros
            [shoreleave.remotes.http-rpc :refer [remote-callback]]
            [degel.receipts.static-validators :refer [validate-receipt-fields]]
            [degel.receipts.html :refer [entry-html confirmation-html setup-html history-html]]))


(declare submit-receipt add-help remove-help cache-password refresh-history)


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
  (set-value! (by-id "Password") (.getItem storage :password))
  (listen! (by-id "submit-pwd") :click cache-password)
  (listen! (by-id "refresh-history") :click refresh-history))


(defn show-new-receipt [prefill-with]
  (set-html! (by-id "receipt-body") (entry-html))
  (fill-defaults)
  (when-let [paid-by (:paid-by prefill-with)]
    (set-value! (by-id "PaidBy") paid-by))
  (when-let [date (:date prefill-with)]
    (set-value! (by-id "Date") date))
  (when-let [amount (:amount prefill-with)]
    (set-value! (by-id "Amount") amount))
  (when-let [category (:category prefill-with)]
    (set-value! (by-id "Category") category))
  (when-let [vendor (:vendor prefill-with)]
    (set-value! (by-id "Vendor") vendor))
  (when-let [comments (:comments prefill-with)]
    (set-value! (by-id "Comments") comments))
  (when-let [for-whom (:for-whom prefill-with)]
    (set-value! (by-id "ForWhom") for-whom))
  (let [submit-btn (by-id "submit-receipt")]
    (listen! submit-btn :click submit-receipt)
    (listen! submit-btn :mouseover add-help)
    (listen! submit-btn :mouseout remove-help)))


(defn set-tab [e]
  ;; [TODO] This would be a lot cleaner if (1) I knew how to extract the id of a node and
  ;; (2) how to iterate over the children of a node.
  (let [tab (.-parentNode (target e))
        navbar (by-class "navbar")
        receipt-tab (by-id "receipt-tab")
        setup-tab (by-id "setup-tab")
        history-tab (by-id "history-tab")]
    (remove-class! receipt-tab "active")
    (remove-class! setup-tab "active")
    (remove-class! history-tab "active")
    (add-class! tab "active")
    (condp = tab
      receipt-tab (show-new-receipt {})
      setup-tab (set-html! (by-id "receipt-body") (setup-html))
      history-tab (set-html! (by-id "receipt-body") (history-html)))
    (fill-defaults)))


(defn submit-receipt []
  (let [params-map {:paid-by  (-> "PaidBy" by-id value)
                    :date     (-> "Date" by-id value)
                    :amount   (-> "Amount" by-id value)
                    :category (-> "Category" by-id value)
                    :vendor   (-> "Vendor" by-id value)
                    :comments (-> "Comments" by-id value)
                    :for-whom (-> "ForWhom" by-id value)
                    :password (.getItem storage :password)}]
    (remote-callback :enter-receipt [params-map]
                     (fn [[success confirmation]]
                       (set-html! (by-id "receipt-body")
                                  (confirmation-html success confirmation))
                       (listen! (by-id "next-receipt") :click
                                #(show-new-receipt (if success {} params-map)))))))


(defn refresh-history []
  (let [password (.getItem storage :password)]
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
      (append! (by-id "receipt-body") (html [:div.help message])))))


(defn add-help []
  (append! (by-id "receipt-body")
             (html [:div.help "Click here to submit receipt"])))


(defn remove-help []
  (destroy! (by-class "help")))

(defn cache-password []
  (let [password (-> "Password" by-id value)]
    (.setItem storage :password password)))


(defn ^:export init []
  (when (and js/document
             (aget js/document "getElementById"))
    (let [navbar (by-class "navbar") ]
      (show-new-receipt {})
      (fill-defaults)
      (listen! navbar :click set-tab))))
