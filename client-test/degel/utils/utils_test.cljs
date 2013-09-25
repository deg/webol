(ns degel.utils.utils-test
  (:require-macros [cemerick.cljs.test :refer (is deftest with-test run-tests testing)])
  (:require [cemerick.cljs.test :as t]))

(deftest dummy
  (is (= 1 1))
  (is (= 1 2)))

