(ns intuition.adapter)

(defn ->plain-js 
  "Converts a Javascript class instace into a plain object."
  [js-object]
  (.assign js/Object #js {} js-object))

(defn args->config
  "Returns a clojure map from a javascript args object.
  See https://nodejs.org/api/util.html#utilparseargsconfig for more info."
  [js-args]
  (-> (.-values js-args)
      (->plain-js)
      (js->clj :keywordize-keys true)))
