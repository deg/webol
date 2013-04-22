(ns degel.receipts.remotes
  (:require [compojure.handler :refer [site]]
            [shoreleave.middleware.rpc :refer [defremote wrap-rpc]]
            [degel.receipts.server :refer [handler]]
            [degel.receipts.receipts :refer [format-receipt]]))

(defremote fill-paid-by [country]
  "v9949")

(def app (-> (var handler)
             (wrap-rpc)
             (site)))

