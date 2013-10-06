(ns degel.receipts.client
  (:require-macros [hiccups.core :refer [html]])
  (:require [clojure.string :refer [blank?]]
            [domina :as dom :refer [log]]
            [domina.events :as events]
            [hiccups.runtime] ;; Needed by hiccups.core macros
            [shoreleave.remotes.http-rpc :refer [remote-callback]]
            [degel.redmapel :as rml]
            [degel.receipts.static-validators :refer [validate-receipt-fields]]
            [degel.utils.storage :refer [read write-local]]
            [degel.utils.html :as dhtml]
            [degel.utils.utils :as dutils]
            [degel.receipts.pages :refer [receipt-tab-controls receipt-tab-html
                                          confirmation-html
                                          setup-tab-controls setup-tab-html
                                          history-tab-html]]
            [degel.receipts.db :as db]))


(declare submit-receipt add-help remove-help refresh-history fill-defaults)

(def ^:export state-tree (rml/make-redmapel-tree))


(defn page-to-storage
  "Save webpage control values to persistent storage."
  []
  (let [controls (cond (dom/by-id :paid-by)   receipt-tab-controls
                       (dom/by-id :password) setup-tab-controls)]
    (doseq [id controls]
      (write-local id (dhtml/clj-value id)))))


(defn storage-to-page
  "Load webpage control values from persistent storage."
  []
  (let [controls (cond (dom/by-id :paid-by)   receipt-tab-controls
                       (dom/by-id :password) setup-tab-controls)]
    (doseq [id controls]
      (read id #(dhtml/set-clj-value! id %)))))


(defn clear-receipt-page []
  (doseq [id receipt-tab-controls]
    (dhtml/set-clj-value! id ""))
  (fill-defaults)
  (page-to-storage))


(defn set-tab [tab]
  (rml/put! state-tree [:current-tab] tab))

(defn update-vendors [- category]
  (when category
    (let [db-key (keyword (str "category-" category "-options"))]
      (dhtml/fill-select-options :vendor :db-key db-key))))


(defn on-current-tab  [_ _ _ tab]
  (page-to-storage)
  (condp = tab
    :receipt-tab (do
                   (dom/set-html! (dom/by-id :contents) (receipt-tab-html))
                   (dhtml/fill-select-options :paid-by)
                   (dhtml/fill-select-options :category :callback update-vendors)
                   (dhtml/fill-select-options :for-whom)
                   (let [submit-btn (dom/by-id :submit-receipt)]
                     (events/listen! submit-btn :click (dhtml/button-handler submit-receipt))))
    :status      ((rml/fetch state-tree [:tab-action]))
    :setup-tab   (dom/set-html! (dom/by-id :contents) (setup-tab-html))
    :history-tab (do
                   (dom/set-html! (dom/by-id :contents) (history-tab-html))
                   (refresh-history)
                   (events/listen! (dom/by-id :refresh-history) :click
                     (dhtml/button-handler refresh-history)))
    ;; Finally, catch clicks on empty parts of tabbar, mostly just for code cleanness.
    "tabbar"      (do ))
  (storage-to-page))

(rml/alert! state-tree [:current-tab] :tab-change on-current-tab)


(defn submit-receipt []
  (let [params-map {:user-id (read :user-id nil), :password (read :password nil)}
        params-map (assoc params-map :uid (+ (goog.string.getRandomString) "-" (:user-id params-map)))
        params-map (apply assoc params-map
                          (mapcat (fn [id] [id (dhtml/value-with-other id)])
                                  receipt-tab-controls))
        params-map (dissoc params-map :paid-by-other)
        params-map (dissoc params-map :category-other)
        params-map (dissoc params-map :for-whom-other)]
    (dom/add-class! (dom/by-id :submit-receipt) "btn-danger")
    (page-to-storage)
    (remote-callback :enter-receipt [params-map]
      (fn [result]
        (dom/remove-class! (dom/by-id :submit-receipt) "btn-danger")
        (rml/put! state-tree [:tab-action]
                  (if (= (:status result) db/SUCCESS)
                    (fn []
                       (clear-receipt-page)
                       (dom/set-html! (dom/by-id :contents)
                         (confirmation-html true (:formatted result))))
                    (fn []
                      (dom/set-html! (dom/by-id :contents)
                        (confirmation-html false (:errmsg result)))
                      (events/listen! (dom/by-id :next-receipt) :click
                        (dhtml/button-handler #(set-tab :receipt-tab))))))
        (set-tab :status)))))


(defn render-table [rows temp?]
  ;; [TODO] {FogBugz:142} Use dot notation once https://github.com/teropa/hiccups/issues/4
  ;;        is resolved.
  (html [:table {:class "table table-striped table-bordered table-condensed"}
         [:thead [:tr
                  (map (fn [s] [:td s])
                       ["Paid by" "Date" "Amount" "Category" "Vendor" "Comment" "For Whom"])]]
         [:tbody (map (fn [row] [:tr (if temp? {:class :error} {})
                                 (map (fn [item] [:td item])
                                      (clojure.string/split row #";"))])
                      rows)]]))


(defn refresh-history []
  (let [password (read :password nil)
        cached-history (read :cached-history nil)]
    (dom/set-html! (dom/by-id "History") (render-table cached-history true))
    (remote-callback :fill-receipt-history [password]
      (fn [history]
        (dom/set-html! (dom/by-id "History") (render-table history false))
        (write-local :cached-history history)))))


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
  (dom/set-value! (dom/by-id :date) (dutils/now-string)))


(defn ^:export init []
  (set-tab :receipt-tab)
  (dom/set-html! (dom/by-id :tabbar)
    (html (dhtml/button-group "tabbar-buttons" true
            [{:id :receipt-tab :text "Receipt"}
             {:id :setup-tab :text "Setup"}
             {:id :history-tab :text "History"}])))
  (events/listen! (dom/by-id "tabbar") :click
    #(-> % events/target (. -id) keyword set-tab))
  (dhtml/set-active-button "tabbar-buttons" :receipt-tab)
  (fill-defaults))
