(ns intuition.ports.jira
  (:require [intuition.components.http :refer [request]]
            [intuition.support :refer [allseq join]]
            [promesa.core :as p]))

(defn- search-page
  ([http config] (search-page http config 0))
  ([http {:jira/keys [url username password jql]} start-at]
   (request http
            {:method      "POST"
             :as          :clj
             :url         (join url "rest/api/3/search")
             :credentials [username password]
             :body        {:jql          jql
                           :expand       ["changelog"]
                           :fields       ["summary" "status" "assignee" "labels"
                                          "reporter" "project" "resolution"
                                          "resolutiondate" "updated" "created"
                                          "key" "statuscategorychangedate"
                                          "priority" "duedate" "issuetype"
                                          "*all"]
                           :fieldsByKeys true
                           :maxResults   100
                           :startAt      start-at}})))

(defn search
  "Performs a paginated search for Jira issues based on the provided configuration.
  
  Parameters:
  - `http`: An HTTP client instance used to make requests.
  - `config`: A map containing Jira configuration settings, including URL, username, password, and JQL query.

  Returns:
  A promise that resolves to a concatenated list of all issues found across all pages."
  [http config]
  (p/let [{:keys [issues total maxResults]} (search-page http config)
          page-count (.floor js/Math (/ total maxResults))
          pages      (map #(* % maxResults) (range 1 (inc page-count)))]
    (p/->> pages
           (partition-all 10)
           (map (fn [chapter]
                  (p/do (p/delay (:jira/delay config))
                        (p/all (map #(search-page http config %) chapter)))))
           (allseq)
           (flatten)
           (mapcat :issues)
           (concat issues))))
