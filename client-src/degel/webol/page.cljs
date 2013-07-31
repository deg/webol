(ns degel.webol.page
  (:require-macros [hiccups.core :refer [html]])
  (:require [hiccups.runtime] ;; Needed by hiccups.core macros
            [domina :as dom :refer [log]]))


(defn table [rows columns & {:keys [cell-fn]}]
  (html
   [:table
    (doall
     (map (fn [r] [:tr (map (fn [c] [:td (cell-fn r c)])
                            (range columns))])
          (range rows)))]))


(defn webol-page []
  (html
   [:div#main
    [:h1 "Webol Computer"]
    [:h2 [:address.author "Copyright &copy; 2013; "
          [:a {:href "mailto:deg@degel.com"} "David Goldfarb"]]]
    [:canvas#sketchboard
     {:width 500 :height 300 :style "border: 2px solid blue"}
     "This browser does not support canvases"]
    [:table#memory]
    [:table#registers]
    [:div#program]]))
