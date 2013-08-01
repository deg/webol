(ns degel.webol.page
  (:require-macros [hiccups.core :refer [html]])
  (:require [hiccups.runtime] ;; Needed by hiccups.core macros
            [domina :as dom :refer [log]]
            [degel.utils.html :as dhtml]))


(defn location-and-value [location value]
  (html [:div
         [:div.location location]
         [:div.value value]]))


(defn webol-page []
  (html
   [:div#main
    [:h1 "Webol Computer"]
    [:h2 [:address.author "Copyright &copy; 2013; "
          [:a {:href "mailto:deg@degel.com"} "David Goldfarb"]]]
    [:div.canvas-wrapper
     [:canvas#sketchboard
      "This browser does not support canvases"]]
    [:table#memory]
    [:table#registers]
    (dhtml/label-and-autocomplete-text-field :input "Cmd" {:size 64})
    [:div#program]]))
