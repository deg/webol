(ns degel.receipts.remotes
  (:require [compojure.handler :refer [site]]
            [shoreleave.middleware.rpc :refer [defremote wrap-rpc]]
            [degel.receipts.server :refer [app-routes]]
            [degel.receipts.receipts :refer [format-receipt]]))

(defremote fill-paid-by [country]
  ["Cash" "Ck #" "other" "v0223" "v5760" "v9949"])

(def app (-> app-routes wrap-rpc site))


