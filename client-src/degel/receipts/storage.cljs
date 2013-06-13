(ns degel.receipts.storage
  (:require [cljs.reader :refer [read-string]] ;; [TODO] Is clojure.edn available in cljs
            [domina :refer [log]]
            [shoreleave.remotes.http-rpc :refer [remote-callback]]))

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
  [value remotable?]
  {:value value :remotable? remotable?})


(defn- write-wrapped-local
  [key wrapped-value]
  (.setItem storage key (pr-str wrapped-value)))


(defn- read-wrapped-local
  "[TODO] Doc TBD"
  [key]
  (when-let [wrapped-value (.getItem storage key)]
    (read-string wrapped-value)))


(defn write-local
  "[TODO] Doc TBD"
  [key value]
  (let [{:keys [remotable?]} (.getItem storage key)]
    (assert (not remotable?)
            (str "'" key "' is stored remotely; can't write local-only copy."))
    (write-wrapped-local key (wrap-value value false))))


(defn write
  "Write a value to local storage and to the server.
  The value will be keyed by a vector of the current user-id and the supplied key.
  remote-callback-fn will be called with a vector of either
   [:failure error-message]
  or
   [:success key value]"
  [key value remote-callback-fn]
  (write-wrapped-local key (wrap-value value true))
  (let [user-id (.getItem storage :user-id)
        password (.getItem storage :password)]
    (remote-callback :write-storage [key value user-id password]
      (fn [[success? errmsg]]
        (remote-callback-fn (if success?
                              [:success key value]
                              [:failure errmsg]))))))

(defn read
  "[TODO] Doc TBD"
  [key read-fn & {:keys [fail-fn]}]
  (let [{:keys [value remotable?]} (read-wrapped-local key)]
    (when read-fn
      (read-fn value :local))
    (when remotable?
      (let [user-id (.getItem storage :user-id)
            password (.getItem storage :password)]
        (remote-callback :read-storage [key user-id password]
          #(let [[success? errmsg-or-value] %]
             (if success?
               (when read-fn
                 (read-fn errmsg-or-value :remote))
               ((or fail-fn js/alert) errmsg-or-value))))))
    value))
