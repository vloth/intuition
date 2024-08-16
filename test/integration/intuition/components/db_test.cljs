(ns integration.intuition.components.db-test
  (:require [clojure.test :refer [is]]
            [intuition.components.db :refer [exec halt-db new-db]]
            [promesa.core :as p]
            [test-support :refer [deftest-async]]))

(deftest-async component-db
  (p/let [db        (new-db {:db/path ":memory:"})
          js-result (exec db "SELECT 42 as out")
          result    (js->clj js-result :keywordize-keys true)]
    (p/do (halt-db db)
          (is (= [{:out 42}] result)))))
