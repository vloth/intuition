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

(use-fixtures :once {:before (s/start-system config) :after s/halt-system}
              :each {:after s/reset-system})

(deftest-async test-upsert-empty-list
  (s/mock-http {"http://jenkins.com/job/example/api/json" {:builds []}})
  (with-redef-async [git/get-tags    (constantly "") 
                     mkdir           (constantly nil)
                     git/get-commits (constantly  (constantly [{:all []}]))]
    (p/let [_ (s/call controller/upsert-jenkins-builds)
            _ (s/call controller/upsert-git-tags)
            _ (s/call controller/upsert-git-commits)
            jenkins-db-state     (s/query-db "select * from jenkins")
            git-tags-db-state    (s/query-db "select * from git.tag")
            git-commits-db-state (s/query-db "select * from git.commit")]
      (is (and (= [] jenkins-db-state)
               (= [] git-tags-db-state)
               (= [] git-commits-db-state))))))

(deftest-async test-upsert-jenkins-builds
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

(deftest-async test-upsert-git-tags
  (with-redef-async [git/get-tags (constantly "hash1\trefs/tags/v1.0\nhash2\trefs/tags/v1.1\nhash3\trefs/tags/v2.0")]
    (p/let [_ (s/call controller/upsert-git-tags)
            db-state (s/query-db "select * from git.tag")]
      (is (= [{:tag "v1.0" :hash "hash1" :source "example"}
              {:tag "v1.1" :hash "hash2" :source "example"}
              {:tag "v2.0" :hash "hash3" :source "example"}]
             db-state)))))
