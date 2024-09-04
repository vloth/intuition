(ns integration.intuition.controller.jenkins-test
  (:require [aux.db :as aux.db]
            [aux.system :as s]
            [aux.test :refer [deftest-async]]
            [cljs.test :refer [use-fixtures]]
            [intuition.controller :as controller]))

(def config
  {:db/path          ":memory:"
   :task/source      "example"
   :jenkins/url      "http://jenkins.com"
   :jenkins/delay    0
   :jenkins/job-path "job/example"})

(use-fixtures :once {:before (s/start-system config) :after s/halt-system})
(use-fixtures :each {:after s/reset-system})

(deftest-async test-upsert-empty-list
 (s/mock-http {#"http://jenkins.com/job/example/runs\?" []})
 (controller/upsert-jenkins-builds @s/system-atom)
 (aux.db/assert= "select * from jenkins" []))

(deftest-async test-upsert-jenkins-builds
  (s/mock-http {#"http://jenkins.com/job/example/runs\?"
                [{:id               "1"
                  :durationInMillis 10
                  :result           "SUCCESS"
                  :startTime        "2020-01-01T00:00:00Z"
                  :endTime          "2020-01-01T00:00:10Z"
                  :causes           [{:shortDescription "commit 1"}]
                  :commitId         "1"
                  :steps            []}]

                #"http://jenkins.com/job/example/runs/1/steps\?"
                []})
  (controller/upsert-jenkins-builds @s/system-atom)
  (aux.db/assert= "select * from jenkins"
                  [{:cause      "commit 1"
                    :start_time #inst "2020-01-01T00:00:00.000-00:00"
                    :steps      []
                    :duration   10
                    :source     "example"
                    :end_time   #inst "2020-01-01T00:00:10.000-00:00"
                    :result     "SUCCESS"
                    :id         1
                    :commit     "1"}]))
