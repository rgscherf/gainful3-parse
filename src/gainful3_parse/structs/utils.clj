(ns gainful3-parse.structs.utils
  (:require [clojure.spec.alpha :as s]))

(defn not-nil? [x] (-> x nil? not))
(defn not-blank? [s] (-> s clojure.string/blank? not))

(defn check-structs
  [spec structs]
  (->> structs
       (filter (fn [s] (not (s/valid? spec s))))
       (map (partial s/explain-data spec))))
