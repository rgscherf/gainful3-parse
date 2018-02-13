(ns gainful3-parse.specs.utils
  (:require [clojure.spec.alpha :as spec]
            [gainful3-parse.utils.logging :as log]))

(defn not-nil? [x] (-> x nil? not))
(defn not-blank? [s] (-> s clojure.string/blank? not))

(defn check-structs
  [spec structs]
  (->> structs
       (filter (fn [s] (not (spec/valid? spec s))))
       (map (partial spec/explain-data spec))))

(defn- filter-for-conformance*
  [conform-spec collected new]
  (if (not (spec/valid? conform-spec new))
    (do
      (log/info (str "INVALID SPEC FOR " conform-spec ", conform data: " (spec/explain-data conform-spec new)))
      collected)
    (conj collected new)))

(defn filter-for-conformance
  [conform-spec coll]
  (reduce (partial filter-for-conformance* conform-spec) [] coll))
