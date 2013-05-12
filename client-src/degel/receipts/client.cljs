(ns degel.receipts.client
  (:require-macros [hiccups.core :refer [html]])
  (:require [domina :refer [append! attr by-class by-id destroy!
                            set-html! set-inner-html! set-value! value]]
            [domina.events :refer [listen! prevent-default target]]
            [hiccups.runtime] ;; Needed by hiccups.core macros
            [shoreleave.remotes.http-rpc :refer [remote-callback]]
            [degel.receipts.static-validators :refer [validate-receipt-fields]]))


(declare submit-receipt add-help remove-help)


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


(defn confirmation-html [success confirmation]
  (html
   [:div {:id "receipt-body"}
    [:p.confirmation [:div:label (if success "Ok: " "Error: ")] confirmation]
    [:input {:type "button"
             :value (if success "Next receipt" "Try again")
             :id "next-receipt"}]]))


(defn entry-html []
  (html
   [:form {:id "receipt-body"}
    [:div
     [:label {:for "PaidBy"} "Paid By:"]
     [:input {:name "PaidBy"
              :id "PaidBy"
              :list "PaymentDevices"
              :title "Enter 'cash', 'ck N' or 'vDDDD'"
              :placeholder "cc #, ck #, or cash"
              :required true
              :MaxLength 8}]]
    [:div
     [:label {:for "Date"} "Date:"]
     [:input {:name "Date"
              :id "Date"
              :type "Date"
              :required true
              :MaxLength "10"}]]
    [:div
     [:label {:for "Amount"} "Amount:"]
     [:input {:name "Amount"
              :id "Amount"
              :type "Number"
              :step "0.01"
              :title "Enter price"
              :placeholder "price"
              :required true
              :MaxLength 10}]]
    [:div
     [:label {:for "Category"} "Category:"]
     [:input {:name "Category"
              :id "Category"
              :required true
              :autocomplete "on"
              :MaxLength 15}]]
    [:div
     [:label {:for "Vendor"} "Vendor:"]
     [:input {:name "Vendor"
              :id "Vendor"
              :required true
              :autocomplete "on"
              :MaxLength "30"}]]
    [:div
     [:label {:for "Comments"} "Comments:"]
     [:input {:name "Comments"
              :id "Comments"
              :autocomplete "on"
              :MaxLength "40"}]]
    [:div
     [:label {:for "ForWhom"} "For whom:"]
     [:input {:name "ForWhom"
              :id "ForWhom"
              :autocomplete "on"
              :MaxLength "15"}]]]
   [:div
    [:input {:type "button"
             :value "Submit Receipt"
             :id "submit-receipt"}]]))


(defn setup-html []
  (html
   [:form {:id "setup-account"}
    [:div [:label {:for "Password"} "Password"]
     [:input {:name "Password"
              :id "Password"
              :type "password"
              :required true
              :MaxLength "10"}]]
    [:div
     [:input {:type "button"
              :value "Setup Account"
              :id "submit-pwd"}]]]
   [:p [:a {:href "help.html"} "Help"] "about this application."]))


(defn history-html []
  (html
   [:div {:id "ForHistory"}]
   [:div
    [:input {:type "button"
             :value "Refresh"
             :id "refresh-history"}]]))


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

(defn show-setup-tab []
  (set-html! (by-id "receipt-body") (setup-html))
  (fill-defaults))

(defn show-history-tab []
  (set-html! (by-id "receipt-body") (history-html))
  (fill-defaults))


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
      (append! (by-id "receipt-body") (html [:div.help message])))))


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
    (let [receipt-tab (by-id "receipt-tab")
          submit-tab (by-id "setup-tab")
          history-tab (by-id "history-tab")
          password-btn (by-id "submit-pwd")
          history-btn (by-id "refresh-history")]
      (show-new-receipt {})
      (fill-defaults)
      (listen! receipt-tab :click show-new-receipt)
      (listen! submit-tab :click show-setup-tab)
      (listen! history-tab :click show-history-tab)
      (listen! password-btn :click cache-password)
      (listen! history-btn :click refresh-history))))
