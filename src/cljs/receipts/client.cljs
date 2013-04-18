(ns hello-clojurescript)

(defn handle-click []
  (js/alert "Hello!"))

(def clickable (.getElementById js/document "clickable"))
(.addEventListener clickable "click" handle-click)


;; define the function to be attached to form submission event
(defn validate-form []
  (let [paid-by (.getElementById js/document "PaidBy")
        amount (.getElementById js/document "Amount")]
    (if (and (> (count (.-value paid-by)) 0)
             (> (count (.-value amount)) 0))
      true
      (do (js/alert "Please, complete the form!")
          false))))

;; define the function to attach validate-form to onsubmit event of
;; the form
(defn init []
  ;; verify that js/document exists and that it has a getElementById
  ;; property
  (if (and js/document
           (.-getElementById js/document))
    ;; get loginForm by element id and set its onsubmit property to
    ;; our validate-form function
    (let [login-form (.getElementById js/document "newReceipt")]
      (set! (.-onsubmit login-form) validate-form))))

;; initialize the HTML page in unobtrusive way
(set! (.-onload js/window) init)
