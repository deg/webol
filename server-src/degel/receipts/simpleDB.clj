(ns degel.receipts.simpleDB
  (:require [cemerick.rummage :as sdb]
            [cemerick.rummage.encoding :as enc]
            [degel.cljutil.devutils :as dev]))
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

(defn- form-errmsg [e title]
  (str title ": " (.getStatusCode e) ": " (.getMessage e)))

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
             [false (form-errmsg e "DB connection failed. Status code: ")])))
    [true "existing connection"]))


(defn put-record [columns]
  (let [[success errmsg] (create-client (:password columns))]
    [success (if (false? success)
               errmsg
               (let [uid (or (:uid columns) (str (java.util.UUID/randomUUID)))]
                 (sdb/put-attrs @the-config "Receipts"
                                (assoc (dissoc columns :password :uid)
                                  ::sdb/id uid))
                 uid))]))

(defn put-user-data-record [key value user-id password]
  (let [[success errmsg] (create-client password)]
    (if (false? success)
      [false errmsg]
      (try [true (sdb/put-attrs @the-config "User-data"
                                {::sdb/id [user-id key] :value value})]
           (catch com.amazonaws.AmazonServiceException e
             [false  (form-errmsg e "DB put failed. Status code")])))))


(defn get-user-data-record
  "[TODO] Doc TBD"
  [key user-id password]
  (let [[success errmsg] (create-client password)]
    (if (false? success)
      [false errmsg]
      (try [true (->> `{select [:value] from User-data limit 1 where (= ::sdb/id [~user-id ~key])}
                      (sdb/query @the-config)
                      (map :value)
                      first)]
           (catch com.amazonaws.AmazonServiceException e
             [false (form-errmsg e "DB get failed. Status code")])))))

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
        (sdb/delete-domain @the-client "User-data")
        (sdb/create-domain @the-client "User-data")
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
