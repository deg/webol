(ns degel.receipts.server
  (:gen-class)
  (:require [clojure.java.io :as io]
            [compojure.core :refer [defroutes GET]]
            [compojure.route :refer [resources not-found]]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.util.response :refer [redirect]]
            [shoreleave.middleware.rpc :refer [wrap-rpc defremote]]
            [net.cgrand.enlive-html :as enlive]
            [compojure.handler :refer [site]]
            [cemerick.austin.repls :refer (browser-connected-repl-js)]
            [degel.cljutil.devutils :as dev]
            [degel.webol.parser :refer [parse-line]]
            [degel.receipts.simpleDB :refer [put-record get-record nuke-db]]
            [degel.receipts.receipts :refer [collect-receipt-history enter-receipt-internal]]))


(defremote fill-receipt-history [password]
  (remove nil? (collect-receipt-history password)))


(defremote enter-receipt [columns]
  (enter-receipt-internal columns))


(defremote write-storage [key value user-id password]
  (let [columns {:password password
                 :uid (str key)
                 :user-id user-id
                 :value value}]
    (put-record "User-data" columns)))


(defremote read-storage [key user-id password]
  (get-record "User-data" key :value password))


(defremote get-parse-tree [line]
  (parse-line line true))


(defn init-db [password]
  (nuke-db password)
  (write-storage :PaidBy-options (str ["Cash" "Ck (# in comment)" "v0223" "v5760" "v9949" "Other"])
                 nil
                 password))


(def repl-env
  (reset! cemerick.austin.repls/browser-repl-env (cemerick.austin/repl-env)))

(enlive/deftemplate webol-page
  (io/resource "public/webol.html")
  []
  [:body] (enlive/append
            (enlive/html [:script (browser-connected-repl-js)])))


(defroutes app-routes
  (GET "/" {:keys [server-name] :as all-keys}
    (cond (re-matches #"(?i).*receipt.*" server-name) (redirect "/new-receipt.html")
          (re-matches #"(?i).*webol.*"   server-name) (webol-page)
          true (not-found "<h1>David moans: 'app not found'.</h1>")))
  ; to serve static pages saved in resources/public directory
  (resources "/")
  (not-found "<h1>David moans: 'page not found'.</h1>"))


(def app (-> app-routes wrap-rpc site))


(defn -main [& [port]]
  (let [port (Integer. (or port (System/getenv "PORT") 3000))]
    (defonce ^:private server
      (run-jetty #'app {:port port :join? false}))
    server))
