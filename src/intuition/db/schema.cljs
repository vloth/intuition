(ns intuition.db.schema 
  (:require [intuition.components.db :refer [exec]]))

(def ^:private schema-def
  "CREATE TABLE IF NOT EXISTS 
    jenkins(
      source VARCHAR(50),
      id INT,
      duration INT,
      result VARCHAR(50),
      start_time TIMESTAMP,
      end_time TIMESTAMP,
      cause VARCHAR(250),
      commit VARCHAR(40),
      steps STRUCT(
        id INT,
        start_time TIMESTAMP,
        name VARCHAR(250),
        description VARCHAR(250),
        duration INT,
        output VARCHAR(1000),
        state VARCHAR(50),
        result VARCHAR(50),
        type VARCHAR(50)
      )[],
      PRIMARY KEY (source, id)
    );

   CREATE TABLE IF NOT EXISTS 
     bitbucket(
       repo_slug VARCHAR(50),
       id SMALLINT,
       title VARCHAR(250),
       description VARCHAR(1000),
       summary VARCHAR(1000),
       state VARCHAR(50),
       author VARCHAR(50),
       closed_by VARCHAR(50),
       commit VARCHAR(50),
       comment_count SMALLINT,
       created TIMESTAMP,
       updated TIMESTAMP,
       destination VARCHAR(50),
       merged TIMESTAMP,
       opened TIMESTAMP,
       PRIMARY KEY (repo_slug, id)
    );

    CREATE TABLE IF NOT EXISTS
      jira(
        key VARCHAR(50) PRIMARY KEY,
        project VARCHAR(50),
        type VARCHAR(50),
        summary VARCHAR(500),
        assignee_name VARCHAR(250),
        assignee_email VARCHAR(250),
        reporter_name VARCHAR(250),
        reporter_email VARCHAR(250),
        priority VARCHAR(50),
        status VARCHAR(50),
        status_category VARCHAR(50),
        status_category_changed DATETIME,
        resolution VARCHAR(50),
        resolution_datetime DATETIME,
        created DATETIME,
        updated DATETIME,
        duedate DATETIME,
        labels VARCHAR(50)[],
        history STRUCT(
          name VARCHAR(250), 
          email VARCHAR(250), 
          created DATETIME, 
          from_status VARCHAR(50), 
          to_status VARCHAR (50)
        )[]
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
      );
      
    CREATE TABLE IF NOT EXISTS
      github(
        owner VARCHAR(50),
        repository VARCHAR(50),
        id INT,
        title VARCHAR(250),
        body VARCHAR(1000),
        state VARCHAR(50),
        author VARCHAR(50),
        created TIMESTAMP,
        updated TIMESTAMP,
        PRIMARY KEY (owner, repository, id)
      );")

(defn sync-schema [db]
  (exec db schema-def))
