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
      );")
