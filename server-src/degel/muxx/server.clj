(ns degel.muxx.server
  (:require [compojure.core :refer [defroutes GET]]
            [compojure.route :refer [resources not-found]]
            [ring.util.response :refer [redirect]]
            [shoreleave.middleware.rpc :refer [wrap-rpc]]
            [net.cgrand.enlive-html :as html]
            [compojure.handler :refer [site]]
            [cemerick.austin.repls :refer (browser-connected-repl-js)]
            [degel.cljutil.devutils :as dev]))


(def app-dispatch (atom {}))

(defrecord app-data [name base-page production-js dev-js])

(defn add-app [& {:keys [name base-page production-js dev-js]}]
  (swap! app-dispatch
         assoc name (->app-data name base-page production-js dev-js)))

(defn dev-page
  ([{:keys [base-page production-js dev-js]}]
     (dev-page (str "public" base-page) production-js dev-js))
  ([page production-js dev-js]
     ((html/template page []
        [:body] (html/append (html/html [:script (browser-connected-repl-js)]))
        [[:script (html/attr= :src production-js)]] (html/set-attr :src dev-js)))))


(defn find-site-records [server-request]
   (filter (fn [{:keys [name] :as record}]
             (re-matches (re-pattern (str "(?i).*" name ".*")) server-request))
           (vals @app-dispatch)))

(defn dev-site? [server-request {:keys [name] :as record}]
  (string? (re-matches (re-pattern (str "(?i).*" name "-dev.*")) server-request)))


(defroutes app-routes
  (GET "/" {:keys [server-name] :as all-keys}
    (let [[matching-site & extra-matches] (find-site-records server-name)]
      (cond (nil? matching-site)        (not-found "<h1>David moans: 'app website not found'.</h1>")
            extra-matches               (not-found "<h1>David moans: 'Ambiguous app website URL'.</h1>")
            (dev-site? server-name
                       matching-site)   (dev-page matching-site)
            true                        (redirect (:base-page matching-site)))))
  (resources "/")   ;; to serve static pages saved in resources/public directory
  (not-found "<h1>David moans: 'page not found'.</h1>"))


(def app (-> app-routes wrap-rpc site))
