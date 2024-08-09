(ns intuition.adapter 
  (:require [intuition.support :refer [parse-int]]))

(defn ->config
  [c]
  {:db/path          (:db-path c)
   :task/type        (:task-type c)
   :http/delay-420   (parse-int (:delay-429 c))
   :jenkins/url      (:jenkins-url c)
   :jenkins/username (:jenkins-username c)
   :jenkins/password (:jenkins-password c)
   :jenkins/job-path (:job-path c)})
