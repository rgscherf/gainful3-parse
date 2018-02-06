(ns gainful3-parse.orgs.ops
  (:require [etaoin.api :as eta]
            [etaoin.keys :as etak]
            [clojure.string :as string]
            [clojure.set :as set]
            [gainful3-parse.utils.string :as pstring]
            [net.cgrand.enlive-html :as enlive]
            ))

(defn- fetch-from-url
  [url]
  (enlive/html-resource (java.net.URL. url)))

;; create a phantom etaoin driver that will be automatically disposed of.
(defn- ops-postings-for-cat
  "Get URLs for OPS postings, given search criteria for 'category' option."
  [cat-string]
  (eta/with-driver :phantom {} phant
                   ;; now do the following..
                   (doto phant
                     ;; go to URL
                     (eta/go "https://www.gojobs.gov.on.ca/Search.aspx")
                     ;; when the elements are visible, click the most specific one.
                     (eta/click-visible [{:tag :select :id :ctl00_Content_lstRegion} {:tag :option :value :REGION-TRNT}])
                     ;; fill chars with keyboard--selects the option element that starts out of view.
                     (eta/fill {:tag :select :id :ctl00_Content_lstCategory} cat-string)
                     ;; then click the 'submit' button
                     (eta/click-visible [{:tag :input :id :ctl00_Content_bSearch}])
                     ;; wait for loading
                     (eta/wait 3))
                   ;; now take the HTML src string and read it as enlive
                   (let [nodes (-> phant
                                   eta/get-source
                                   java.io.StringReader.
                                   enlive/html-resource
                                   (enlive/select [:div.searchResultWrapper :div.searchResultCol3 :a]))]
                     ;; get urls values
                     (->> nodes
                          ;; get element href attr
                          (map #(get-in % [:attrs :href]))
                          ;; only take English postings
                          (filter #(string/includes? % "Language=English"))
                          ;; and fill full URLs
                          (map #(str "https://www.gojobs.gov.on.ca/" %))))))

;; Template map for all jobs.
;; Contains all needed fields
(def job-template
  {:city        nil
   :government  nil
   :division    nil
   :office      nil
   :title       nil
   :salary-min  nil
   :salary-max  nil
   :hourly?     nil
   :posted-date nil
   :close-date  nil})

(defn- get-job-title
  [parsed]
  (-> parsed
      (enlive/select [:div.GeneralSearchWrapper :h1])
      first
      :content
      first
      pstring/title-case))


(defn- close-date
  "Return the closing date for an OPS job. parsed is an Enlive HTML resource of the job page."
  [parsed]
  (->>
    (enlive/select parsed [:div.GeneralSearchWrapper :div.JobAdHeaderCol1 :i])
    first
    :content
    second
    (drop 1)
    (reduce str)
    (java.util.Date.)))

(defn- posted-date
  "Return the posted date for an OPS job."
  [_]
  (java.util.Date.))

(defn- get-fields-in-page-body
  "From an Enlive-parsed job page, grab job fields in the main page."
  [parsed]
  (let [nodes (-> parsed
                  (enlive/select [:div.JobAdCol1
                                  #{:div.JobAdPositionCol1 :div.JobAdPositionCol2}]))]
    (->> nodes
         ;; get the content of each node
         (map :content)
         ;; group what will be keys and values
         (partition 2)
         (map (fn [[fst snd]]
                ;; make the key for this map...
                {(-> fst
                     second
                     :content
                     first
                     ;; the 'status' key is a hyperlink, so we'll just special-case it.
                     (#(if (string? %)
                         (string/replace % #":" "")
                         "Status"))
                     string/lower-case
                     (string/replace #" " "-")
                     keyword)
                 ;; make the val for this map...
                 (-> snd
                     first
                     string/trim
                     (string/replace #"\s+" " "))}))
         (reduce merge))))

;; Template map for all jobs.
;; Contains all needed fields
(def job-template
  {:city        nil
   :government  nil
   :division    nil
   :office      nil
   :title       nil
   :salary-min  nil
   :salary-max  nil
   :wage-type   nil
   :posted-date nil
   :close-date  nil})

(defn- parse-salary
  "Pull "
  [salary-string]
  (let [lower-salary-string (string/lower-case salary-string)
        salary-type (cond
                      (string/includes? lower-salary-string "hour") :hourly
                      (string/includes? lower-salary-string "week") :weekly
                      :else :yearly)
        cleaned-salary (string/replace salary-string #"([A-Za-z]|\*|,|\$|\.)" "")
        salary-numbers (->> (string/split cleaned-salary #"-")
                            (map string/trim)
                            (map #(try (Double/valueOf ^String %)
                                       (catch NumberFormatException e 0)))
                            (map #(/ % 100)))]
    {:wage-type  salary-type
     :salary-min (first salary-numbers)
     :salary-max (second salary-numbers)}))

(comment
  (map parse-salary test-sals)
  (def test-sals ["$75,924.00 - $110,352.00* Per week"
                  "$1,261.36 - $1,586.61 Per Week*\n*Indicates the salary listed as per the OPSEU Collective Agreement."
                  "$14.00 - $14.85 Per Hour*\n*Indicates the salary listed as per the OPSEU Collective Agreement. "])
  (def salary-string
    "$14.00 - $14.85 Per Hour*\n*Indicates the salary listed as per the OPSEU Collective Agreement. ")
  (def cleaned-salary (string/replace salary-string #"([A-Za-z]|\*|,|\$)" ""))
  )

(defn- body-fields
  "Process body k-vs from get-fields-in-page-body for job teplate shape"
  [parsed]
  (let [{:keys [city organization division salary]} (get-fields-in-page-body parsed)
        {:keys [wage-type salary-min salary-max]} (parse-salary salary)]
    {:city       (if (string/includes? city ",") "Multiple" city)
     :division   organization
     :office     division
     :salary-min salary-min
     :salary-max salary-max
     :wage-type  wage-type}))

(defn- make-map
  [[url parsed]]
  (merge job-template
         {:url         url
          :title       (get-job-title parsed)
          :close-date  (close-date parsed)
          :posted-date (posted-date parsed)
          :government  "Ontario Public Service"}
         (body-fields parsed)))

(defn execute
  [current-urls]
  (let [keywords ["policy"
                  "consulting"]]
    (->> keywords
         (pmap ops-postings-for-cat)
         (reduce into #{})
         (#(set/difference % current-urls))
         (pmap (fn [url] [url (fetch-from-url url)]))
         (map make-map))))

