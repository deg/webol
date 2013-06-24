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


(defn- write-wrapped-local
  "We store values locally wrapped in a map that includes the value and a tag indicating
   if it should be backed to the server or be stored only locally."
  [key value local-only?]
  (.setItem storage key (pr-str {:value value :local-only? local-only?}))
  {:status db/SUCCESS})


(defn- read-wrapped-local
  "Unwrap a locally-stored value and return the value itself."
  [key]
  (when-let [wrapped-value (.getItem storage key)]
    (read-string wrapped-value)))


(defn read
  "Read a value. This function always returns the locally-stored value (or nil if there is none).
   In addition, if read-fn is supplied, it is called up to twice. First it is called with the local
   result (this is the same value returned from this function. We call read-fn just for symmetry with
   the remote result).
   Later, asynchronously, read-fn is called again with the remote value, and the local store is
   automatically updated with this value.
   The first parameter passed to read-fn is the value. The second parameter is :local or :remote,
   indicating the data source of the supplied value."
  [key read-fn & {:keys [fail-fn]}]
  (let [{:keys [value local-only?]} (read-wrapped-local key)]
    (when read-fn
      (read-fn value :local))
    (when-not local-only?
      (let [user-id (:value (read-wrapped-local :user-id))
            password (:value (read-wrapped-local :password))]
        (remote-callback :read-storage [key user-id password]
          #(if (= (:status %) db/SUCCESS)
             (let [remote-value (read-string (:value %))]
               (write-wrapped-local key remote-value false)
               (when read-fn
                 (read-fn remote-value :remote)))
             ((or fail-fn js/alert) (:errmsg %))))))
    value))


(defn write-local
  "Store a value locally only. It is an error to do so if the value has already been stored remotely."
  [key value]
  (let [{:keys [local-only?] :as local-wrapper} (read-wrapped-local key)]
    (assert (or (not local-wrapper) local-only?)
            (str "'" key "' is stored remotely; can't write local-only copy."))
    (write-wrapped-local key value true)))


(defn write
  "Write a value to local storage and to the server.
  The value will be keyed by a vector of the current user-id and the supplied key.
  remote-callback-fn will be called with a map including :status, :errmsg, and/or :uid."
  [key value remote-callback-fn]
  (write-wrapped-local key value false)
  (let [user-id (read :user-id nil)
        password (read :password nil)]
    (remote-callback :write-storage [key value user-id password]
      #(remote-callback-fn %))))
