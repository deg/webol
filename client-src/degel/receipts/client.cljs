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
            [degel.receipts.html :refer [button-group set-active-button selection]]
            [degel.receipts.pages :refer [receipt-tab-controls receipt-tab-html
                                          confirmation-html
                                          setup-tab-controls setup-tab-html
                                          history-tab-html]]
            [degel.receipts.db :as db]))


(declare submit-receipt add-help remove-help cache-user-data refresh-history fill-defaults)


(defn- clj-value [id]
  (-> id dom/by-id (#(when % (dom/value %))) js->clj))

(defn- set-clj-value! [id value]
  (dom/set-value! (dom/by-id id) (clj->js value)))


(defn page-to-storage
  "Save webpage control values to persistent storage."
  []
  (let [controls (cond (dom/by-id "PaidBy")   receipt-tab-controls
                       (dom/by-id "Password") setup-tab-controls)]
    (doseq [[key id] controls]
      (write-local key (clj-value id)))))


(defn storage-to-page
  "Load webpage control values from persistent storage."
  []
  (let [controls (cond (dom/by-id "PaidBy")   receipt-tab-controls
                       (dom/by-id "Password") setup-tab-controls)]
    (doseq [[key id] controls]
      (read key #(set-clj-value! id %)))))


(defn clear-receipt-page []
  (doseq [[_ id] receipt-tab-controls]
    (set-clj-value! id ""))
  (fill-defaults)
  (page-to-storage))


(defn set-tab [tab]
  (page-to-storage)
  (condp = tab
    "receipt-tab" (do
                    (dom/set-html! (dom/by-id "contents") (receipt-tab-html))
                    (read :PaidBy-options
                          (fn [vals _]
                            (dom/set-html! (dom/by-id "PaidBy")
                              (html [:select {:name "paidby-choices"}
                                     (selection (map (fn [x] [x x]) vals)
                                                (or (clj-value "PaidBy") (read :paid-by nil)))]))))
                    (read :ForWhom-options
                          (fn [vals _]
                            (dom/set-html! (dom/by-id "ForWhom")
                              (html [:select {:name "ForWhom-choices"}
                                     (selection vals
                                                (or (clj-value "ForWhom") (read :for-whom nil)))]))))
                    (let [submit-btn (dom/by-id "submit-receipt")]
                      (events/listen! submit-btn :click submit-receipt)
                      (events/listen! submit-btn :mouseover add-help)
                      (events/listen! submit-btn :mouseout remove-help)))
    "setup-tab"   (dom/set-html! (dom/by-id "contents") (setup-tab-html))
    "history-tab" (do
                    (dom/set-html! (dom/by-id "contents") (history-tab-html))
                    (refresh-history)
                    (events/listen! (dom/by-id "refresh-history") :click refresh-history))
    ;; Finally, catch clicks on empty parts of tabbar, mostly just for code cleanness.
    "tabbar"      (do ))
  (storage-to-page))


(defn submit-receipt []
  (let [params-map {:user-id (read :user-id nil), :password (read :password nil)}
        params-map (assoc params-map :uid (+ (goog.string.getRandomString) "-" (:user-id params-map)))
        params-map (reduce-kv (fn [init key id] (assoc init key (clj-value id)))
                              params-map
                              receipt-tab-controls)
        params-map (update-in params-map [:for-whom] (partial reduce str))]
    (page-to-storage)
    (remote-callback :enter-receipt [params-map]
      (fn [result]
        (condp = (:status result)
          db/SUCCESS (do
                       (clear-receipt-page)
                       (dom/set-html! (dom/by-id "contents")
                         (confirmation-html true (:formatted result))))
          db/FAILURE (dom/set-html! (dom/by-id "contents")
                       (confirmation-html false (:errmsg result))))
        (events/listen! (dom/by-id "next-receipt") :click
          #(set-tab "receipt-tab"))))))


(defn refresh-history []
  (let [password (read :password nil)]
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
