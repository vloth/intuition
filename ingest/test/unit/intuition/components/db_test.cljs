(ns unit.intuition.components.db-test
  (:require [aux.async-test :refer [deftest-async]]
            [clojure.test :refer [is]]
            [intuition.components.db :refer [halt-db new-db]]
            [promesa.core :as p]))

(defn- query [{:keys [exec]} instruction] (exec instruction))

(deftest-async test-async-add
  (p/let [db        (new-db {:db-path ":memory:"})
          js-result (query db "SELECT 42 as out")
          result    (js->clj js-result :keywordize-keys true)]
    (p/do (halt-db db)
          (is (= [{:out 42}] result)))))
