(ns degel.receipts.remotes
  (:require [shoreleave.middleware.rpc :refer [defremote]]
            [degel.receipts.receipts :refer [collect-receipt-history enter-receipt-internal]]))

(defremote fill-paid-by [country]
  ["Cash" "Ck #" "other" "v0223" "v5760" "v9949"])

(defremote fill-receipt-history [password]
  (remove nil? (collect-receipt-history password)))

(defremote enter-receipt [columns]
  (enter-receipt-internal columns))

