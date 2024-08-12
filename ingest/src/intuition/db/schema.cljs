(ns intuition.db.schema)

(def schema-def
  "CREATE TABLE IF NOT EXISTS 
    jenkins(
      source VARCHAR(50),
      id INT,
      duration INT,
      result VARCHAR(50),
      commits VARCHAR(40)[],
      PRIMARY KEY (source, id)
    );")
