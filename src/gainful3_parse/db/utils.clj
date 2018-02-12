(ns gainful3-parse.db.utils
  (:import (java.util Date)))

(defn date->unix
  "Convert a j.u.Date to UNIX timestamp (in seconds)"
  [^Date date]
  (when date
    (-> date
        .toInstant
        .getEpochSecond)))

(defn unix->date
  "Convert UNIX timestamp (in milliseconds) to j.u.Date."
  [unix]
  (when unix
    (->> unix
         (* 1000)
         Date.)))

(defn struct->dbjob
  "Convert a :struct/job to matching DB row."
  [{:keys [job/url job/location job/government job/division job/office job/title job/salary-min job/salary-max job/wage-type job/posted-date job/close-date]}]
  {:url          url
   :location     location
   :government   government
   :division     division
   :office       office
   :title        title
   :salary_min   salary-min
   :salary_max   salary-max
   :wage_type    (name wage-type)
   :posted_date  (date->unix posted-date)
   :close_date   (date->unix close-date)
   :created_date (date->unix (Date.))})

(defn dbjob->struct
  "Convert a job retrieved from DB into a map matching :struct/job spec."
  [{:keys [salary_min division close_date salary_max created_date posted_date title office wage_type id url location government]}]
  #:job{:url          url
        :location     location
        :government   government
        :division     division
        :office       office
        :title        title
        :salary-min   salary_min
        :salary-max   salary_max
        :wage-type    (keyword wage_type)
        :posted-date  (unix->date posted_date)
        :close-date   (unix->date close_date)
        :created-date (unix->date created_date)})

