(ns integration.intuition.controller-test
  (:require [cljs.test :refer [is use-fixtures]]
            [integration.aux.system :as s]
            [intuition.controller :as controller]
            [promesa.core :as p]
            [test-support :refer [deftest-async]]))

(def config
  {:db/path          ":memory:"
   :task/source      "example"
   :jenkins/url      "http://jenkins.com"
   :jenkins/delay    0
   :jenkins/job-path "job/example"})

(use-fixtures :once
  {:before (s/start-system config)
   :after  s/halt-system})

(use-fixtures :each {:before s/reset-system})

(deftest-async
 test-upsert-jenkins-builds
  (s/mock-http 
    {"http://jenkins.com/job/example/api/json"
     {:builds [{:url "http://jenkins.com/job/example/1"}
               {:url "http://jenkins.com/job/example/2"}]}

     "http://jenkins.com/job/example/1/api/json"
     {:number     1
      :duration   10
      :result     "SUCCESS"
      :changeSets [{:items [{:commitId 1} {:commitId 2}]}]}

     "http://jenkins.com/job/example/2/api/json"
     {:number 2 :duration 20 :result "FAIL" :changeSets []}})

  (p/let [_ (s/call controller/upsert-jenkins-builds)
          db-state (s/query-db "select * from jenkins")]
    (is (= [{:source   "example"
             :id       1
             :duration 10
             :result   "SUCCESS"
             :commits  ["1" "2"]}
            {:source "example"
             :id 2
             :duration 20
             :result "FAIL"
             :commits []}]
           db-state))))
