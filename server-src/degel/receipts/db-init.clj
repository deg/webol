(ns degel.receipts.db_init)

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


(def category-Dogs-options
  ["Chayot HaBayit" "Dod Moshe" "Vet"])


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


(def category-Gift-options
  ["Devarim Yafim"
   "Kangaroo"
   "Kfar HaShaashuim"
   "Magnolia"
   "Tachshit Plus"])


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


(def category-Tax-options
  ["Meches" "Philip Stein"])
