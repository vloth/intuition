(ns intuition.controller 
  (:require [intuition.ports.jenkins :as jenkins]))

(defn upsert-jenkins-builds
  [{:keys [config http]}]
  (jenkins/get-builds http config))
