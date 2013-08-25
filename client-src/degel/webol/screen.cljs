(ns degel.webol.screen
  (:require [clojure.string :as str]
            [domina :as dom :refer [log]]
            [degel.webol.store :as store]))



(defn- on-screen [fcn]
  (let [canvas (dom/by-id "sketchboard")
        context (.getContext canvas "2d")
        width (.-width canvas)
        height (.-height canvas)]
    (fcn {:canvas canvas :context context :width width :height height})))


(defn blank-line []
  (clojure.string/join
   (repeat (store/fetch [:screen :width-in-chars]) " ")))


(defn text-mode []
  (on-screen (fn [{:keys [context width height]}]
               (let [char-height 13
                     line-height (+ char-height 2)]
                 (set! (.-textBaseline context) "top")
                 (set! (.-font context) (str char-height "px Monospace"))
                 (let [char-width (-> context (.measureText "M") .-width)
                       width-in-chars (fix (/ width char-width))
                       height-in-lines (fix (/ height line-height))]
                   (store/put! [:screen :mode] :text)
                   (store/put! [:screen :width-in-chars] width-in-chars)
                   (store/put! [:screen :height-in-lines] height-in-lines)
                   (store/put! [:screen :text-x] 0)
                   (store/put! [:screen :text-y] 0)
                   (store/alert! [:screen :line] :text-draw-line
                                 (fn [_ [_ _ line] _ text]
                                   (set! (.-fillStyle context) "BurlyWood")
                                   (.fillRect context 0 (* line line-height) width line-height)
                                   (set! (.-fillStyle context) "DarkBlue")
                                   (.fillText context text 0 (* line line-height))))
                   (doseq [n (range height-in-lines)]
                     (store/put! [:screen :line n] (blank-line))))))))


(defn text-scroll
  ([]
     (text-scroll 1))
  ([num-lines]
     (when (= (store/fetch [:screen :mode]) :text)
       (let [height-in-lines (store/fetch [:screen :height-in-lines])]
         (doseq [n (rest (range height-in-lines))]
           (store/put! [:screen :line (dec n)]
                       (store/fetch [:screen :line n])))
         (store/put! [:screen :line (dec height-in-lines)] (blank-line))))))


(defn- string-into [s n s1]
  (str (subs s 0 n) s1 (subs s (+ n (count s1)))))


(defn newline-out []
  (when (= (store/fetch [:screen :mode]) :text)
    (store/put! [:screen :text-x] 0)
    (let [new-y (store/update! [:screen :text-y] inc)]
      (when (>= new-y (store/fetch [:screen :height-in-lines]))
        (text-scroll 1)
        (store/update! [:screen :text-y] dec)))))


(defn text-out [text]
  (when (= :text (store/fetch [:screen :mode]))
    (loop [[line & rest] (str/split-lines text)]
      (let [start-x (store/fetch [:screen :text-x])
            start-y (store/fetch [:screen :text-y])]
        (store/update! [:screen :text-x] + (count line))
        (store/update! [:screen :line start-y] string-into start-x line))
      (when (seq rest)
        (newline-out)
        (recur rest)))))
