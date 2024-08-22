(ns unit.intuition.adapter-test
  (:require [cljs.test :refer [is deftest testing]]
            [intuition.adapter :as adapter]))

(deftest test->config
  (testing "->config function"
    (is
      (= {:db/path               "path/to/db"
          :task/type             "type"
          :task/source           "source"
          :http/delay-429        429
          :jenkins/url           "http://jenkins.url"
          :jenkins/username      "jenkins-user"
          :jenkins/password      "jenkins-pass"
          :jenkins/job-path      "job/path"
          :jenkins/delay          500
          :git/repository        "repo"
          :git/branch            "branch"
          :git/remote            "remote"
          :git/pull?             true
          :jira/url              "http://jira.url"
          :jira/username         "jira-user"
          :jira/password         "jira-pass"
          :jira/jql              "jql"
          :bitbucket/url         "http://bitbucket.url"
          :bitbucket/username    "bitbucket-user"
          :bitbucket/password    "bitbucket-pass"
          :bitbucket/repo-slug   "repo-slug"
          :bitbucket/filter-from (js/Date. "2023-01-01T00:00:00Z")
          :bitbucket/past-months  -6}
         (adapter/->config
           {:db-path               "path/to/db"
            :task-type             "type"
            :task-source           "source"
            :http-delay-429        "429"
            :jenkins-url           "http://jenkins.url"
            :jenkins-delay          "500"
            :jenkins-username      "jenkins-user"
            :jenkins-password      "jenkins-pass"
            :jenkins-job-path      "job/path"
            :git-repository        "repo"
            :git-branch            "branch"
            :git-remote            "remote"
            :git-pull              "true"
            :jira-url              "http://jira.url"
            :jira-username         "jira-user"
            :jira-password         "jira-pass"
            :jira-jql              "jql"
            :bitbucket-url         "http://bitbucket.url"
            :bitbucket-username    "bitbucket-user"
            :bitbucket-password    "bitbucket-pass"
            :bitbucket-repo-slug   "repo-slug"
            :bitbucket-filter-from "2023-01-01T00:00:00Z"
            :bitbucket-past-months "-6"})))))
