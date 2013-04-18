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

;; define the function to attach validate-form to onsubmit event of
;; the form
(defn ^:export init []
  ;; verify that js/document exists and that it has a getElementById
  ;; property
  (if (and js/document
           (.-getElementById js/document))
    (ev/listen! (dom/by-id "submit") :click validate-form)))
