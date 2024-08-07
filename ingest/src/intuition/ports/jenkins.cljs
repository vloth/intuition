(ns intuition.ports.jenkins
  (:require [intuition.components.http :refer [request]]
            [intuition.support :refer [join allseq]]
            [promesa.core :as p]))

(defn- ->api [& parts] (apply join (concat parts ["api/json"])))

(defn- get-build
  [http credentials job-path]
  (request http {:method :GET :url job-path :credentials credentials}))

(defn get-builds
  [http {:jenkins/keys [url username password job-path]}]
  (p/let [credentials [username password]
          build       (get-build http credentials (->api url job-path))]
    (p/->> (:builds build)
           (map #(->api (:url %)))
           (partition-all 10)
           (map (fn [chapter]
                  (p/do (p/delay 200)
                        (p/all (map #(get-build http credentials %) chapter)))))
           (allseq)
           (flatten))))
