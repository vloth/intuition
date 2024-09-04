(ns integration.intuition.controller-test
  (:require [cljs.test :refer [is use-fixtures]]
            [integration.aux.system :as s]
            [intuition.controller :as controller]
            [intuition.ports.git :as git]
            [intuition.support :refer [mkdir]]
            [promesa.core :as p]
            [test-support :refer [deftest-async with-redef-async]]))

(def config
  {:db/path          ":memory:"
   :task/source      "example"
   :jenkins/url      "http://jenkins.com"
   :jenkins/delay    0
   :jenkins/job-path "job/example"})

(use-fixtures :once {:before (s/start-system config) :after s/halt-system})
(use-fixtures :each {:after s/reset-system})

(deftest-async
  test-upsert-empty-list
  (s/mock-http {#"http://jenkins.com/job/example/runs\?" []})
  (with-redef-async
    [git/get-tags (constantly "") mkdir (constantly nil) git/get-commits
     (constantly (constantly [{:all []}]))]
    (p/let [_ (s/call controller/upsert-jenkins-builds)
            _ (s/call controller/upsert-git-tags)
            _ (s/call controller/upsert-git-commits)
            jenkins-db-state     (s/query-db "select * from jenkins")
            git-tags-db-state    (s/query-db "select * from git.tag")
            git-commits-db-state (s/query-db "select * from git.commit")]
      (is (and (= [] jenkins-db-state)
               (= [] git-tags-db-state)
               (= [] git-commits-db-state))))))

(deftest-async 
  test-upsert-jenkins-builds
  (s/mock-http 
    {#"http://jenkins.com/job/example/runs\?"
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
  (p/let [_ (s/call controller/upsert-jenkins-builds)
          db-state (s/query-db "select * from jenkins")]
    (is (= [{:cause      "commit 1"
             :start_time #inst "2020-01-01T00:00:00.000-00:00"
             :steps      []
             :duration   10
             :source     "example"
             :end_time   #inst "2020-01-01T00:00:10.000-00:00"
             :result     "SUCCESS"
             :id         1
             :commit     "1"}]
          db-state))))


(deftest-async test-upsert-git-tags
  (with-redef-async [git/get-tags (constantly "hash1\trefs/tags/v1.0\nhash2\trefs/tags/v1.1\nhash3\trefs/tags/v2.0")]
    (p/let [_ (s/call controller/upsert-git-tags)
            db-state (s/query-db "select * from git.tag")]
      (is (= [{:tag "v1.0" :hash "hash1" :source "example"}
              {:tag "v1.1" :hash "hash2" :source "example"}
              {:tag "v2.0" :hash "hash3" :source "example"}]
             db-state)))))
