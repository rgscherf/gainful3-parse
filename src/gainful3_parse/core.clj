(ns gainful3-parse.core
  (:require [clojure.spec.alpha :as spec]
            [gainful3-parse.utils.logging :as log]
            [gainful3-parse.db.main :as db]
            [gainful3-parse.orgs.ops]
            [gainful3-parse.orgs.toronto]
            [gainful3-parse.structs.utils :as struct-utils]
            [gainful3-parse.structs.job]))

(defn -main
  [& args]
  ;; prune expired jobs from DB
  ;; ???

  ;; get all current job URLs from DB, in a set
  (let [current-job-urls (db/all-job-urls)
        scrape-fns [gainful3-parse.orgs.ops/execute
                    gainful3-parse.orgs.toronto/execute]]
    ;; apply each of the fns, with the current job URLS (current URLs are not re-parsed)
    (->> scrape-fns
         (map #(% current-job-urls))
         flatten
         ;; ensure only valid jobs are being returned
         (reduce (partial struct-utils/check-conformances :struct/job) [])
         ;; insert all the jobs
         db/insert-jobs!)
    ))

(comment
  (time (-main))
  (-main)
  (db/all-jobs)
  (count (db/all-jobs))
  (->> (db/all-jobs)
       (filter #(= "Ontario Public Service" (:job/government %)))
       count)
  (frequencies (map :job/url (db/all-jobs))))
