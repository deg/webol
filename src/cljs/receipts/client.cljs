(ns degel.receipts.cljs.client
  (:use [domina :only [by-id value set-value!]]))

(defn handle-click []
  (js/alert "Hello!"))

(def clickable (.getElementById js/document "clickable"))
(.addEventListener clickable "click" handle-click)


;; define the function to be attached to form submission event
(defn validate-form []
  (let [paid-by (by-id "PaidBy")
        amount (by-id "Amount")]
    (set-value! (by-id "ForWhom") "DASH")
    (if (and (> (count (value paid-by)) 0)
             (> (count (value amount)) 0))
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
    ;; get loginForm by element id and set its onsubmit property to
    ;; our validate-form function
    (let [login-form (.getElementById js/document "newReceipt")]
      (set! (.-onsubmit login-form) validate-form))))
