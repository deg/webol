(ns degel.webol.client
  (:require [domina :as dom :refer [log]]
            [redmapel :as rml]
            [degel.utils.html :as dhtml]
            [degel.webol.page :as page]))


(def ^:export webol-tree (rml/make-redmapel-tree))

(defn fetch [key]
  (rml/fetch webol-tree key))

(def memory-per-row 10)
(def memory-num-rows 10)


(defn clear-all []
  (doseq [n (range (* memory-per-row memory-num-rows))]
    (rml/put! webol-tree [:memory n] 0))
  (rml/put! webol-tree [:register :pc] 0)
  (rml/put! webol-tree [:register :sp] nil))

(defn register-name [index]
  (case index
    0 "Program counter"
    1 "Stack pointer"
    "Unknown"))

(defn register-key [index]
  (case index
    0 [:register :pc]
    1 [:register :sp]
    [:error]))

(defn or-empty [s]
  (str (or s "(empty)")))


(defn render-screen []
  (let [canvas (dom/by-id "canvas1")
        ctx    (.getContext canvas "2d")
        width  (.-width canvas)
        height (.-height canvas)]
    (set! (-> canvas .-style .-border) "2px dashed gray")
    [canvas ctx width height]))


(defn ^:export init []
  (clear-all)
  (dom/set-html! (dom/by-id "page") (page/webol-page))
  (dom/set-html! (dom/by-id "memory")
    (dhtml/table memory-num-rows memory-per-row
                 :cell-fn #(let [loc (+ (* %1 memory-per-row) %2)]
                             (page/location-and-value
                              loc
                              (-> [:memory loc] fetch or-empty)))))
  (dom/set-html! (dom/by-id "registers")
    (dhtml/table 1 2
                 :cell-fn #(page/location-and-value
                            (register-name %2)
                            (->  (register-key %2) fetch or-empty)))))
