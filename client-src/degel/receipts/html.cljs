(ns degel.receipts.html
  (:require-macros [hiccups.core :refer [html]])
  (:require [hiccups.runtime] ;; Needed by hiccups.core macros
            [domina :as dom :refer [log]]
            [domina.events :as events]
            [degel.receipts.storage :refer [read]]))


(defn- clj-value [id]
  (-> id dom/by-id (#(when % (dom/value %))) js->clj))


(defn- set-clj-value! [id value]
  (dom/set-value! (dom/by-id id) (clj->js value)))


(defn control-pair [id label attrs]
  [:div.control-group {:id (str id "-group")}
   [:label.control-label {:for id} (str label ":&nbsp;")]
   [:div.control
    [:input (merge {:name id :id id} attrs)]]])


(defn submit-button [id label]
  [:div.control-group
   [:div.controls
    [:button.btn {:type "submit" :id id :class "btn-large"}
     label]]])


(defn button-handler [handler]
  "Wrap button handler to create a new handler which also disables the
   standard button post behavior."
  (fn [evt]
    (handler)
    (events/prevent-default evt)))


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


(defn fill-select-options
  "Fill in the elements of a select control.
   list-id - DOM id of the select control.
   db-key - Storage key of the persistent elements list. Defaults to a keyword composing
            the list-id and '-options'.
   with-other - If supplied, extra selection element that will invoke an overflow control.
   other-id - DOM id of the overflow control. This will typically be a text entry box or a div
            that includes some form of free data entry control within."
  [list-id & {:keys [db-key with-other other-id]
              :or {db-key (keyword (str list-id "-options"))}}]
  (read db-key
        (fn [vals _]
          (dom/set-html! (dom/by-id list-id)
            (html [:select
                   (selection (map #(if (vector? %) % [% %])
                                   (if with-other (conj vals with-other) vals))
                              (or (clj-value list-id) (read db-key nil)))]))
          (when with-other
            (let [fill-other
                  #(let [category (clj-value list-id)
                         display-style (if (or (empty? category)
                                               (= category with-other))
                                         "block" "none")]
                     (dom/set-style! (dom/by-id other-id) "display" display-style))]
              (fill-other)
              (events/listen! (dom/by-id list-id) :change fill-other))))))


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
