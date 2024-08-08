(ns unit.intuition.components.config-test
  (:require [cljs.test :refer [deftest are]]
            [intuition.components.config :refer [new-config]]))

(deftest create-new-config
  (are [expected env extra filter-keys] (= expected
                                           (new-config env extra filter-keys))
    {:a "a"} {} {:a "a"} []
    {:a "a"} {:a :b} {:a "a"} []
    {:a "a"} {:a "a" :c :d} {} [:a]
    {:foo-bar "999"} {:FOO_BAR "999"} {} [:FOO_BAR]))
