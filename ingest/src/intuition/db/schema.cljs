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
    );

    CREATE SCHEMA IF NOT EXISTS git;

    CREATE TABLE IF NOT EXISTS
      git.commit(
        hash VARCHAR(250) PRIMARY KEY,
        author_email VARCHAR(50),
        author_name VARCHAR(50),
        source VARCHAR(250),
        date DATETIME,
        email VARCHAR(250),
        message VARCHAR(250),
        body VARCHAR(1000),
        changed_files VARCHAR(500)[]
      );

    CREATE TABLE IF NOT EXISTS
      git.tag(
        tag VARCHAR(250),
        hash VARCHAR(250),
        source VARCHAR(250),
        PRIMARY KEY (tag, hash, source)
      );")
