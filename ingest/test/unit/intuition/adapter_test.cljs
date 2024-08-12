(ns unit.intuition.adapter-test
  (:require [cljs.test :refer [is deftest]]
            [intuition.adapter :as adapter]))

(deftest transform->config
  (is (= {:db/path          "db/path"
          :task/type        "task/type"
          :task/source      "task/source"
          :http/delay-420   999
          :jenkins/url      "jenkins/url"
          :jenkins/username "jenkins/username"
          :jenkins/password "jenkins/password"
          :jenkins/job-path "jenkins/job-path"}
         (adapter/->config
           {:db-path          "db/path"
            :task-type        "task/type"
            :task-source      "task/source"
            :delay-429        "999"
            :jenkins-url      "jenkins/url"
            :jenkins-username "jenkins/username"
            :jenkins-password "jenkins/password"
            :jenkins-job-path "jenkins/job-path"}))))

