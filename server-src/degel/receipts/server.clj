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
  ;; [TODO] This is ugly.  Store these in a text (or json or xml) file.
  (write-storage :PaidBy-options
                 (str ["Cash"
                       ["v9949" "H Shufersal"]
                       ["v9457-5760" "H UBank"]
                       ["v0223" "D Shufersal"]
                       ["v3732" "D UBank"]
                       ["v5692" "AARP -5692"]
                       ["v9835" "Fid Debit -9835"]
                       ["mc5331" "Fid MC -5331"]])
                 nil password)
  (write-storage :ForWhom-options
                 (str [["D" "David"]
                       ["H" "Heidi"]
                       ["A" "Aviva"]
                       ["S" "Shoshana"]
                       ["Degel" "Degel"]
                       ["HBA" "HBA"]
                       ["Netzach" "Netzach Menashe"]])
                 nil password)
  (write-storage :Category-options
                 (str ["Books"
                       "Car"
                       "Charity"
                       "Cleaning"
                       "Clothing"
                       "Dogs"
                       "Entertainment"
                       "Food"
                       "Garden"
                       "Gift"
                       "Health"
                       "Home"
                       "Jewelry"
                       "Kids"
                       "Restaurant"
                       "Tax"
                       "Travel"])
                 nil password)
  (write-storage :Category-Books-options
                 (str ["Sefarim v'od"	"Steimatzky" "Tzomet Sefarim"])
                 nil
                 password)
  (write-storage :Category-Car-options
                 (str ["Parking" "Paz" "Puncheria Yossi" "Subaru"])
                 nil
                 password)
  (write-storage :Category-Charity-options (str ["Beit Hagalgalim"
                                                 "Deaf group"
                                                 "Hakol Lashulchan"
                                                 "Ima Ani Re'ev"
                                                 "Mouth or foot painting artists"
                                                 "Swim4Sadna"])
                 nil password)
  (write-storage :Category-Cleaning-options (str ["..."]) nil password)
  (write-storage :Category-Clothing-options (str ["200 meter fashion"
                                                  "Amnon shoes"
                                                  "Autenti"
                                                  "Bazaar Strauss"
                                                  "Clarks Shoes"
                                                  "Dry cleaner"
                                                  "fox"
                                                  "Glik"
                                                  "H&O"
                                                  "Hamachsan"
                                                  "Hige IQ"
                                                  "Kosher Casual"
                                                  "Lili Shoes"
                                                  "Lior kids clothing"
                                                  "Macbeset Hagiva"
                                                  "Matias Matar Club Ofna"
                                                  "Matok fashion"
                                                  "Mekimi"
                                                  "Olam Habeged"
                                                  "Onyx Shoes"
                                                  "Pinuk"
                                                  "Ricochet"
                                                  "Shop Center"
                                                  "Sisna fashion"
                                                  "Super Shoe"
                                                  "Tamnoon"
                                                  "Zara"]) nil password)
  (write-storage :Category-Dogs-options (str ["Chayot HaBayit" "Dod Moshe" "Vet"]) nil password)
  (write-storage :Category-Entertainment-options (str ["..."]) nil password)
  (write-storage :Category-Food-options (str ["..."]) nil password)
  (write-storage :Category-Garden-options (str ["..."]) nil password)
  (write-storage :Category-Gift-options (str ["..."]) nil password)
  (write-storage :Category-Health-options (str ["..."]) nil password)
  (write-storage :Category-Home-options (str ["..."]) nil password)
  (write-storage :Category-Jewelry-options (str ["..."]) nil password)
  (write-storage :Category-Kids-options (str ["..."]) nil password)
  (write-storage :Category-Restaurant-options (str ["Aldo"
                                                    "Aroma"
                                                    "Big Apple Pizza"
                                                    "Burger's Bar"
                                                    "Café Bagels"
                                                    "Café Café"
                                                    "Café Neeman"
                                                    "Canela"
                                                    "Chalav u'dvash"
                                                    "El Baryo"
                                                    "Felafal Ahava"
                                                    "Gachalim"
                                                    "Japan-Japan"
                                                    "Levi's Pizza"
                                                    "McDonalds"
                                                    "Marvad hakesmim"
                                                    "Mercaz HaPizza"
                                                    "Olla"
                                                    "Pikansin"
                                                    "Pizza Hut"
                                                    "Sbarro"
                                                    "Shwarma Skouri"
                                                    "Tom's Place"]) nil password)
  (write-storage :Category-Tax-options (str ["..."]) nil password)
  (write-storage :Category-Travel-options (str ["Bus" "Taxi" "Train"]) nil password))


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


(defn start-cljs-repl []
  (-main)
  (cemerick.austin.repls/cljs-repl repl-env))

