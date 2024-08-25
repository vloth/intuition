(ns intuition.components.http
  (:require ["util" :refer [format]]
            [intuition.support :refer [base-64]]
            [promesa.core :as p]))

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
      (p/catch #(->error :response-not-json % response))))

(defn- coerce-text
  [response]
  (-> (p/-> response .text)
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

(defn new-http [config] #(fetch config %))

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
