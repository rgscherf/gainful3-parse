(ns gainful3-parse.utils.string
  (:require [clojure.string :as string]))

(defn title-case
  "Convert a string to Title Case."
  [input-str]
  (->> input-str
       string/trim
       string/lower-case
       (#(string/split % #" "))
       (map string/capitalize)
       (string/join " ")))

(defn salary-type
  [salary-string]
  (let [lower-salary-string (string/lower-case salary-string)
        salary-contains? (fn [needle] (string/includes? lower-salary-string needle))]
    (cond
      (salary-contains? "hour") :hourly
      (salary-contains? "week") :weekly
      :else :annual)))


