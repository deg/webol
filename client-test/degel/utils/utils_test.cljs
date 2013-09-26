(ns degel.utils.utils-test
  (:require-macros [cemerick.cljs.test :refer (is deftest with-test run-tests testing)])
  (:require [cemerick.cljs.test :as t]
            [degel.utils.utils :as utils]))

(deftest test-read-string-or-nil
  (is (= (utils/read-string-or-nil nil) nil))
  (is (= (utils/read-string-or-nil "") nil))
  (is (= (utils/read-string-or-nil "123") 123))
  (is (= (utils/read-string-or-nil "(+ 1 2)") '(+ 1 2))))
