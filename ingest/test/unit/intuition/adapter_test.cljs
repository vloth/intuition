(ns unit.intuition.adapter-test
  (:require [cljs.test :refer [is deftest testing]]
            [intuition.adapter :as adapter]))

(deftest test->config
  (testing "->config function with all keywords"
    (is
      (= {:db/path          "path/to/db"
          :task/type        "type"
          :task/source      "source"
          :http/delay-429   100
          :jenkins/url      "http://jenkins.url"
          :jenkins/username "user"
          :jenkins/password "pass"
          :jenkins/job-path "job/path"
          :git/repository   "repo"
          :git/branch       "branch"
          :git/remote       "remote"
          :git/pull?        true}
         (adapter/->config {:db-path          "path/to/db"
                            :task-type        "type"
                            :task-source      "source"
                            :delay-429        "100"
                            :jenkins-url      "http://jenkins.url"
                            :jenkins-username "user"
                            :jenkins-password "pass"
                            :jenkins-job-path "job/path"
                            :git-repository   "repo"
                            :git-branch       "branch"
                            :git-remote       "remote"
                            :git-pull         "true"})))))
