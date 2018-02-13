(ns gainful3-parse.specs.job
  (:require [clojure.spec.alpha :as s]
            [gainful3-parse.specs.utils :refer :all]
            [clojure.string :as string]))

(comment
  ;; here's job template:
  {:url         nil
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

(s/def ::url (s/and #(string/starts-with? % "http") not-nil? string? not-blank?))
(s/def ::location (s/and not-nil? string? not-blank?))
(s/def ::government (s/and not-nil? string? not-blank?))
(s/def ::division (s/and not-nil? string? not-blank?))
(s/def ::office (s/or :no-office nil? :has-office (s/and not-nil? string? not-blank?)))
(s/def ::title (s/and not-nil? string? not-blank?))
(s/def ::salary-min number?)
(s/def ::salary-max (s/or :no-bound nil? :bound number?))
(s/def ::wage-type #(valid-wage-types %))
(s/def ::posted-date (s/or :no-bound-posted nil? :date inst?))
(s/def ::close-date (s/or :no-bound-close nil? :date inst?))

(s/def ::job (s/keys :req [::url
                           ::location
                           ::government
                           ::division
                           ::office
                           ::title
                           ::salary-min
                           ::salary-max
                           ::wage-type
                           ::posted-date
                           ::close-date]))
