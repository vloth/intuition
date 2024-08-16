(ns unit.intuition.support-test
  (:require [cljs.test :refer [deftest is are]]
            [intuition.support :refer [->json allseq base-64 join parse-int]]
            [promesa.core :as p]
            [test-support :refer [deftest-async]]))

(deftest-async test-allseq-normal
  (-> (allseq [(p/resolved 1) (p/resolved 2) (p/resolved 3)])
      (p/then (fn [results] (is (= results [1 2 3]))))))

(deftest-async test-allseq-empty
  (-> (allseq [])
      (p/then (fn [results]
                (is (= results []))))))

(deftest-async test-allseq-order
  (-> (allseq [(p/delay 100 1) (p/delay 50 2) (p/delay 10 3)])
      (p/then (fn [results]
                (is (= results [1 2 3]))))))

(deftest test->json
  (are [js-class-instance expected]
       (let [result (->json js-class-instance)]
         (is (= expected (js->clj result :keywordize-keys true))))
    (js-obj "a" 1 "b" 2) {:a 1 :b 2}
    (js-obj) {}
    (js-obj "a" 1 "b" (js-obj "c" 3)) {:a 1 :b {:c 3}}))

(deftest test-parse-int
  (are [input expected]
       (is (= (parse-int input) expected))
    "42" 42
    "0" 0
    "-42" -42))

(deftest test-parse-int-nan
  (is (js/isNaN (parse-int "not a number"))))

(deftest test-base-64
  (are [input expected] (is (= (base-64 (js/Buffer.from input)) expected))
    "hello"   "aGVsbG8="
    ""        ""
    "Clojure" "Q2xvanVyZQ=="))

(deftest test-join
  (are [paths expected]
       (is (= (apply join paths) expected))
    ["path" "to" "file"] "path/to/file"
    ["path/" "/to" "/file"] "path/to/file"
    ["http://" "example.com" "/path"] "http://example.com/path"
    ["path" "?query=1"] "path?query=1"))
