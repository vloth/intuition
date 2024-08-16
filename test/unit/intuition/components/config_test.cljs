(ns unit.intuition.components.config-test
  (:require [cljs.test :refer [deftest are]]
            [intuition.components.config :refer [new-config]]))

(deftest create-new-config
  (are [expected source] (= expected (new-config source))
    {:a "a"} {:env/data #js {:a "a"} :cli/options {} :cli/args []}
    {:a "b" :c "d" :e true} {:env/data    #js {:a "a" :c "d"}
                             :cli/args    ["--a=b" "--c=d" "--e"]
                             :cli/options {:a {:type "string"}
                                           :c {:type "string"}
                                           :e {:type "boolean"}}}))
