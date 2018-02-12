(ns gainful3-parse.structs.utils
  (:require [clojure.spec.alpha :as spec]
            [gainful3-parse.utils.logging :as log]))

(defn not-nil? [x] (-> x nil? not))
(defn not-blank? [s] (-> s clojure.string/blank? not))

(defn check-structs
  [spec structs]
  (->> structs
       (filter (fn [s] (not (spec/valid? spec s))))
       (map (partial spec/explain-data spec))))

(defn check-conformances
  [conform-spec collected new]
  (if (spec/invalid? conform-spec)
    (do
      (log/log (str "INVALID SPEC FOR " conform-spec ", conform data: " (spec/explain conform-spec new)))
      collected)
    (conj collected new)))
