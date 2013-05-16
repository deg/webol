(ns degel.receipts.html
  (:require-macros [hiccups.core :refer [html]])
  (:require [hiccups.runtime] ;; Needed by hiccups.core macros
            ))

(defn control-pair [id label attrs]
  [:div.control-group
   [:label.control-label {:for id} (str label ":&nbsp;")]
   [:div.control
    [:input (merge {:name id :id id} attrs)]]])

(defn submit-button [id label]
  [:div.control-group
   [:div.controls
    [:button.btn {:type "submit" :id id}
     label]]])


(defn entry-html []
  (html
   [:form.form-horizontal {:id "receipt-body"}
    (control-pair "PaidBy" "Paid By"
                  {:type "text"
                   :list "PaymentDevices"
                   :title "Enter 'cash', 'ck N' or 'vDDDD'"
                   :placeholder "cc #, ck #, or cash"
                   :required true
                   :MaxLength 8})
    (control-pair "Date" "Date"
                  {:type "Date"
                   :required true})
    (control-pair "Amount" "Amount"
                  {:type "Number"
                   :step "0.01"
                   :title "Enter price"
                   :placeholder "price"
                   :required true
                   :MaxLength 10})
    (control-pair "Category" "Category"
                  {:type "text"
                   :required true
                   :autocomplete "on"
                   :MaxLength 15})
    (control-pair "Vendor" "Vendor"
                  {:type "text"
                   :required true
                   :autocomplete "on"
                   :MaxLength "30"})
    (control-pair "Comment" "Comment"
                  {:type "text"
                   :autocomplete "on"
                   :MaxLength "40"})
    (control-pair "ForWhom" "For whom"
                  {:type "text"
                   :autocomplete "on"
                   :MaxLength "15"})
    [:div
     [:input {:type "button"
              :value "Submit Receipt"
              :id "submit-receipt"}]]
    ;; Doesn't work; Centers button, but causes POST.
    ;; [TODO] Fix soon if we don't move to a different lib.
    #_(submit-button "submit-receipt" "Submit Receipt")]))


(defn confirmation-html [success confirmation]
  (html
   [:div {:id "receipt-body"}
    [:p.confirmation [:div:label (if success "Ok: " "Error: ")] confirmation]
    [:input {:type "button"
             :value (if success "Next receipt" "Try again")
             :id "next-receipt"}]]))


(defn setup-html []
  (html
   [:form.form-horizontal {:id "setup-account"}
    (control-pair "user-id" "User ID"
                  {:type "text"
                   :required true
                   :MaxLength "10"})
    (control-pair "Password" "Password"
                  {:type "password"
                   :required true
                   :MaxLength "10"})
    (submit-button "submit-pwd" "Setup Account")]
   [:p [:a {:href "help.html"} "Help"] " about this application."]))


(defn history-html []
  (html
   [:div {:id "ForHistory"}]
   [:div
    [:input {:type "button"
             :value "Refresh"
             :id "refresh-history"}]]))
