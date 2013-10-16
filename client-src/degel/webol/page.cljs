(ns degel.webol.page
  (:require-macros [hiccups.core :refer [html]])
  (:require [hiccups.runtime] ;; Needed by hiccups.core macros
            [domina :as dom :refer [log]]
            [degel.webol.store :as store]
            [degel.utils.html :as dhtml]))


(defn location-and-value [location value]
  (html [:div
         [:div.location location]
         [:div.value value]]))


(defn webol-page []
  (html
   [:div#main.leftAppCol
    [:h1 "Webol Computer"]
    [:h2 [:address.author "Copyright &copy; 2013; "
          [:a {:href "mailto:deg@degel.com"} "David Goldfarb"]]]
    [:div.save-load
     (dhtml/cmd-button-group
         [[:list-program "List"]
          [:save-program "Save"]
          [:clear-program "Clear"]
          [:run-program "Run"]
          [:abort-program "Abort"]
          [:help-program "Help"]])]
    [:div.canvas-wrapper
     [:canvas#sketchboard {:width "640px" :height "360px"}
      "This browser does not support canvases"]]
    (dhtml/label-and-autocomplete-text-field :input "Cmd" {:size 64})
    [:table#memory]
    [:table#registers]
    [:div#program]
    [:h6 (clojure.string/join
          "<br>"
          (map (fn [[_ artifact version]] (str artifact ": " version))
               (store/fetch [:versions])))]]
   [:div#manual.rightManCol
    [:iframe {:src "webol-help.html" :width "100%" :height "100%"}]]))
