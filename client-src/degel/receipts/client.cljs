(ns degel.receipts.client
  (:require-macros [hiccups.core :refer [html]])
  (:require [clojure.string :refer [blank?]]
            [domina :as dom]
            [domina.events :as events]
            [hiccups.runtime] ;; Needed by hiccups.core macros
            [shoreleave.remotes.http-rpc :refer [remote-callback]]
            [degel.receipts.static-validators :refer [validate-receipt-fields]]
            [degel.receipts.utils :refer [storage now-string]]
            [degel.receipts.html :refer [entry-html confirmation-html setup-html history-html
                                         button-group set-active-button]]))


(declare submit-receipt add-help remove-help cache-user-data refresh-history fill-defaults)

(defn page-to-storage
  "Save webpage control values to persistent storage."
  []
  (when (dom/by-id "PaidBy")
    (.setItem storage :cntl-paid-by (dom/value (dom/by-id "PaidBy")))
    (.setItem storage :cntl-date (dom/value (dom/by-id "Date")))
    (.setItem storage :cntl-amount (dom/value (dom/by-id "Amount")))
    (.setItem storage :cntl-category (dom/value (dom/by-id "Category")))
    (.setItem storage :cntl-vendor (dom/value (dom/by-id "Vendor")))
    (.setItem storage :cntl-comment (dom/value (dom/by-id "Comment")))
    (.setItem storage :cntl-for-whom (dom/value (dom/by-id "ForWhom"))))
  (when (dom/by-id "Password")
    (.setItem storage :cntl-user-id (dom/value (dom/by-id "user-id")))
    (.setItem storage :cntl-password (dom/value (dom/by-id "Password")))))


(defn storage-to-page
  "Load webpage control values from persistent storage."
  []
  (when (dom/by-id "PaidBy")
    ;; [TODO] Now handled in set-tab below. Clean up, once we get the data callback scheme working.
    ;; (dom/set-value! (dom/by-id "PaidBy") (.getItem storage :cntl-paid-by))
    (dom/set-value! (dom/by-id "Date") (.getItem storage :cntl-date))
    (dom/set-value! (dom/by-id "Amount") (.getItem storage :cntl-amount))
    (dom/set-value! (dom/by-id "Category") (.getItem storage :cntl-category))
    (dom/set-value! (dom/by-id "Vendor") (.getItem storage :cntl-vendor))
    (dom/set-value! (dom/by-id "Comment") (.getItem storage :cntl-comment))
    (dom/set-value! (dom/by-id "ForWhom") (.getItem storage :cntl-for-whom)))
  (when (dom/by-id "Password")
    (let [pwd-from-page (.getItem storage :cntl-password)
          cached-pwd (.getItem storage :password)]
      (dom/set-value! (dom/by-id "Password") (if (blank? pwd-from-page) cached-pwd pwd-from-page)))
    (let [uid-from-page (.getItem storage :cntl-user-id)
          cached-uid (.getItem storage :user-id)]
      (dom/set-value! (dom/by-id "user-id") (if (blank? uid-from-page) cached-uid uid-from-page)))))


(defn clear-receipt-page []
  (dom/set-value! (dom/by-id "PaidBy") "")
  (dom/set-value! (dom/by-id "Date") "")
  (dom/set-value! (dom/by-id "Amount") "")
  (dom/set-value! (dom/by-id "Category") "")
  (dom/set-value! (dom/by-id "Vendor") "")
  (dom/set-value! (dom/by-id "Comment") "")
  (dom/set-value! (dom/by-id "ForWhom") "")
  (fill-defaults)
  (page-to-storage))


(defn set-tab [tab]
  (set-active-button "tabbar-buttons" tab)
  (page-to-storage)
  (condp = tab
    "receipt-tab" (do
                    (dom/set-html! (dom/by-id "contents") (entry-html))
                    (remote-callback :fill-paid-by [:israel]
                      #(dom/set-inner-html! (dom/by-id "PaidBy")
                         (let [selected-paid-by (.getItem storage :cntl-paid-by)]
                           (html [:select {:name "paidby-choices"}
                                  (for [paid-by %] [:option
                                                    (if (= selected-paid-by paid-by)
                                                      {:value paid-by :selected ""}
                                                      {:value paid-by})
                                                    paid-by])]))))
                    (let [submit-btn (dom/by-id "submit-receipt")]
                      (events/listen! submit-btn :click submit-receipt)
                      (events/listen! submit-btn :mouseover add-help)
                      (events/listen! submit-btn :mouseout remove-help)))
    "setup-tab" (do
                  (dom/set-html! (dom/by-id "contents") (setup-html))
                  (events/listen! (dom/by-id "submit-pwd") :click cache-user-data))
    "history-tab" (do
                    (dom/set-html! (dom/by-id "contents") (history-html))
                    (refresh-history)
                    (events/listen! (dom/by-id "refresh-history") :click refresh-history)))
  (storage-to-page))


(defn submit-receipt []
  (let [params-map {:paid-by  (-> "PaidBy" dom/by-id dom/value)
                    :date     (-> "Date" dom/by-id dom/value)
                    :amount   (-> "Amount" dom/by-id dom/value)
                    :category (-> "Category" dom/by-id dom/value)
                    :vendor   (-> "Vendor" dom/by-id dom/value)
                    :comment  (-> "Comment" dom/by-id dom/value)
                    :for-whom (reduce str (-> "ForWhom" dom/by-id dom/value))
                    :user-id (.getItem storage :user-id)
                    :password (.getItem storage :password)}]
    (page-to-storage)
    (remote-callback :enter-receipt [params-map]
      (fn [[success confirmation]]
        (when success
          (clear-receipt-page))
        (dom/set-html! (dom/by-id "contents")
          (confirmation-html success confirmation))
        (events/listen! (dom/by-id "next-receipt") :click
          #(set-tab "receipt-tab"))))))


(defn refresh-history []
  (let [password (.getItem storage :password)]
    (remote-callback :fill-receipt-history [password]
      (fn [records]
        (dom/destroy! (dom/by-class "history"))
        (let [h (html [:div.history
                       (map (fn [r] [:p r]) records)])]
          (dom/append! (dom/by-id "ForHistory") h))))))


(defn verify-not-empty [e]
  (let [target (events/target e)
        message (dom/attr target :title)]
    (when (empty? (dom/value target))
      (dom/append! (dom/by-id "contents") (html [:div.alert message])))))


(defn add-help []
  (dom/append! (dom/by-id "contents")
    (html [:div.help "Click here to submit receipt"])))


(defn remove-help []
  (dom/destroy! (dom/by-class "help")))


(defn cache-user-data []
  (let [password (-> "Password" dom/by-id dom/value)]
    (.setItem storage :password password))
  (let [user-id (-> "user-id" dom/by-id dom/value)]
    (.setItem storage :user-id user-id)))


(defn fill-defaults []
  (dom/set-value! (dom/by-id "Date") (now-string))
  (dom/set-value! (dom/by-id "user-id") (.getItem storage :user-id))
  (dom/set-value! (dom/by-id "Password") (.getItem storage :password)))


(defn ^:export init []
  (set-tab "receipt-tab")
  (dom/set-html! (dom/by-id "tabbar")
    (html (button-group "tabbar-buttons" true
            [{:id "receipt-tab" :text "Receipt"}
             {:id "setup-tab" :text "Setup"}
             {:id "history-tab" :text "History"}])))
  (events/listen! (dom/by-id "tabbar") :click
    #(-> % events/target (. -id) set-tab))
  (set-active-button "tabbar-buttons" "receipt-tab")
  (fill-defaults))
