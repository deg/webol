(ns degel.utils.utils-test
  (:require-macros [cemerick.cljs.test :refer (is deftest with-test run-tests testing)])
  (:require [cemerick.cljs.test :as t]
            [degel.utils.utils :as utils]))

(deftest test-date-string
  (is (= (utils/date-string (js/Date. "September 27, 2013, 12:00")) "2013-09-27"))
  (is (= (utils/date-string (js/Date. "December 31, 2013, 23:59")) "2013-12-31"))
  (is (= (utils/date-string (js/Date. "January 1, 2014, 00:01")) "2014-01-01")))


(deftest test-read-string-or-nil
  (is (= (utils/read-string-or-nil nil) nil))
  (is (= (utils/read-string-or-nil "") nil))
  (is (= (utils/read-string-or-nil "123") 123))
  (is (= (utils/read-string-or-nil "(+ 1 2)") '(+ 1 2))))
