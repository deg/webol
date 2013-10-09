(ns degel.webol.server
  (:require [shoreleave.middleware.rpc :as rpc]
            [degel.webol.parser :refer [parse-line]]
            [degel.cljutil.devutils :as dev]))


(rpc/defremote get-parse-tree [line]
  (parse-line line true))


(defn app-properties
  "Descriptor of this web app, primarily for the sake of muxx."
  []
  {:name          "webol"
   :base-page     "/webol.html"
   :production-js "js/receipts.js"
   :dev-js        "js/receipts-dev.js"})
