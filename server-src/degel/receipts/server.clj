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
  ;; [TODO] {FogBugz:138} This is ugly.  Store these in a text (or json or xml) file.
  (write-storage :paid-by-options
                 (str ["Cash"
                       ["v9949" "H Shufersal"]
                       ["v9457-5760" "H UBank"]
                       ["v0223" "D Shufersal"]
                       ["v3732" "D UBank"]
                       ["v5692" "AARP -5692"]
                       ["v9835" "Fid Debit -9835"]
                       ["mc5331" "Fid MC -5331"]])
                 nil password)
  (write-storage :for-whom-options
                 (str [["D" "David"]
                       ["H" "Heidi"]
                       ["A" "Aviva"]
                       ["S" "Shoshana"]
                       ["Degel" "Degel"]
                       ["HBA" "HBA"]
                       ["Netzach" "Netzach Menashe"]])
                 nil password)
  (write-storage :category-options
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
  (write-storage :category-Books-options
                 (str ["Masada" "Sefarim v'od" "Steimatzky" "Tzomet Sefarim"])
                 nil
                 password)
  (write-storage :category-Car-options
                 (str ["Parking" "Paz" "Puncheria Yossi" "Subaru"])
                 nil
                 password)
  (write-storage :category-Charity-options (str ["Beit Hagalgalim"
                                                 "Deaf group"
                                                 "Hakol Lashulchan"
                                                 "Ima Ani Re'ev"
                                                 "Mouth or foot painting artists"
                                                 "Swim4Sadna"])
                 nil password)
  (write-storage :category-Cleaning-options (str ["Levana"]) nil password)
  (write-storage :category-Clothing-options (str ["200 meter fashion"
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
  (write-storage :category-Dogs-options (str ["Chayot HaBayit" "Dod Moshe" "Vet"]) nil password)
  (write-storage :category-Entertainment-options (str ["Al Derech Burma"
                                                       "BS Chamber Music Series"
                                                       "Ba B'tov"
                                                       "Bowling Modiin"
                                                       "Cinema City"
                                                       "Country Beit Shemesh"
                                                       "Disc Club"
                                                       "Ezra"
                                                       "Globus Max"
                                                       "Mesilat Zion pool"
                                                       "Nitzanim Beach"
                                                       "Rav Chen Modiin"
                                                       "Yamit 2000"]) nil password)
  (write-storage :category-Food-options (str ["Alonit"
                                              "Angel"
                                              "Benny's"
                                              "Café Neeman"
                                              "Co-op"
                                              "Kanyon Mamtakim"
                                              "Kimat Chinam"
                                              "Mister Zol"
                                              "Naomi's Cookies"
                                              "Osher ad"
                                              "Paz Yellow"
                                              "Pommeranz"
                                              "Shufersal deal"
                                              "Shufersal online"
                                              "Super Aviv"
                                              "Super Hatzlacha"
                                              "SuperPharm"
                                              "Supersol Deal"]) nil password)
  (write-storage :category-Garden-options (str ["Beit Shemesh"
                                                "Hapina Hayeroka"
                                                "Mishtelet Habayit"
                                                "Nursery Chatzer Hamashtela"
                                                "Richard's Flower Shop"]) nil password)
  (write-storage :category-Gift-options (str ["Devarim Yafim"
                                              "Kangaroo"
                                              "Kfar HaShaashuim"
                                              "Magnolia"
                                              "Tachshit Plus"]) nil password)
  (write-storage :category-Health-options (str ["Arthur's Pharmacy"
                                                "Chaya Shames"
                                                "Dr. Metz"
                                                "Hadassa SHaRaP"
                                                "Hamashbir LeTzarchan"
                                                "Meuchedet RBS drugstore"
                                                "Meuchedet"
                                                "Meuhedet drugstore"
                                                "Optika Ayin Tov"
                                                "Optika Halperin"
                                                "Optika Speed"
                                                "Optiko Petel-Natali"
                                                "Pharma shaul"
                                                "Shaarei Tzedek"
                                                "Superpharm"
                                                "Terem"]) nil password)
  (write-storage :category-Home-options (str ["Ashley Coleman"
                                              "Ben Harush"
                                              "Big Deal"
                                              "Buy2"
                                              "Chana Weiss"
                                              "City Shop"
                                              "Cobbler"
                                              "Devarim Yafim"
                                              "Ehud Exterminator"
                                              "Graphos"
                                              "Gris"
                                              "Hakol Labait RBS"
                                              "Hakol lashulchan"
                                              "Hamashbir LaTzarchan"
                                              "Hidur"
                                              "Keter"
                                              "Kol Bo Yehezkel"
                                              "Kolbo Ohel Zahav"
                                              "Lighting store"
                                              "Limor Zakuto"
                                              "M S Photos"
                                              "Machsanei Hakol LaShulchan"
                                              "Marvadim"
                                              "Masada"
                                              "Office Depot"
                                              "Post Office"
                                              "Reuven Fabrics"
                                              "Sefarim v'od (R R Shivuk)"
                                              "Super Binyan"
                                              "Tachshit Plus"
                                              "Tza'atzu'ei Hagiva"
                                              "Yossi Zadiki"]) nil password)
  (write-storage :category-Jewelry-options (str ["Magnolia" "Tachshit plus"]) nil password)
  (write-storage :category-Kids-options (str ["Beit Shemesh"
                                              "Bnei Akiva"
                                              "Conservatory"
                                              "Ezra"
                                              "Library"
                                              "Matnas"
                                              "Moadon Sport RBS"
                                              "Naomi Ocean"
                                              "Pelech"
                                              "S'farim V'od"
                                              "Devarim Yafim"
                                              "HaPirate HaAdom"
                                              "Kangaroo"
                                              "Kfar HaShaashuim"
                                              "Red Pirate"]) nil password)
  (write-storage :category-Restaurant-options (str ["Aldo"
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
  (write-storage :category-Tax-options (str ["Meches" "Philip Stein"]) nil password)
  (write-storage :category-Travel-options (str ["Bus" "Taxi" "Train"]) nil password))


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

