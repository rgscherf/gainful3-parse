(ns gainful3-parse.structs.job
  (:require [clojure.spec.alpha :as s]
            [gainful3-parse.structs.utils :refer :all]))

(def valid-wage-types
  #{:annual
    :weekly
    :hourly})

(s/def :job/city (s/and not-nil? string? not-blank?))
(s/def :job/government (s/and not-nil? string? not-blank?))
(s/def :job/division (s/and not-nil? string? not-blank?))
(s/def :job/office (s/and not-nil? string? not-blank?))
(s/def :job/title (s/and not-nil? string? not-blank?))
(s/def :job/salary-min number?)
(s/def :job/salary-max number?)
(s/def :job/wage-type #(valid-wage-types %))
(s/def :job/posted-date inst?)
(s/def :job/close-date inst?)

(s/def :structs/job (s/keys :req [:job/city
                                  :job/government
                                  :job/division
                                  :job/office
                                  :job/title
                                  :job/salary-min
                                  :job/salary-max
                                  :job/wage-type
                                  :job/posted-date
                                  :job/close-date]))
