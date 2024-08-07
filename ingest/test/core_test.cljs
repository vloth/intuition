(ns core-test
  (:require [cljs.test :refer [deftest is]]
            [core :refer [sum]]))

(deftest test-sum
  (is (= (sum 1 1)
         2)))
