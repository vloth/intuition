(ns unit.intuition.adapter-test
  (:require [aux.cli :refer [make-cli-args]]
            [cljs.test :refer [are deftest]]
            [intuition.adapter :as adapter]))

(deftest transform-args->config
  (are [x y]
       (= x (adapter/args->config (make-cli-args {:foo :boolean :bar :string} y)))
    {:foo true :bar "123"} "--foo --bar=123"
    {:bar "123"} "--bar=123"
    {} ""))
