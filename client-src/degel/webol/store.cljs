(ns degel.webol.store
  (:require [domina :as dom :refer [log]]
            [degel.redmapel :as rml]))


(def ^:export webol-tree (rml/make-redmapel-tree))

(defn fetch [key]
  (rml/fetch webol-tree key))

(defn put! [key value]
  (rml/put! webol-tree key value)
  value)

(defn update! [key f & args]
  (apply rml/update! webol-tree key f args)
  (fetch key))

(defn alert! [key id f]
  (rml/alert! webol-tree key id f))

(defn guard! [key id f]
  (rml/guard! webol-tree key id f))
