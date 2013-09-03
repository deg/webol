(ns degel.webol.parser-test
  (:require [clojure.test :refer :all]
            [degel.webol.parser :refer :all]))

(defn- wrap [expr]
  {:status :success :parse expr})

(deftest case-checks
  (testing "upper"
    (is (= (parse-line "PRINT 42") (wrap [:print-cmd [:arith 42]]))))
  (testing "lower"
    (is (= (parse-line "print 42") (wrap [:print-cmd [:arith 42]]))))
  (testing "mixed"
    (is (= (parse-line "pRiNt 42") (wrap [:print-cmd [:arith 42]])))))

(deftest white-check
  (testing "before"
    (is (= (parse-line " run 10") (wrap [:run-cmd [:line-num 10]]))))
  (testing "after"
    (is (= (parse-line "run 10  ") (wrap [:run-cmd [:line-num 10]]))))
  (testing "both"
    (is (= (parse-line "  run 10 ") (wrap [:run-cmd [:line-num 10]]))))
  (testing "neither"
    (is (= (parse-line "run 10") (wrap [:run-cmd [:line-num 10]])))))

(deftest print-checks
  (let [parses [["\"OneWord\"" "OneWord"]
                ["\"Hello world!\"" "Hello world!"]
                ["\"With ' single-quote\"" "With ' single-quote"]
                ["\"With \"\" double-quote\"" "With \"\" double-quote"]
                ["\"With \"\"several\"\" double-quote\"" "With \"\"several\"\" double-quote"]
                ["3" [:arith 3]]
                ["35" [:arith 35]]
                ["0" [:arith 0]]
                ["4.7" [:arith 4.7]]
                ["0.3" [:arith 0.3]]
                [".756" [:arith 0.756]]
                ["3+5" [:arith [:add 3 5]]]
                ["3+7*x" [:arith [:add 3 [:mul 7 [:var "x"]]]]]
                ["5.07*(x/.7)" [:arith [:mul 5.07 [:div [:var "x"] 0.7]]]]]]
    (doseq [[in parse] parses]
      (let [test (str "print " in)]
        (testing test
          (is (= (parse-line test) (wrap [:print-cmd parse]))))))
    (doseq [[in parse] parses]
      (let [test (str "print " in " " in " " in)]
        (testing (str "multi-" test)
          (is (= (parse-line test) (wrap [:print-cmd parse parse parse]))))))
    ;; [TODO] {FogBugz:137} Relatively expensive test, and not terribly interesting. Maybe delete
    (doall (for [[in1 parse1] parses
                 [in2 parse2] parses]
      (let [test (str "print " in1 " " in2)]
        (testing test
          (is (= (parse-line test)
                 (wrap [:print-cmd parse1 parse2])))))))))


(deftest immediate-cmds
  (testing "edit"
    (is (= (parse-line "edit 20") (wrap [:edit-cmd [:line-num 20]]))))
  (testing "help"
    (is (= (parse-line "help") (wrap [:help-cmd]))))
  (testing "renumber all"
    (is (= (parse-line "renumber") (wrap [:renumber-cmd]))))
  (testing "renumber start"
    (is (= (parse-line "renumber") (wrap [:renumber-cmd]))))
  (testing "renumber by"
    (is (= (parse-line "renumber 20") (wrap [:renumber-cmd [:line-num 20]])))))
