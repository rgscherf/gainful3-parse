(ns gainful3-parse.orgs.toronto
  (:require [net.cgrand.enlive-html :as enlive]
            [gainful3-parse.utils.string :as pstring]
            [clojure.string :as string])
  (:import (java.net URL)
           (java.util Date)))

(def toronto-search-url
  (URL. "https://www.brainhunter.com/frontoffice/searchSeekerJobAction.do?sitecode=pl389"))

(defn- job-urls
  []
  (let [searchpage (enlive/html-resource toronto-search-url)]
    (->>
      (enlive/select searchpage [:table.job_list_table :a.joblisttable_link])
      (map #(get-in % [:attrs :href]))
      (map #(str "https://www.brainhunter.com/frontoffice/" %))
      (into #{}))))

(defn- job-details-cells
  [job-url]
  (-> job-url
      URL.
      enlive/html-resource
      (enlive/select [:table#job_desc
                      :table.tablebackground_job_description
                      :tr
                      #{:td.job_header_text_bold :td.job_header_text}])))

(defn- get-job-fields
  [job-url]
  (let [parsed (job-details-cells job-url)]
    (->> (partition 2 parsed)
         (map (fn [x] [(-> x first :content first)
                       (-> x second :content first)]))
         (map (fn [[fst snd]] [(string/trim fst)
                               snd]))
         (into {}))))

(defn- salary-min-max
  [salary-string]
  (->>
    (string/split salary-string #"-")
    (map (fn [s] (string/replace s #"(,|\.)" "")))
    (map (fn [s] (re-find #"\d+" s)))
    (map #(try (Double/valueOf ^String %)
               (catch NumberFormatException e 0)))
    (map #(/ % 100))))

(defn- parse-salary
  [salary-string]
  (let [[min-sal max-sal] (salary-min-max salary-string)]
    {:type (pstring/salary-type salary-string)
     :min  min-sal
     :max  max-sal}))

(defn- try-date
  [date-string]
  (try (Date. ^String date-string)
       (catch Exception _ nil)))

(defn- scrape-single-job
  [job-url]
  (let [fields (get-job-fields job-url)
        from-fields (fn [field] (try (string/trim (get fields field))
                                     (catch Exception _ (get fields field))))
        {:keys [type min max]} (parse-salary (from-fields "Salary/Rate"))]
    #:job{:url         job-url
          :location    "Toronto"
          :government  "City of Toronto"
          :division    (from-fields "Division")
          :office      (from-fields "Section")
          :title       (-> "Job Classification Title" from-fields pstring/title-case)
          :salary-min  min
          :salary-max  max
          :wage-type   type
          :posted-date (-> "Posting Date" from-fields try-date)
          :close-date  (-> "Closing Date" from-fields try-date)}))

(defn execute
  [banned-urls]
  (map
    scrape-single-job
    (clojure.set/difference (job-urls) banned-urls)))
