(ns gainful3-parse.structs.utils)

(defn not-nil? [x] (-> x nil? not))
(defn not-blank? [s] (-> s clojure.string/blank? not))

