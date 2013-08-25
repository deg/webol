(ns degel.utils.html
  (:require-macros [hiccups.core :refer [html]])
  (:require [clojure.string :as str]
            [hiccups.runtime] ;; Needed by hiccups.core macros
            [domina :as dom :refer [log]]
            [domina.events :as events]
            [degel.utils.storage :refer [read]]
            [degel.utils.utils :as utils]))


(defn- clj-value [id]
  (some-> id dom/by-id dom/value js->clj))


(defn- set-clj-value! [id value]
  (dom/set-value! (dom/by-id id) (clj->js value)))


(defn table
  "Build an html table, sized rows X columns.
   cell-fn is called to generate the content of each cell and is passed the
   zero-based row/column indices."
  [rows columns & {:keys [cell-fn]}]
  (html [:table
         (doall
          (map (fn [r] [:tr (map (fn [c] [:td (cell-fn r c)])
                                 (range columns))])
               (range rows)))]))


(defn control-pair [id label attrs]
  [:div.control-group {:id (str id "-group")}
   [:label.control-label {:for id} (str label ":&nbsp;")]
   [:div.control
    [:input (merge {:name id :id id} attrs)]]])


(defn label-and-autocomplete-text-field [id label attrs]
  (control-pair id label
                (assoc attrs :type "text"
                       :autocomplete "on")))


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


(defn selection
  "Build a selection control.
   values - vector of values to place in the list. Each value is either a vector of item id and
   the string to present to the user, or a bare string that will play both roles.
   selected-values - vector of list ids to be initially selected, or a single such id."
  [values selected-values]
  (for [val-or-pair values]
    (let [value (if (string? val-or-pair) val-or-pair (first val-or-pair))
          text  (if (string? val-or-pair) val-or-pair (second val-or-pair))]
      [:option
       (into {:value value}
             (when (or (and (vector? selected-values)
                            (some #{value} selected-values))
                       (= value selected-values))
               {:selected ""}))
       text])))


(defn selection-list [id label attrs multiple? selected-value value-text-pairs]
  (let [with-others (map #(if (string? %) % (first %))
                         (:with-others attrs))
        other-labels (into {} (map #(if (string? %)
                                      [% (str % " " label)]
                                      %)
                                   (:with-others attrs)))
        control-group-attrs (assoc attrs
                              :id (str id "-group")
                              :with-others with-others)
        control-group [:div.control-group control-group-attrs
                       [:label.control-label {:for id} (str label ":&nbsp;")]
                       [:div.control
                        [:select (into {:id id}
                                       (when multiple? { :multiple ""}))
                         (selection value-text-pairs selected-value)]]]]
    (into [:div control-group]
          (map #(label-and-autocomplete-text-field
                 (str id "-" %) (% other-labels) {:required ""})
               with-others))))


(defn fill-select-options
  "Fill in the elements of a select control.
   list-id - DOM id of the select control.
   db-key - Storage key of the persistent elements list. Defaults to a keyword composing
            the list-id and '-options'."
  [list-id & {:keys [db-key] :or {db-key (keyword (str list-id "-options"))}}]
  (let [list-ctrl (dom/by-id list-id)
        with-others (-> list-ctrl .-parentNode .-parentNode
                        (dom/attr :with-others) utils/safe-read-string)]
    (read db-key
          (fn [vals _]
            (dom/set-html! list-ctrl
              (html [:select
                     (selection (into vals with-others)
                                (or (clj-value list-id) (read db-key nil)))]))
            (when with-others
              (let [fill-others
                    #(doseq [other with-others]
                       (let [value (clj-value list-id)
                             other-group-id (str list-id "-" other "-group")
                             display-style (if (or (and (vector? value)
                                                        (some #{other} value))
                                                   (= value other))
                                             "block" "none")
                             ]
                         (dom/set-style! (dom/by-id other-group-id) "display" display-style)))]
                (fill-others)
                (events/listen! list-ctrl :change fill-others)))))))


(defn value-with-other [ctrl-id]
  (let [value (clj-value ctrl-id)
        others (-> (dom/by-id ctrl-id) .-parentNode .-parentNode
                   (dom/attr :with-others) utils/safe-read-string)]
    (if (empty? others)
      value
      (str/join " "
                (map #(let [other (some #{%} others)]
                        (if other
                          (str other ":" (clj-value (str ctrl-id "-" other)))
                          %))
                     (if (vector? value) value (vector value)))))))


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
