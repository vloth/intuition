(ns unit.intuition.adapter-test
  (:require [cljs.test :refer [deftest is testing]]
            [intuition.adapter :as adapter]
            [intuition.support :refer [parse-int]]))

(deftest test->config
  (testing "->config function"
    (let [raw-config {:db-path                  "path/to/db"
                      :something-else           "else"
                      :task-type                "type"
                      :task-source              "source"
                      :http-delay-429           "429"
                      :jenkins-url              "http://jenkins.url"
                      :jenkins-delay            "500"
                      :jenkins-username         "jenkins-user"
                      :jenkins-password         "jenkins-pass"
                      :jenkins-job-path         "job/path"
                      :git-repository           "repo"
                      :git-branch               "branch"
                      :git-remote               "remote"
                      :git-pull                 "true"
                      :jira-url                 "http://jira.url"
                      :jira-username            "jira-user"
                      :jira-password            "jira-pass"
                      :jira-jql                 "jql"
                      :jira-delay               "3000"
                      :bitbucket-url            "http://bitbucket.url"
                      :bitbucket-username       "bitbucket-user"
                      :bitbucket-password       "bitbucket-pass"
                      :bitbucket-repo-slug      "repo-slug"
                      :bitbucket-filter-from    "2023-01-01T00:00:00Z"
                      :bitbucket-past-months    "-6"
                      :bitbucket-delay          "1000"
                      :bitbucket-activity-delay "2000"}
          expanded-config-keys (-> raw-config
                                   (dissoc :something-else)
                                   keys)]
      (is
        (= {:db/path                  (:db-path raw-config)
            :task/type                (:task-type raw-config)
            :task/source              (:task-source raw-config)
            :http/delay-429           (parse-int (:http-delay-429 raw-config))
            :jenkins/url              (:jenkins-url raw-config)
            :jenkins/username         (:jenkins-username raw-config)
            :jenkins/password         (:jenkins-password raw-config)
            :jenkins/job-path         (:jenkins-job-path raw-config)
            :jenkins/delay            (parse-int (:jenkins-delay raw-config))
            :git/repository           (:git-repository raw-config)
            :git/branch               (:git-branch raw-config)
            :git/remote               (:git-remote raw-config)
            :git/pull?                (= "true" (:git-pull raw-config))
            :jira/url                 (:jira-url raw-config)
            :jira/username            (:jira-username raw-config)
            :jira/password            (:jira-password raw-config)
            :jira/jql                 (:jira-jql raw-config)
            :jira/delay               (parse-int (:jira-delay raw-config))
            :bitbucket/url            (:bitbucket-url raw-config)
            :bitbucket/username       (:bitbucket-username raw-config)
            :bitbucket/password       (:bitbucket-password raw-config)
            :bitbucket/repo-slug      (:bitbucket-repo-slug raw-config)
            :bitbucket/delay          (parse-int (:bitbucket-delay raw-config))
            :bitbucket/activity-delay (parse-int (:bitbucket-activity-delay raw-config))
            :bitbucket/filter-from    (js/Date. (:bitbucket-filter-from raw-config))
            :bitbucket/past-months    (parse-int (:bitbucket-past-months raw-config))}
           (adapter/->config raw-config expanded-config-keys))))))
