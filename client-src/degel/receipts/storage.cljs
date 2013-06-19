(ns degel.receipts.storage
  (:require [cljs.reader :refer [read-string]] ;; [TODO] Is clojure.edn available in cljs?
            [domina :refer [log]]
            [shoreleave.remotes.http-rpc :refer [remote-callback]]
            [degel.receipts.db :as db]))

;;; Use local storage as a cache backed by server-side storage.
;;;
;;; Read requests do the following:
;;; - Async return of the cached value
;;; - Optionally, fire off a read request to the server
;;; - When value returns from server
;;;   - Store in local storage
;;;   - Second aysnc return of value
;;;
;;; Write requests do the following:
;;; - Store in local storage
;;; - Optionally fire off a request to store on server
;;; - When server returns success or failure, async return of status


(def storage (.-localStorage js/window))


(defn- wrap-value
  "[TODO] Doc TBD"
  [value local-only?]
  {:value value :local-only? local-only?})


(defn- write-wrapped-local
  [key wrapped-value]
  (.setItem storage key (pr-str wrapped-value))
  {:status db/SUCCESS})


(defn- read-wrapped-local
  "[TODO] Doc TBD"
  [key]
  (when-let [wrapped-value (.getItem storage key)]
    (read-string wrapped-value)))


(defn write-local
  "[TODO] Doc TBD"
  [key value]
  (let [{:keys [local-only?] :as existing} (read-wrapped-local key)]
    (assert (or (not existing) local-only?)
            (str "'" key "' is stored remotely; can't write local-only copy."))
    (write-wrapped-local key (wrap-value value true))))


(defn read
  "[TODO] Doc TBD"
  [key read-fn & {:keys [fail-fn]}]
  (let [{:keys [value local-only?]} (read-wrapped-local key)]
    (when read-fn
      (read-fn value :local))
    (when (not local-only?)
      (let [user-id (read :user-id nil)
            password (read :password nil)]
        (remote-callback :read-storage [key user-id password]
          #(if (= (:status %) db/SUCCESS)
             (let [remote-value (read-string (:value %))]
               (write-wrapped-local key (wrap-value remote-value false))
               (when read-fn
                 (read-fn remote-value :remote)))
             ((or fail-fn js/alert) (:errmsg %))))))
    value))


(defn write
  "Write a value to local storage and to the server.
  The value will be keyed by a vector of the current user-id and the supplied key.
  remote-callback-fn will be called with a map including :status, :errmsg, and/or :uid."
  [key value remote-callback-fn]
  (write-wrapped-local key (wrap-value value false))
  (let [user-id (read :user-id nil)
        password (read :password nil)]
    (remote-callback :write-storage [key value user-id password]
      #(remote-callback-fn %))))
