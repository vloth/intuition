(ns intuition.ports.jenkins
  (:require ["util" :refer [format]]
            [intuition.components.http :refer [basic-auth request]]
            [intuition.support :refer [allseq join parse-int]]
            [promesa.core :as p]))

(def ^:private runs-api
  "/runs?tree=causes[shortDescription],durationInMillis,id,result,startTime,endTime,endQueueTime,commitId")

(defn- steps-api
  [id]
  (-> "/runs/%s/steps?tree=displayDescription,displayName,durationInMillis,result,startTime,state,id,type"
      (format id)))

(defn- log-api
  [run-id step-id]
  (-> "/runs/%s/steps/%s/log"
      (format run-id step-id)))

(defn- ->api [& parts] (apply join (concat parts)))

(defn- jenkins-get-fn
  [http credentials url job-path]
  (fn [url-pargs & {:keys [as] :or {as :clj}}]
    (request http
             {:method :GET
              :as     as
              :url    (->api url job-path url-pargs)
              :auth   (basic-auth credentials)})))

(defn- agument-step-details
  [jenkins-get build step]
  (p/->> (jenkins-get (log-api (:id build) (:id step)) :as :text)
         (assoc step :output)))

(defn- agument-build-details
  [jenkins-get config build]
  (letfn [(agument-step [step] (agument-step-details jenkins-get build step))]
    (p/->> (jenkins-get (steps-api (:id build)))
           (partition-all 10)
           (map (fn [group]
                  (p/do (p/delay (:jenkins/step-delay config))
                        (p/all (map agument-step group)))))
           allseq
           flatten
           (assoc build :steps))))

(defn get-builds
  "Fetches Jenkins builds and augments them with detailed step information.
  
  Parameters:
  - `http`: An HTTP client instance used to make requests.
  - `config`: A map containing Jenkins configuration settings, including URL, username, password, job path, and delay.
  - `build-id`: An optional build ID to filter builds. Only builds with IDs greater than this value will be fetched.

  Returns:
  A promise that resolves to a list of augmented builds."
  [http {:jenkins/keys [url username password job-path delay] :as config}
   build-id]
  (p/let [jenkins-get (jenkins-get-fn http [username password] url job-path)]
    (p/->> (jenkins-get runs-api)
           (filter #(or (nil? build-id) (> (parse-int (:id %)) build-id)))
           (partition-all 10)
           (map (fn [builds-page]
                  (p/do (p/delay delay)
                        (allseq
                          (map #(agument-build-details jenkins-get config %)
                            builds-page)))))
           allseq
           flatten)))
