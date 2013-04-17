(ns receipts.server
  (:use compojure.core)
  (:require [ring.util.response :as resp]
            [compojure.handler :as handler]
            [compojure.route :as route]))

(defroutes app-routes
  ; to serve document root address
  (GET "/" [] (resp/redirect "/help.html"))
  ; to serve static pages saved in resources/public directory
  (route/resources "/")
  ; if page is not found
  #_(route/not-found "<h1>Page not found.</h1>"))

(def handler
  (handler/site app-routes))


