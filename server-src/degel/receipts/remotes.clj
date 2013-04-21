(ns degel.receipts.remotes
  (:require [compojure.handler :refer [site]]
            [shoreleave.middleware.rpc :refer [defremote wrap-rpc]]
            [degel.receipts.server :refer [handler]]
            [degel.receipts.receipts :refer [format-receipt]]))

(defremote fill-paid-by [country]
  "v9949")

(defremote save-receipt [paid-by date amount category vendor comments for-whom]
  (str "REMOTE: " (format-receipt paid-by date amount category vendor comments for-whom)))

(def app (-> (var handler)
             (wrap-rpc)
             (site)))

