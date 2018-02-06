(ns gainful3-parse.structs.job
  (:require [clojure.spec.alpha :as s]
            [gainful3-parse.structs.utils :refer :all]
            [clojure.string :as string]))

(comment
  ;; here's job template:
  #:job{:url         nil
        :location    nil
        :government  nil
        :division    nil
        :office      nil
        :title       nil
        :salary-min  nil
        :salary-max  nil
        :wage-type   nil
        :posted-date nil
        :close-date  nil})

(def valid-wage-types
  #{:annual
    :weekly
    :hourly})

(s/def :job/url (s/and #(string/starts-with? % "http") not-nil? string? not-blank?))
(s/def :job/location (s/and not-nil? string? not-blank?))
(s/def :job/government (s/and not-nil? string? not-blank?))
(s/def :job/division (s/and not-nil? string? not-blank?))
(s/def :job/office (s/or :no-office nil? :has-office (s/and not-nil? string? not-blank?)))
(s/def :job/title (s/and not-nil? string? not-blank?))
(s/def :job/salary-min number?)
(s/def :job/salary-max (s/or :no-bound nil? :bound number?))
(s/def :job/wage-type #(valid-wage-types %))
(s/def :job/posted-date (s/or :no-bound-posted nil? :date inst?))
(s/def :job/close-date (s/or :no-bound-close nil? :date inst?))

(s/def :struct/job (s/keys :req [:job/url
                                 :job/location
                                 :job/government
                                 :job/division
                                 :job/office
                                 :job/title
                                 :job/salary-min
                                 :job/salary-max
                                 :job/wage-type
                                 :job/posted-date
                                 :job/close-date]))
