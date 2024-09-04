(ns intuition.ports.github 
  (:require ["util" :refer [format]]
            [intuition.components.http :refer [bearer-auth request]]))

(defn get-issues 
  [http {:github/keys [owner repository token]}]
  (request http
           {:method :GET
            :auth (bearer-auth token)
            :as :clj
            :url (format "https://api.github.com/repos/%s/%s/issues" owner repository)}))
