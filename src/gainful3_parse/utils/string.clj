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

