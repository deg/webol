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
            [degel.receipts.db-init :as db-init]
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
  (write-storage :paid-by-options (str db-init/paid-by-options) nil password)
  (write-storage :for-whom-options (str db-init/for-whom-options) nil password)
  (write-storage :category-options (str db-init/category-options) nil password)
  (write-storage :category-Books-options (str db-init/category-Books-options) nil password)
  (write-storage :category-Car-options (str db-init/category-Car-options) nil password)
  (write-storage :category-Charity-options (str db-init/category-Charity-options) nil password)
  (write-storage :category-Cleaning-options (str db-init/category-Cleaning-options) nil password)
  (write-storage :category-Clothing-options (str db-init/category-Clothing-options) nil password)
  (write-storage :category-Dogs-options (str db-init/category-Dogs-options) nil password)
  (write-storage :category-Entertainment-options (str db-init/category-Entertainment-options) nil password)
  (write-storage :category-Food-options (str db-init/category-Food-options) nil password)
  (write-storage :category-Garden-options (str db-init/category-Garden-options) nil password)
  (write-storage :category-Gift-options (str db-init/category-Gift-options) nil password)
  (write-storage :category-Health-options (str db-init/category-Health-options) nil password)
  (write-storage :category-Home-options (str db-init/category-Home-options) nil password)
  (write-storage :category-Jewelry-options (str db-init/category-Jewelry-options) nil password)
  (write-storage :category-Kids-options (str db-init/category-Kids-options) nil password)
  (write-storage :category-Restaurant-options (str db-init/category-Restaurant-options) nil password)
  (write-storage :category-Tax-options (str db-init/category-Tax-options) nil password)
  (write-storage :category-Travel-options (str db-init/category-Travel-options) nil password))


(def repl-env
  (reset! cemerick.austin.repls/browser-repl-env (cemerick.austin/repl-env)))

(enlive/deftemplate webol-dev-page
  (io/resource "public/webol-dev.html")
  []
  [:body] (enlive/append
            (enlive/html [:script (browser-connected-repl-js)])))

(defroutes app-routes
  (GET "/" {:keys [server-name] :as all-keys}
    (cond (re-matches #"(?i).*receipt.*" server-name) (redirect "/new-receipt.html")
          (re-matches #"(?i).*webol-dev.*"   server-name) (webol-dev-page)
          (re-matches #"(?i).*webol.*" server-name) (redirect "/webol.html")
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


(defn start-cljs-repl []
  (-main)
  (cemerick.austin.repls/cljs-repl repl-env))

