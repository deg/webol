(ns degel.receipts.client
  (:require-macros [hiccups.core :refer [html]])
  (:require [clojure.string :refer [blank?]]
            [domina :refer [add-class! append! attr by-class by-id destroy! log
                            remove-class! set-html! set-inner-html! set-value! value]]
            [domina.events :refer [listen! prevent-default target]]
            [hiccups.runtime] ;; Needed by hiccups.core macros
            [shoreleave.remotes.http-rpc :refer [remote-callback]]
            [degel.receipts.static-validators :refer [validate-receipt-fields]]
            [degel.receipts.utils :refer [storage now-string]]
            [degel.receipts.html :refer [entry-html confirmation-html setup-html history-html]]))


(declare submit-receipt add-help remove-help cache-user-data refresh-history fill-defaults)

(defn page-to-storage
  "Save webpage control values to persistent storage."
  []
  (when (by-id "PaidBy")
    (.setItem storage :cntl-paid-by (value (by-id "PaidBy")))
    (.setItem storage :cntl-date (value (by-id "Date")))
    (.setItem storage :cntl-amount (value (by-id "Amount")))
    (.setItem storage :cntl-category (value (by-id "Category")))
    (.setItem storage :cntl-vendor (value (by-id "Vendor")))
    (.setItem storage :cntl-comment (value (by-id "Comment")))
    (.setItem storage :cntl-for-whom (value (by-id "ForWhom"))))
  (when (by-id "Password")
    (.setItem storage :cntl-user-id (value (by-id "user-id")))
    (.setItem storage :cntl-password (value (by-id "Password")))))


(defn storage-to-page
  "Load webpage control values from persistent storage."
  []
  (when (by-id "PaidBy")
    (set-value! (by-id "PaidBy") (.getItem storage :cntl-paid-by))
    (set-value! (by-id "Date") (.getItem storage :cntl-date))
    (set-value! (by-id "Amount") (.getItem storage :cntl-amount))
    (set-value! (by-id "Category") (.getItem storage :cntl-category))
    (set-value! (by-id "Vendor") (.getItem storage :cntl-vendor))
    (set-value! (by-id "Comment") (.getItem storage :cntl-comment))
    (set-value! (by-id "ForWhom") (.getItem storage :cntl-for-whom)))
  (when (by-id "Password")
    (let [pwd-from-page (.getItem storage :cntl-password)
          cached-pwd (.getItem storage :password)]
      (set-value! (by-id "Password") (if (blank? pwd-from-page) cached-pwd pwd-from-page)))
    (let [uid-from-page (.getItem storage :cntl-user-id)
          cached-uid (.getItem storage :user-id)]
      (set-value! (by-id "user-id") (if (blank? uid-from-page) cached-uid uid-from-page)))))


(defn clear-receipt-page []
  (set-value! (by-id "PaidBy") "")
  (set-value! (by-id "Date") "")
  (set-value! (by-id "Amount") "")
  (set-value! (by-id "Category") "")
  (set-value! (by-id "Vendor") "")
  (set-value! (by-id "Comment") "")
  (set-value! (by-id "ForWhom") "")
  (fill-defaults)
  (page-to-storage))


(defn set-tab [tab]
  ;; [TODO] This would be a lot cleaner if (1) I knew how to extract the id of a node and
  ;; (2) how to iterate over the children of a node.
  (let [receipt-tab (by-id "receipt-tab")
        setup-tab (by-id "setup-tab")
        history-tab (by-id "history-tab")]
    (remove-class! receipt-tab "active")
    (remove-class! setup-tab "active")
    (remove-class! history-tab "active")
    (add-class! tab "active")
    (page-to-storage)
    (condp = tab
      receipt-tab (do
                    (set-html! (by-id "contents") (entry-html))
                    (let [submit-btn (by-id "submit-receipt")]
                      (listen! submit-btn :click submit-receipt)
                      (listen! submit-btn :mouseover add-help)
                      (listen! submit-btn :mouseout remove-help)))
      setup-tab   (do
                    (set-html! (by-id "contents") (setup-html))
                    (listen! (by-id "submit-pwd") :click cache-user-data))
      history-tab (do
                    (set-html! (by-id "contents") (history-html))
                    (refresh-history)
                    (listen! (by-id "refresh-history") :click refresh-history)))
    (storage-to-page)))


(defn submit-receipt []
  (let [params-map {:paid-by  (-> "PaidBy" by-id value)
                    :date     (-> "Date" by-id value)
                    :amount   (-> "Amount" by-id value)
                    :category (-> "Category" by-id value)
                    :vendor   (-> "Vendor" by-id value)
                    :comment  (-> "Comment" by-id value)
                    :for-whom (-> "ForWhom" by-id value)
                    :user-id (.getItem storage :user-id)
                    :password (.getItem storage :password)}]
    (page-to-storage)
    (remote-callback :enter-receipt [params-map]
                     (fn [[success confirmation]]
                       (when success
                         (clear-receipt-page))
                       (set-html! (by-id "contents")
                                  (confirmation-html success confirmation))
                       (listen! (by-id "next-receipt") :click
                                #(set-tab (by-id "receipt-tab")))))))


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
      (append! (by-id "contents") (html [:div.alert message])))))


(defn add-help []
  (append! (by-id "contents")
             (html [:div.help "Click here to submit receipt"])))


(defn remove-help []
  (destroy! (by-class "help")))


(defn cache-user-data []
  (let [password (-> "Password" by-id value)]
    (.setItem storage :password password))
  (let [user-id (-> "user-id" by-id value)]
    (.setItem storage :user-id user-id)))


(defn fill-defaults []
  (remote-callback :fill-paid-by [:israel]
                   #(append! (by-id "PaidBy")
                             (html [:datalist {:id "PaymentDevices"}
                                    (for [x %] [:option {:value x}])])))
  (set-value! (by-id "Date") (now-string))
  (set-value! (by-id "user-id") (.getItem storage :user-id))
  (set-value! (by-id "Password") (.getItem storage :password)))


(defn ^:export init []
  (listen! (by-id "tabbar-menu"):click #(set-tab (target %)))
  (set-tab (by-id "receipt-tab"))
  (fill-defaults))
