(ns intuition.ports.bitbucket
  (:require [intuition.components.http :refer [request]]
            [intuition.support :refer [allseq join]]
            [promesa.core :as p]))

(defn- pullrequest-url
  [{:bitbucket/keys [url repo-slug]} page]
  (join url
        repo-slug
        "pullrequests"
        "?state=OPEN&state=MERGED&state=DECLINED&state=SUPERSEDED&pagelen=50"
        (str "&page=" page)))

(defn- activity-url
  [{:bitbucket/keys [url repo-slug]} pull-request-id]
  (join url repo-slug "pullrequests" pull-request-id "activity?pagelen=50"))

(defn- get-bb
  ([http {:bitbucket/keys [username password]} url]
   (request http {:method "GET" :url url :as :clj :credentials [username password]})))

(defn- get-activity
  [activity-url http config]
  (p/let [curr       (get-bb http config activity-url)
          activities (:values curr)]
    (p/loop [curr       curr
             activities activities]
      (if-let [next-page (:next curr)]
        (p/do (p/delay (:bitbucket/activity-delay config))
              (p/let [next-activity (get-bb http config next-page)]
                (p/recur next-activity
                         (concat activities (:values next-activity)))))
        activities))))

(defn- get-bare-pullrequests
  [http config]
  (p/let [{:keys [size values pagelen]} (get-bb http config (pullrequest-url config 1))
          page-count (.ceil js/Math (/ size pagelen))
          pages      (range 2 (inc page-count))]
    (p/->> pages
           (partition-all 10)
           (map (fn [chapter]
                  (p/do (p/delay (:bitbucket/delay config))
                        (p/all (map #(get-bb http config (pullrequest-url config %))
                                 chapter)))))
           (allseq)
           (flatten)
           (mapcat :values)
           (concat values))))

(defn- scoped
  [pull-requests filter-from]
  (if filter-from
    (filter #(>= (js/Date. (:created_on %)) filter-from) pull-requests)
    pull-requests))

(defn get-pull-requests
  "Fetches all pull requests from the specified Bitbucket repository and includes their activities.

  Parameters:
  - `http`: An HTTP client instance used to make requests.
  - `config`: A map containing Bitbucket configuration settings, including URL, username, password, and repository slug.
  - `filter-from`: An optional date to filter pull requests from.

  Returns:
  A promise that resolves to a list of pull requests with their associated activities."
  [http config filter-from]
  (p/let [pull-requests (get-bare-pullrequests http config)
          pr-in-scope   (scoped pull-requests filter-from)]
    (p/->> pr-in-scope
           (partition-all 10)
           (map (fn [chapter]
                  (p/all (map #(p/-> (activity-url config (:id %))
                                     (get-activity http config)
                                     ((fn [activities]
                                        (assoc % :activity activities))))
                           chapter))))
           allseq
           flatten)))
