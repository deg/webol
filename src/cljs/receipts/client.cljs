(ns degel.receipts.cljs.client
  (:require [domina :as dom]
            [domina.events :as ev]))

(defn handle-click []
  (js/alert "Hello!"))

(def clickable (.getElementById js/document "clickable"))
(.addEventListener clickable "click" handle-click)


;; define the function to be attached to form submission event
(defn validate-form []
  (let [paid-by (dom/value (dom/by-id "PaidBy"))
        amount (dom/value (dom/by-id "Amount"))]
    (dom/set-value! (dom/by-id "ForWhom") "DASH")
    (if (and (> (count paid-by) 0)
             (> (count amount) 0))
      true
      (do (js/alert "Please complete the form!")
          false))))

(defn add-help []
  (dom/append! (dom/by-id "newReceipt")
               "<div class='help'>Help me please</div>"))

(defn remove-help []
  (dom/destroy! (dom/by-class "help")))

;; define the function to attach validate-form to onsubmit event of
;; the form
(defn ^:export init []
  ;; verify that js/document exists and that it has a getElementById
  ;; property
  (when (and js/document
           (.-getElementById js/document))
    (ev/listen! (dom/by-id "submit") :click validate-form)
    (ev/listen! (dom/by-id "submit") :mouseover add-help)
    (ev/listen! (dom/by-id "submit") :mouseout remove-help)
    (ev/listen! (dom/by-id "Date") :mouseover add-help)
    (ev/listen! (dom/by-id "Date") :mouseout remove-help)))
