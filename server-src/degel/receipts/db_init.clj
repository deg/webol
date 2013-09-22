(ns degel.receipts.db-init)

;; [TODO] {FogBugz:138} This is ugly.  Store these in a text (or json or xml) file.

(def paid-by-options
  ["Cash"
   ["v9949" "H Shufersal"]
   ["v9457-5760" "H UBank"]
   ["v0223" "D Shufersal"]
   ["v3732" "D UBank"]
   ["v5692" "AARP -5692"]
   ["v9835" "Fid Debit -9835"]
   ["mc5331" "Fid MC -5331"]])


(def for-whom-options
  [["D" "David"]
   ["H" "Heidi"]
   ["A" "Aviva"]
   ["S" "Shoshana"]
   ["Degel" "Degel"]
   ["HBA" "HBA"]
   ["Netzach" "Netzach Menashe"]])


(def category-options
  ["Books"
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


(def category-Books-options
  ["Masada" "Sefarim v'od" "Steimatzky" "Tzomet Sefarim"])


(def category-Car-options
  ["Parking" "Paz" "Puncheria Yossi" "Subaru"])


(def category-Charity-options
  ["Beit Hagalgalim"
   "Deaf group"
   "Hakol Lashulchan"
   "Ima Ani Re'ev"
   "Mouth or foot painting artists"
   "Swim4Sadna"])


(def category-Cleaning-options
  ["Levana"])


(def category-Clothing-options
  ["200 meter fashion"
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
   "Zara"])


(def category-Dogs-options
  ["Chayot HaBayit" "Dod Moshe" "Vet"])


(def category-Entertainment-options
  ["Al Derech Burma"
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
   "Yamit 2000"])


(def category-Food-options
  ["Alonit"
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
   "Supersol Deal"])


(def category-Garden-options
  ["Beit Shemesh"
   "Hapina Hayeroka"
   "Mishtelet Habayit"
   "Nursery Chatzer Hamashtela"
   "Richard's Flower Shop"])


(def category-Gift-options
  ["Devarim Yafim"
   "Kangaroo"
   "Kfar HaShaashuim"
   "Magnolia"
   "Tachshit Plus"])


(def category-Health-options
  ["Arthur's Pharmacy"
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
   "Terem"])


(def category-Home-options
  ["Ashley Coleman"
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
   "Yossi Zadiki"])


(def category-Jewelry-options
  ["Magnolia"
   "Tachshit plus"])


(def category-Kids-options
  ["Beit Shemesh"
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
   "Red Pirate"])


(def category-Restaurant-options
     ["Aldo"
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
      "Tom's Place"])


(def category-Tax-options
  ["Meches" "Philip Stein"])


(def category-Travel-options
  ["Bus" "Taxi" "Train"])
