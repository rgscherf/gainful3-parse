(ns gainful3-parse.core
  (:require [gainful3-parse.orgs.ops]))


(defn -main [& args]
  ;; prune expired jobs from DB
  (comment "???")

  ;; get all current job URLs from DB, in a set
  (def current-job-urls #{})

  ;; make a coll of all scraping fns
  (def scrape-fns [
                   gainful3-parse.orgs.ops/execute
                   ])

  ;; apply each of the fns, with the current job URLS (current URLs are not re-parsed)
  (->> scrape-fns
       (map #(% current-job-urls))
       flatten)

  ;; insert all these new jobs into DB
  (comment "???"))

