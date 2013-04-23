(ns degel.receipts.db
  (:require [cemerick.rummage :as sdb]
            [cemerick.rummage.encoding :as enc]))
(def aws-key "AKIAI2SJMRRD53FHKY3Q")
(defn assemble-aws-secret [pwd]
  ;; Reminder: (MAYNARD (ROOT 1654)) or https://portal.aws.amazon.com/gp/aws/securityCredentials
  (str "PqoUYiVHA" (get pwd 0)
       "pRhV00HZ7YZ" (get pwd 1)
       "Zc7ZF" (get pwd 2)
       "3JCt" (get pwd 3)
       "ECEnPdc"))


(def the-client (atom nil))
(def the-config (atom nil))
(defn create-client [pwd]
  (reset! the-client (sdb/create-client aws-key (assemble-aws-secret pwd)))
  (reset! the-config (assoc enc/all-strings :client @the-client)))


(defn put-record [columns]
  (when (nil? @the-config)
    (create-client (:password columns)))
  (let [guid (str (java.util.UUID/randomUUID))]
    (sdb/put-attrs @the-config "Receipts"
                   (assoc (dissoc columns :password) ::sdb/id guid))
    guid))


(defn nuke-db [pwd]
  (create-client pwd)
  (sdb/delete-domain @the-client "Receipts")
  (sdb/create-domain @the-client "Receipts"))

(defn test-db [pwd]
  (nuke-db pwd)
  (put-record :paid-by "Visa" :date "2013-04-13"
              :currency "NIS" :amount "5.46"
              :category "Food" :Vendor "Shufersal" :comments "Shufersal online"
              :for-whom "DASH")
  (put-record :paid-by "Check" :Check# "1234" :date "2013-04-28"
              :currency "USD" :amount "15.23"
              :category "Books" :Vendor "Barnes & Noble" :comments "At airport"
              :for-whom "D")
  (map #(dissoc % :cemerick.rummage/id)
       (sdb/query-all @the-config '{select * from Receipts})))
