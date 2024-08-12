(ns intuition.adapter 
  (:require [intuition.support :refer [parse-int]]))

(defn ->config
  [c]
  {:db/path          (:db-path c)
   :task/type        (:task-type c)
   :task/source      (:task-source c)
   :http/delay-420   (parse-int (:delay-429 c))
   :jenkins/url      (:jenkins-url c)
   :jenkins/username (:jenkins-username c)
   :jenkins/password (:jenkins-password c)
   :jenkins/job-path (:jenkins-job-path c)})

(defn ->build
  [{:task/keys [source]} jenkins-build]
  {:source   source
   :id       (:number jenkins-build)
   :duration (:duration jenkins-build)
   :result   (:result jenkins-build)
   :commits  (->> jenkins-build
                  :changeSets
                  (map :items)
                  (flatten)
                  (map :commitId))})
