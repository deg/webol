(ns degel.receipts.pages
  (:require-macros [hiccups.core :refer [html]])
  (:require [hiccups.runtime] ;; Needed by hiccups.core macros
            [domina :as dom :refer [log]]
            [degel.utils.html :refer [control-pair
                                      label-and-autocomplete-text-field
                                      submit-button
                                      selection-list]]))


(def receipt-tab-controls [:paid-by
                           :paid-by-check
                           :paid-by-other
                           :date
                           :amount
                           :category
                           :category-other
                           :vendor
                           :comment
                           :for-whom
                           :for-whom-other])

(defn receipt-tab-html []
  (html
   [:form.form-horizontal {:id "receipt-body"}
    (selection-list :paid-by "Paid By"
                    {:style "margin-top:10px"
                     :with-others [[:check "Ck #"] [:other "Source"]]}
                    false nil [])
    (control-pair :date "Date"
                  {:type "Date"
                   :required ""})
    (control-pair :amount "Amount"
                  {:type "Number"
                   :step "0.01"
                   :title "Enter price"
                   :placeholder "price"
                   :required ""
                   :MaxLength 10})
    (selection-list :category "Category" {:with-others [[:other "Other"]]} false nil [])
    (selection-list :vendor "Vendor" {:with-others [[:other "Other"]]} false nil [])
    (label-and-autocomplete-text-field :comment "Comment" {})
    (selection-list :for-whom "For Whom"
                    {:style "margin-bottom:10px"
                     :with-others [[:other "For other"]]}
                    true nil [])
    (submit-button :submit-receipt "Submit Receipt")]))


(defn confirmation-html [success confirmation]
  (html
   [:form.form-horizontal {:id "receipt-body"}
    [:p.confirmation [:div:label (if success "Ok: " "Error: ")] confirmation]
    (submit-button :next-receipt (if success "Next receipt" "Try again"))]))


(def setup-tab-controls [:user-id :password])

(defn setup-tab-html []
  (html
   [:form.form-horizontal {:id "setup-account"}
    (control-pair :user-id "User ID"
                  {:type "text"
                   :required ""
                   :MaxLength "16"})
    (control-pair :password "Password"
                  {:type "password"
                   :required ""
                   :MaxLength "10"})]
   [:p [:a {:href "help.html"} "Help"] " about this application."]))


(defn history-tab-html []
  (html
   [:form.form-horizontal {:id "history-body"}
    [:div {:id "History"}]
    (submit-button :refresh-history "Refresh")]))
