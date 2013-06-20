(ns degel.receipts.html
  (:require-macros [hiccups.core :refer [html]])
  (:require [hiccups.runtime] ;; Needed by hiccups.core macros
            [domina :as dom :refer [log]]))


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


(defn selection [value-text-pairs selected-value]
  (for [[value text] value-text-pairs]
    [:option
     (if (if (vector? selected-value)
           (some #(= value %) selected-value)
           (= value selected-value))
       {:value value :selected ""}
       {:value value})
     text]))


(defn selection-list [id label attrs multiple? selected-value value-text-pairs]
  [:div.control-group (merge {:id (str id "-group")} attrs)
   [:label.control-label {:for id} (str label ":&nbsp;")]
   [:div.control
    [:select (if multiple?
               {:id id :multiple ""}
               {:id id})
     (selection value-text-pairs selected-value)]]])


(defn button-group
  "Return HTML for a button-group.
   id is the id for the group div.
   radio? (nyi) is true if the group should behave as radio buttons (only one selection at a
   time) or false to allow multiple selections.
   buttons is a vector of maps of button attributes. For now, we support just :id and :text"
  [id radio? buttons]
  [:div {:class "btn-group"
         :id id
         :data-toggle (if radio? "buttons-radio" "buttons-checkbox")}
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
