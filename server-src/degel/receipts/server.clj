(ns degel.receipts.server
  (:require [shoreleave.middleware.rpc :as rpc]
            [degel.receipts.db-init :as db-init]
            [degel.receipts.simpleDB :as simpleDB]
            [degel.receipts.receipts :refer [collect-receipt-history enter-receipt-internal]]
            [degel.cljutil.devutils :as dev]))


(rpc/defremote fill-receipt-history [password]
  (remove nil? (collect-receipt-history password)))

(rpc/defremote enter-receipt [columns]
  (enter-receipt-internal columns))

(rpc/defremote read-storage [key user-id password]
  (db-init/read-storage key user-id password))

(rpc/defremote write-storage [key value user-id password]
  (db-init/write-storage key value user-id password))

(defn init-db [password]
  (simpleDB/nuke-db password)
  (db-init/init-category-options password))

(defn app-properties
  "Descriptor of this web app, primarily for the sake of muxx."
  []
  {:name          "receipts"
   :base-page     "/receipts.html"
   :production-js "js/receipts.js"
   :dev-js        "js/receipts-dev.js"})
