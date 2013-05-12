(ns degel.receipts.html
  (:require-macros [hiccups.core :refer [html]])
  (:require [hiccups.runtime] ;; Needed by hiccups.core macros
            ))


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

(defn confirmation-html [success confirmation]
  (html
   [:div {:id "receipt-body"}
    [:p.confirmation [:div:label (if success "Ok: " "Error: ")] confirmation]
    [:input {:type "button"
             :value (if success "Next receipt" "Try again")
             :id "next-receipt"}]]))


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
