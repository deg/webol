(ns degel.receipts.pages
  (:require-macros [hiccups.core :refer [html]])
  (:require [hiccups.runtime] ;; Needed by hiccups.core macros
            [degel.receipts.html :refer [control-pair submit-button selection-list]]))


(def receipt-tab-controls {:paid-by  "PaidBy"
                           :date     "Date"
                           :amount   "Amount"
                           :category "Category"
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
    (control-pair "Category" "Category"
                  {:type "text"
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
                    true nil
                    [["D" "David"] ["H" "Heidi"] ["A" "Aviva"] ["S" "Shoshana"] ["O" "Other"]])
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


(def setup-tab-controls {:user-id "user-id"
                         :password "Password"})

(defn setup-tab-html []
  (html
   [:form.form-horizontal {:id "setup-account"}
    (control-pair "user-id" "User ID"
                  {:type "text"
                   :required ""
                   :MaxLength "10"})
    (control-pair "Password" "Password"
                  {:type "password"
                   :required ""
                   :MaxLength "10"})]
   [:p [:a {:href "help.html"} "Help"] " about this application."]))


(defn history-tab-html []
  (html
   [:div {:id "ForHistory"}]
   [:div
    [:input {:type "button"
             :value "Refresh"
             :id "refresh-history"}]]))
