(ns degel.receipts.pages
  (:require-macros [hiccups.core :refer [html]])
  (:require [hiccups.runtime] ;; Needed by hiccups.core macros
            [domina :as dom :refer [log]]
            [degel.receipts.html :refer [control-pair submit-button selection-list]]))


(def receipt-tab-controls {:paid-by  "PaidBy"
                           :date     "Date"
                           :amount   "Amount"
                           :category "Category"
                           :category-other "CategoryOther"
                           :vendor   "Vendor"
                           :comment  "Comment"
                           :for-whom "ForWhom"})

(defn receipt-tab-html []
  (html
   [:form.form-horizontal {:id "receipt-body"}
    (selection-list "PaidBy" "Paid By"
                    {:style "margin-top:10px"}
                    false nil [])
    (control-pair "Date" "Date"
                  {:type "Date"
                   :required ""})
    (control-pair "Amount" "Amount"
                  {:type "Number"
                   :step "0.01"
                   :title "Enter price"
                   :placeholder "price"
                   :required ""
                   :MaxLength 10})
    (selection-list "Category" "Category"
                    nil false nil [])
    (control-pair "CategoryOther" "Other category"
                  {:type "text" "none"
                   :required ""
                   :autocomplete "on"
                   :MaxLength 15})
    (control-pair "Vendor" "Vendor"
                  {:type "text"
                   :required ""
                   :autocomplete "on"
                   :MaxLength "30"})
    (control-pair "Comment" "Comment"
                  {:type "text"
                   :autocomplete "on"
                   :MaxLength "40"})
    (selection-list "ForWhom" "For whom"
                    {:style "margin-bottom:10px"}
                    true nil [])
    (submit-button "submit-receipt" "Submit Receipt")]))


(defn confirmation-html [success confirmation]
  (html
   [:form.form-horizontal {:id "receipt-body"}
    [:p.confirmation [:div:label (if success "Ok: " "Error: ")] confirmation]
    (submit-button "next-receipt" (if success "Next receipt" "Try again"))]))


(def setup-tab-controls {:user-id "user-id"
                         :password "Password"})

(defn setup-tab-html []
  (html
   [:form.form-horizontal {:id "setup-account"}
    (control-pair "user-id" "User ID"
                  {:type "text"
                   :required ""
                   :MaxLength "16"})
    (control-pair "Password" "Password"
                  {:type "password"
                   :required ""
                   :MaxLength "10"})]
   [:p [:a {:href "help.html"} "Help"] " about this application."]))


(defn history-tab-html []
  (html
   [:form.form-horizontal {:id "history-body"}
    [:div {:id "History"}]
    (submit-button "refresh-history" "Refresh")]))
