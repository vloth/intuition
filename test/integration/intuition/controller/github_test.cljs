(ns integration.intuition.controller.github-test
  (:require [aux.db :as aux.db]
            [aux.system :as s]
            [aux.test :refer [deftest-async]]
            [cljs.test :refer [use-fixtures]]
            [intuition.controller :as controller]))

(def config
  {:db/path           ":memory:"
   :task/source       "example"
   :github/owner      "owner"
   :github/repository "repo"})

(use-fixtures :once {:before (s/start-system config) :after s/halt-system})
(use-fixtures :each {:after s/reset-system})

(deftest-async test-upsert-empty-list
  (s/mock-http {#"https://api.github.com/repos/owner/repo/issues" []})
  (controller/upsert-github-issues @s/system-atom)
  (aux.db/assert= "select * from github" []))

(deftest-async test-upsert-github-issues
  (s/mock-http {#"https://api.github.com/repos/owner/repo/issues"
                [{:number     1
                  :title      "title"
                  :body       "body"
                  :state      "open"
                  :created_at "2020-01-01T00:00:00Z"
                  :updated_at "2020-01-01T00:00:00Z"
                  :user       {:login "author"}}]})
  (controller/upsert-github-issues @s/system-atom)
  (aux.db/assert=
   "select * from github"
   [{:id         1
     :title      "title"
     :body       "body"
     :state      "open"
     :created    #inst "2020-01-01T00:00:00.000-00:00"
     :updated    #inst "2020-01-01T00:00:00.000-00:00"
     :owner      "owner"
     :author     "author"
     :repository "repo"}]))
