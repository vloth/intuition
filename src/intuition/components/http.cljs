(ns intuition.components.http
  (:require ["util" :refer [format]]
            [clojure.string :as str]
            [intuition.log :as l :refer [log]]
            [intuition.support :refer [base-64]]
            [promesa.core :as p]
            [test-support :refer [new-id]]))

(defn- auth-header
  [[username password]]
  (->> (format "%s:%s" username password)
       (.from js/Buffer)
       base-64
       (format "Basic %s")))

(defn- ->error
  [type error response]
  {:error-type type :response response :error error})

(defn- coerce-clj
  [response]
  (-> (p/-> response .json (js->clj :keywordize-keys true))
      (p/then (fn [clj] {:body clj :status (.-status response)}))
      (p/catch #(->error :response-not-json % response))))

(defn- coerce-text
  [response]
  (-> (p/-> response .text)
      (p/then (fn [text] {:body text :status (.-status response)}))
      (p/catch #(->error :response-not-text % response))))

(defn- error-message
  [{:keys [response error]} {:keys [method url]}]
  (->> [(format "[HTTP ERROR]\n%s %s failed" method url)
        (when response
          (format "\n%s: %s" (.-statusText response) (.-status response)))
        (when error (format "\n%s" (.-message error))) "."]
       (filter some?)
       (apply str)))

(defn- fail? [response] (= :request-error (:error-type response)))
(defn- too-many? [response] (= (.-status response) 429))
(defn- ok? [response] (.-ok response))

(defn- error? [result] (:error-type result))

(defn- safe-fetch
  [fetch-args]
  (-> (apply js/fetch fetch-args)
      (p/catch #(->error :request-error % nil))))

(defn- retry
  [fetch-fn config arguments]
  (p/do (p/delay (:http/delay-420 config) (fetch-fn config arguments))))

(defn- ->fetch-args
  [{:keys [url method body credentials]}]
  [url
   (clj->js {:method  method
             :body    (some->> body clj->js (.stringify js/JSON))
             :headers {:Authorization (auth-header credentials)
                       :Accept        "application/json"
                       :Content-Type  "application/json"}})])

(defn- fetch
  [config arguments]
  (p/let [response (safe-fetch (->fetch-args arguments))]
    (cond (fail? response)          response
          (too-many? response)      (retry fetch config arguments)
          (not (ok? response))      (->error :response-not-ok nil response)
          (= :clj (:as arguments))  (coerce-clj response)
          (= :text (:as arguments)) (coerce-text response)

          :else
          (->error :response-unknown-coercion nil response))))

(defn explode-url
  [url]
  (let [url-obj  (js/URL. url)
        base-url (str (.-origin url-obj) (.-pathname url-obj))
        params   (js/URLSearchParams. (.-search url-obj))]
    {:base-url     (str base-url)
     :query-params (when-not (str/blank? params) params)}))

(defn log-result
  [id {:keys [url method]} {:keys [status]} duration]
  (let [{:keys [base-url query-params]} (explode-url url)]
    (log {:msg "%1\t\nmethod=%2\nurl=%3\nparams=%4\nstatus=%5\nduration=%6ms\n"
          :args [id method base-url (or query-params "none") status duration]
          :color {id         l/id-color
                  "method"   l/bold-style
                  "status"   l/bold-style
                  "url"      l/bold-style
                  "params"   l/bold-style
                  "duration" l/bold-style}})))

(defn new-http
  [config]
  (fn [fetch-args]
    (p/let [id         (format "[%s]" (new-id))
            start-time (.now js/Date)
            result     (fetch config fetch-args)
            end-time   (.now js/Date)
            duration   (- end-time start-time)]
      (log-result id fetch-args result duration)
      (:body result))))

(defn new-mock-http
  [spec]
  (fn [args]
    (let [url (:url args)]
      (if-let [response (get @spec url)]
        response
        (->error :response-not-found nil args)))))

(defn request
  [http http-args]
  (p/let [result (http http-args)]
    (if (error? result)
      (throw (ex-info (error-message result http-args)
                      (assoc result :args http-args)))
      result)))
