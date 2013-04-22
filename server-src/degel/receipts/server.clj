(ns degel.receipts.server
  (:require [compojure.core :refer [defroutes GET POST]]
            [compojure.route :refer [resources not-found]]
            [ring.util.response :refer [redirect]]
            [compojure.handler :refer [site]]
            [degel.receipts.receipts :refer [enter-receipt]]))

(defroutes app-routes
  (GET "/" [] (redirect "/new-receipt.html"))
  (POST "/enterReceipt" [PaidBy Date Amount Category Vendor Comments ForWhom]
        (enter-receipt {:paid-by PaidBy
                        :date Date
                        :amount Amount
                        :category Category
                        :vendor Vendor
                        :comments Comments
                        :for-whom ForWhom}))
  ; to serve static pages saved in resources/public directory
  (resources "/")
  ; if page is not found
  #_(not-found "<h1>Page not found.</h1>"))

(def handler
  (site app-routes))


