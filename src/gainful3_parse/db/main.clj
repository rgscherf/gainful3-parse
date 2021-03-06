(ns gainful3-parse.db.main
  (:require [clojure.java.jdbc :as sql]
            [gainful3-parse.utils.logging :as log]
            [gainful3-parse.specs.job :as job]
            [gainful3-parse.specs.utils :as spec-utils]
            [gainful3-parse.db.utils :refer :all]
            [gainful3-parse.utils.logging :as log])
  (:import (java.util Date)))

(def dbspec
  {:classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname     "db/database.db"})

(defn create-jobs-table!
  "Create the 'jobs' table. Make sure to use g.db.utils fns to convert between DB row format and :struct/jobs spec"
  []
  (->> [[:id :integer :primary :key :autoincrement]
        [:url :string :not :null]
        [:location :text :not :null]
        [:government :text :not :null]
        [:division :text :not :null]
        [:office :text]
        [:title :text :not :null]
        [:salary_min :real]
        [:salary_max :real]
        [:wage_type :text :not :null]
        [:posted_date :integer]
        [:close_date :integer]
        [:created_date :integer :not :null]]
       (sql/create-table-ddl :jobs)
       (sql/execute! dbspec)))

(defn drop-jobs-table!
  "Drop the 'jobs' table."
  []
  (sql/execute! dbspec "drop table if exists jobs"))

(defn all-jobs
  "Retrieve all jobs in DB."
  []
  (->> (sql/query dbspec "select * from jobs")
       (map dbjob->struct)
       (into #{})))

(defn all-job-urls
  []
  (into #{} (map ::job/url (all-jobs))))

(defn- filter-existing-urls
  [jobs]
  (let [all-urls (all-job-urls)]
    (reduce (fn [collected new]
              (if (all-urls (::job/url new))
                (do
                  (log/info (str "Existing job found in DB at URL " (::job/url new)))
                  collected)
                (conj collected new)))
            nil
            jobs)))

(defn insert-jobs!
  "Insert jobs into DB. Filters out non-conforming jobs, and jobs whose URLs already exist in DB."
  [jobs]
  (->> jobs
       (spec-utils/filter-for-conformance ::job/job)
       filter-existing-urls
       (map struct->dbjob)
       (sql/insert-multi! dbspec :jobs)))

(comment
  (drop-jobs-table!)
  (create-jobs-table!)
  (all-jobs)
  (let [j
        {:url         "https://google.ca"
         :location    "Victoria"
         :government  "City of Victoria"
         :division    "Planning?"
         :office      nil
         :title       "Chief Planner"
         :salary-min  90000
         :salary-max  105000
         :wage-type   :yearly
         :posted-date (Date.)
         :close-date  (Date.)}]
    (insert-jobs! [j])))