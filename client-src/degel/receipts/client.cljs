(ns degel.receipts.client
  (:require-macros [hiccups.core :refer [html]])
  (:require [clojure.string :refer [blank?]]
            [domina :as dom :refer [log]]
            [domina.events :as events]
            [hiccups.runtime] ;; Needed by hiccups.core macros
            [shoreleave.remotes.http-rpc :refer [remote-callback]]
            [degel.receipts.static-validators :refer [validate-receipt-fields]]
            [degel.receipts.utils :refer [now-string]]
            [degel.receipts.storage :refer [read write-local]]
            [degel.receipts.html :refer [entry-html confirmation-html setup-html history-html
                                         button-group set-active-button]]))


(declare submit-receipt add-help remove-help cache-user-data refresh-history fill-defaults)


(defn- clj-value [id]
  (-> id dom/by-id dom/value js->clj))

(defn- set-clj-value! [id value]
  (dom/set-value! (dom/by-id id) (clj->js value)))


(defn page-to-storage
  "Save webpage control values to persistent storage."
  []
  (let [pairs (cond (dom/by-id "PaidBy")   [["PaidBy" :cntl-paid-by]
                                            ["Date" :cntl-date]
                                            ["Amount" :cntl-amount]
                                            ["Category" :cntl-category]
                                            ["Vendor" :cntl-vendor]
                                            ["Comment" :cntl-comment]
                                            ["ForWhom" :cntl-for-whom]]
                    (dom/by-id "Password") [["user-id" :cntl-user-id]
                                            ["Password" :cntl-password]])]
    (doseq [[id key] pairs]
      (write-local key (clj-value id)))))


(defn storage-to-page
  "Load webpage control values from persistent storage."
  []
  (let [storage-to-control (fn [key id] (read key #(set-clj-value! id %)))]
    (when (dom/by-id "PaidBy")
      ;; [TODO] Now handled in set-tab below. Clean up, once we get the data callback scheme working.
      ;; (dom/set-value! (dom/by-id "PaidBy") (.getItem storage :cntl-paid-by))
      (doseq [[key id] [[:cntl-date "Date"]
                        [:cntl-amount "Amount"]
                        [:cntl-category "Category"]
                        [:cntl-vendor "Vendor"]
                        [:cntl-comment "Comment"]
                        [:cntl-for-whom "ForWhom"]]]
        (storage-to-control key id)))
    (when (dom/by-id "Password")
      (doseq [[key id] [[:cntl-password "Password"]
                        [:cntl-user-id "user-id"]]]
        (storage-to-control key id)))))


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
  (page-to-storage)
  (condp = tab
    "receipt-tab" (do
                    (dom/set-html! (dom/by-id "contents") (entry-html))
                    (remote-callback :fill-paid-by [:israel]
                      #(dom/set-inner-html! (dom/by-id "PaidBy")
                         (let [selected-paid-by (read :cntl-paid-by nil)]
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
                  (dom/set-html! (dom/by-id "contents") (setup-html)))
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
                    :user-id (read :cntl-user-id nil)
                    :password (read :cntl-password nil)}]
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
  (let [password (read :cntl-password nil)]
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


(defn fill-defaults []
  (dom/set-value! (dom/by-id "Date") (now-string)))


(defn ^:export init []
  (log "Init 0")
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
