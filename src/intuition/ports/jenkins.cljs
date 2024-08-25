(ns intuition.ports.jenkins
  (:require [intuition.components.http :refer [request]]
            [intuition.support :refer [allseq join]]
            [promesa.core :as p]))

(def runs-suffix
  "/runs?tree=causes[shortDescription],durationInMillis,id,result,startTime,endTime,endQueueTime,commitId")

(defn steps-suffix
  [run-id]
  (str
    "/runs/"
    run-id
    "/steps?tree=displayDescription,displayName,durationInMillis,result,startTime,state,id,type"))

(defn output-suffix
  [run-id step-id]
  (str "/runs/" run-id "/steps/" step-id "/log"))

(defn- ->api [& parts] (apply join (concat parts)))

(defn- jenkins-get-fn
  [http credentials url job-path]
  (fn [url-pargs & {:keys [as] :or {as :clj}}]
    (prn "MAKING REQUEST" url-pargs)
    (request http
             {:method      :GET
              :as          as
              :url         (->api url job-path url-pargs)
              :credentials credentials})))

(defn agument-step-details
  [jenkins-get build step]
  (p/let [log (jenkins-get (output-suffix (:id build) (:id step)) :as :text)]
    (assoc step :output log)))

(defn agument-build-details
  [jenkins-get config build]
  (p/let [steps        (jenkins-get (steps-suffix (:id build)))
          step-details (p/->> (partition-all 10 steps)
                              (map (fn [sub-steps]
                                     (p/do (p/delay (:jenkins/step-delay
                                                      config))
                                           (p/all (map #(agument-step-details
                                                          jenkins-get
                                                          build
                                                          %)
                                                    sub-steps)))))
                              (allseq)
                              (flatten))]
    (assoc build :steps step-details)))

(defn get-builds
  [http {:jenkins/keys [url username password job-path delay] :as config}]
  (p/let [jenkins-get (jenkins-get-fn http [username password] url job-path)]
    (p/->> (jenkins-get runs-suffix)
           (partition-all 10)
           (map (fn [builds-page]
                  (p/do (p/delay delay)
                        (allseq
                          (map #(agument-build-details jenkins-get config %)
                            builds-page)))))
           (allseq)
           (flatten))))
