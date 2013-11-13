(ns degel.webol.page
  (:require-macros [hiccups.core :refer [html]])
  (:require [clojure.string :as str]
            [hiccups.runtime] ;; Needed by hiccups.core macros
            [domina :as dom :refer [log]]
            [goog.ui.Zippy :as zippy]
            [degel.webol.store :as store]
            [degel.utils.html :as dhtml]))


(defn header-bar []
  (html
   [:div
    [:h1 "Webol Computer"]
    [:h2 [:address.author "Copyright &copy; 2013; "
          [:a {:href "mailto:deg@degel.com"} "David Goldfarb"]]]]))

(defn program-name-bar []
  (html [:h2 "Loaded program: " [:span#progname (store/fetch [:program :name])]]))

(defn location-and-value [location value]
  (html [:div
         [:div.location location]
         [:div.value value]]))

(defn- webol-menu-bar []
  (dhtml/cmd-button-group
   [[:list-program "List"]
    [:save-program "Save"]
    [:clear-program "Clear"]
    [:run-program "Run"]
    [:abort-program "Abort"]
    [:step-program "Step"]
    [:help-program "Help"]]))


(defn manpage-frame [page]
  (html [:iframe#manpage {:src page :width "100%" :height "100%"}]))


(defn manpage-and-menu [page]
  (html [:div#manual.rightManCol
         (dhtml/cmd-button-group
          [[:manual-introduction "Introduction"]
           [:manual-tutorial "Tutorial"]
           [:manual-reference "Reference"]
           [:manual-exercises "Exercises"]])
         (manpage-frame page)]))


(defn- page-bottom-notices []
  [:h6#footer
   [:div#credits-tag "Credits"]
   [:div#credits-body (str/join
                       "<br>"
                       (map (fn [[_ artifact version]] (str artifact ": " version))
                            (store/fetch [:versions])))]])


(defn webol-page []
  (html
   [:div#main.leftAppCol
    (header-bar)
    (program-name-bar)
    (webol-menu-bar)
    [:div.canvas-wrapper
     [:canvas#sketchboard {:width "640px" :height "360px"}
      "This browser does not support canvases"]]
    (dhtml/label-and-autocomplete-text-field :input "Cmd" {:size 64})
    [:table#registers]
    [:table#memory]
    [:div#program]
    (page-bottom-notices)]
   (manpage-and-menu "webol-intro.html")))

(defn enable-behaviors []
  (goog.ui.Zippy. (dom/by-id "credits-tag") (dom/by-id "credits-body")))
