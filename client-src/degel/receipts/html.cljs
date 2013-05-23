(ns degel.receipts.html
  (:require-macros [hiccups.core :refer [html]])
  (:require [hiccups.runtime] ;; Needed by hiccups.core macros
            [domina :as dom]))


(defn control-pair [id label attrs]
  [:div.control-group {:id (str id "-group")}
   [:label.control-label {:for id} (str label ":&nbsp;")]
   [:div.control
    [:input (merge {:name id :id id} attrs)]]])


(defn submit-button [id label]
  [:div.control-group
   [:div.controls
    [:button.btn {:type "submit" :id id}
     label]]])


(defn selection-list [id label selected-value value-text-pairs]
  [:div.control-group {:id (str id "-group")}
   [:label.control-label {:for id} (str label ":&nbsp;")]
   [:div.control
    [:select {:id id}
     (for [[value text] value-text-pairs]
       [:option
        (if (= selected-value value)
          {:value value :selected ""}
          {:value value})
        text])]]])


(defn button-group
  "Return HTML for a button-group.
   id is the id for the group div.
   radio? (nyi) is true if the group should behave as radio buttons (only one selection at a
   time) or false to allow multiple selections.
   buttons is a vector of maps of button attributes. For now, we support just :id and :text"
  [id radio? buttons]
  [:div {:class "btn-group"
         :id id
         :data-toggle "buttons-radio"}
   (for [button buttons]
     [:button {:type "button" :class "btn btn-primary" :id (:id button)}
      (:text button)])])


(defn set-active-button
  "Set one button to be active in a button-group.
   [TODO] Need to learn xpath syntax or equivalent, to only search for button within group.
   [TODO] Efficiency suggests keeping a map of keywords to dom elements, if that is possible,
          rather than searching each time"
  [button-group button]
  (dom/remove-class! (dom/by-id button-group) "active")
  (dom/add-class! (dom/by-id button) "active"))


(defn entry-html []
  (html
   [:form.form-horizontal {:id "receipt-body"}
    (selection-list "PaidBy" "Paid By" nil [])
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
                   :required ""
                   :MaxLength "10"})
    (control-pair "Password" "Password"
                  {:type "password"
                   :required ""
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
