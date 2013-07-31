(ns degel.webol.client
  (:require [domina :as dom :refer [log]]
            [redmapel :as rml]
            [degel.webol.page :as page]))


(def ^:export webol-tree (rml/make-redmapel-tree))

(def memory-per-row 10)
(def memory-num-rows 10)


(defn clear-all []
  (doseq [n (range (* memory-per-row memory-num-rows))]
    (rml/put! webol-tree [:memory n] 0))
  (rml/put! webol-tree [:register :pc] 0)
  (rml/put! webol-tree [:register :sp] nil))


(defn render-screen []
  (let [canvas (dom/by-id "canvas1")
        ctx    (.getContext canvas "2d")
        width  (.-width canvas)
        height (.-height canvas)]
    (set! (-> canvas .-style .-border) "2px dashed gray")
    [canvas ctx width height]))


(defn ^:export init []
  (dom/set-html! (dom/by-id "page") (page/webol-page))
  (dom/set-html! (dom/by-id "memory") (page/table memory-num-rows memory-per-row)))
