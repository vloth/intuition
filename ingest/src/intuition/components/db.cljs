(ns intuition.components.db
  (:require ["duckdb$default" :as duckdb]
            ["util" :refer [format promisify]]
            [clojure.string :as s]
            [promesa.core :as p]))

(defn new-db
  [{:db/keys [path]}]
  (let [db   (duckdb/Database. path)
        conn (.connect db)]
    {:db   db
     :conn conn
     :all (-> (.-all conn)
              (promisify)
              (.bind conn))}))

(defn halt-db
  [{:keys [db conn]}]
  (p/do
    (.close conn)
    (.close db)))

(defn exec [{:keys [all]} instruction] (all instruction))

(defn- format-struct
  [struct escape]
  (->> struct
       (map (fn [[k v]] (format "%s: %s" (name k) (escape v))))
       (s/join ", ")))

(defn- escape
  [item]
  (cond (nil? item)    "NULL"
        (string? item) (format "'%s'" (s/replace item #"'" "''"))
        (inst? item)   (format "'%s'" (.toISOString item))
        (map? item)    (format "{%s}" (format-struct item escape))
        :else          item))

(defn- prepare-item
  [item]
  (cond
    (sequential? item) (->> (map escape item) (s/join ",") (format "[%s]"))
    :else              (escape item)))

(defn prepare-stmt
  [columns]
  (format "(%s)"
          (->> columns
               (map prepare-item)
               (s/join ","))))

(defn insert
  [db table columns {:keys [on-conflict]} data]
  (when (not-empty data)
    (->> (format "insert %s into %s(%s) values %s"
                 (cond (= :ignore on-conflict)  "or ignore"
                       (= :replace on-conflict) "or replace"
                       :else                    "")
                 table
                 (->> columns
                      (map name)
                      (s/join ","))
                 (->> data
                      (map (comp prepare-stmt (apply juxt columns)))
                      (s/join ",")))
         (exec db))))

(defn delete
  [db table columns data]
  (->> (format "delete from %s where %s"
               table
               (->> data
                    (map #(select-keys % columns))
                    (map (fn [x]
                           (->> x
                                (map (fn [[k v]]
                                       (format "%s=%s" (name k) (escape v))))
                                (s/join " AND "))))
                    (s/join " OR ")))
       (exec db)))
