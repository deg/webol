(ns degel.receipts.server
  (:require [shoreleave.middleware.rpc :as rpc]
            [degel.webol.parser :refer [parse-line]]
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

(rpc/defremote get-parse-tree [line]
  (parse-line line true))

(defn init-db [password]
  (simpleDB/nuke-db password)
  (db-init/init-category-options password))
