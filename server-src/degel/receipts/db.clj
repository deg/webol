(ns degel.receipts.db
  (:require [cemerick.rummage :as sdb]
            [cemerick.rummage.encoding :as enc]))
(def aws-key "AKIAI2SJMRRD53FHKY3Q")
(defn assemble-aws-secret [password]
  ;; Reminder: (MAYNARD (ROOT 1654)) or https://portal.aws.amazon.com/gp/aws/securityCredentials
  (str "PqoUYiVHA" (get password 0)
       "pRhV00HZ7YZ" (get password 1)
       "Zc7ZF" (get password 2)
       "3JCt" (get password 3)
       "ECEnPdc"))


(def the-client (atom nil))
(def the-config (atom nil))
(defn create-client
  "Connect to DB and validate the password. Return [success error-message]"
  [password]
  (if (nil? @the-config)
    (let [client (sdb/create-client aws-key (assemble-aws-secret password))
          config (assoc enc/keyword-strings :client client)]
      ;; I think this is the cheapest operation that will test the password
      (try (do (sdb/query config '{select count from Receipts})
               (reset! the-client client)
               (reset! the-config config)
               [true "New connection"])
           (catch com.amazonaws.AmazonServiceException e
             [false (str "DB connection failed. Status code: "
                         (.getStatusCode e) ": "
                         (.getMessage e))])))
    [true "existing connection"]))


(defn put-record [columns]
  (let [[success errmsg] (create-client (:password columns))]
    [success (if (false? success)
               errmsg
               (let [guid (str (java.util.UUID/randomUUID))]
                 (sdb/put-attrs @the-config "Receipts"
                                (assoc (dissoc columns :password) ::sdb/id guid))
                 guid))]))

(defn get-all-records [password columns]
  (let [[success errmsg] (create-client password)]
    (if (false? success)
      errmsg
      (map #(dissoc % ::sdb/id) (sdb/query-all @the-config `{select ~columns from Receipts})))))


(defn nuke-db [password]
  (let [[success errmsg] (create-client password)]
    (if (false? success)
      errmsg
      (do
        (sdb/delete-domain @the-client "Receipts")
        (sdb/create-domain @the-client "Receipts")))))

(defn test-db [password]
  (nuke-db password)
  (put-record {:paid-by "Visa" :date "2013-04-13"
               :currency "NIS" :amount "5.46"
               :category "Food" :Vendor "Shufersal" :comment "Shufersal online"
               :for-whom "DASH"
               :formatted "Dummy"
               :password password})
  (put-record {:paid-by "Check" :Check# "1234" :date "2013-04-28"
               :currency "USD" :amount "15.23"
               :category "Books" :Vendor "Barnes & Noble" :comment "At airport"
               :for-whom "D"
               :formatted "Dummy" ;; [TODO] Generate this at right level
               :password password})
  (map #(dissoc % :cemerick.rummage/id)
       (sdb/query-all @the-config '{select * from Receipts})))
