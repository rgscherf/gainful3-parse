(ns gainful3-parse.core
  (:require [clojure.spec.alpha :as spec]
            [gainful3-parse.utils.logging :as log]
            [gainful3-parse.db.main :as db]
            [gainful3-parse.orgs.ops]
            [gainful3-parse.orgs.toronto]
            [gainful3-parse.specs.utils :as spec-utils]
            [gainful3-parse.specs.job :as job]
            ))

(def all-job-urls (db/all-job-urls))

(defn -main
  [& _]
  ;; TODO: prune expired jobs from DB

  (let [scrape-fns [gainful3-parse.orgs.ops/execute
                    gainful3-parse.orgs.toronto/execute]]
    ;; now scrape job sites using fns loaded above.
    (->> scrape-fns
         ;; apply each of the fns, with the current job URLS (current URLs are not re-parsed)
         (mapcat #(% all-job-urls))
         ;; insert-jobs! checks for spec conformance and duplication.
         db/insert-jobs!)
    ))

(comment
  (mapcat #(-> % first vector) (partition 2 (db/all-job-urls)))
  (time (-main))
  (-main)
  (db/all-jobs)
  (count (db/all-jobs))
  (->> (db/all-jobs)
       (filter #(= "Ontario Public Service" (::job/government %)))
       count)
  (spec-utils/filter-for-conformance ::job/job [{:hello :world}])
  (reduce (partial spec-utils/filter-for-conformance* ::job/job) [] (db/all-jobs))
  (spec-utils/filter-for-conformance ::job/job (db/all-jobs))
  (map) (partial spec-utils/filter-for-conformance ::job/job) (db/all-jobs)
  (map (partial spec-utils/filter-for-conformance ::job/job) (db/all-jobs))
  (frequencies (map ::job/url (db/all-jobs))))
